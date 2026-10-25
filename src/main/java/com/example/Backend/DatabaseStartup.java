package com.example.Backend;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Starts the optional local transit service on a background thread. */
public final class DatabaseStartup {
    private static final String DUMP = "data/gtfs/dump-gtfs-202406251610.sql";
    private static final Duration COMMAND_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration READY_TIMEOUT = Duration.ofMinutes(15);
    private static final Pattern POINTER = Pattern.compile(
            "\\Aversion https://git-lfs.github.com/spec/v1\\Roid sha256:([0-9a-f]{64})\\Rsize ([0-9]+)\\s*\\z");
    private final CommandRunner runner;
    private final DatabaseProbe probe;
    private final Settings settings;
    private final Sleeper sleeper;

    public record Result(boolean ready, String reason) {
    }

    record CommandResult(int exitCode, String output, boolean timedOut) {
    }

    @FunctionalInterface
    interface CommandRunner {
        CommandResult run(List<String> command, Path directory, Duration timeout)
                throws IOException, InterruptedException;
    }

    @FunctionalInterface
    interface DatabaseProbe {
        boolean isReady(DatabaseConfig.ConnectionSettings settings) throws SQLException;
    }

    interface Settings {
        String get(String property, String environmentVariable);
        Path workingDirectory();
        Path codeLocation();
    }

    @FunctionalInterface
    interface Sleeper {
        void sleep(Duration duration) throws InterruptedException;
    }

    DatabaseStartup(CommandRunner runner, DatabaseProbe probe, Settings settings, Sleeper sleeper) {
        this.runner = runner;
        this.probe = probe;
        this.settings = settings;
        this.sleeper = sleeper;
    }

    public static Result ensureReady(Consumer<String> progress) {
        return new DatabaseStartup(DatabaseStartup::runCommand, DatabaseStartup::probeDatabase,
                systemSettings(), duration -> Thread.sleep(duration.toMillis())).start(progress);
    }

    Result start(Consumer<String> progress) {
        Consumer<String> report = progress == null ? ignored -> { } : progress;
        try {
            checkInterrupted();
            DatabaseConfig.ConnectionSettings manual = new DatabaseConfig.ConnectionSettings(
                    settings.get("transitor.db.url", "TRANSITOR_DB_URL"),
                    settings.get("transitor.db.user", "TRANSITOR_DB_USER"),
                    settings.get("transitor.db.password", "TRANSITOR_DB_PASSWORD"));
            if (manual.isPresent()) {
                if (!manual.isComplete()) {
                    return new Result(false, "Transit database settings are incomplete. Set TRANSITOR_DB_URL, "
                            + "TRANSITOR_DB_USER and TRANSITOR_DB_PASSWORD, or remove all three to use Docker.");
                }
                report.accept("Checking the configured transit database…");
                return probe.isReady(manual)
                        ? new Result(true, "Configured transit database is ready.")
                        : new Result(false, "The configured transit database has no imported GTFS data.");
            }
            if ("false".equalsIgnoreCase(settings.get("transitor.db.autoStart", "TRANSITOR_DB_AUTO_START"))) {
                return new Result(false, "Automatic transit database startup is disabled.");
            }
            Path root = findProjectRoot();
            report.accept("Checking Docker and Docker Compose…");
            requireSuccess(command(List.of("docker", "info", "--format", "{{.ServerVersion}}"), root,
                    COMMAND_TIMEOUT), "Docker is unavailable. Start Docker Desktop or the Docker daemon, then reopen Transitor.");
            requireSuccess(command(List.of("docker", "compose", "version", "--short"), root,
                    COMMAND_TIMEOUT), "Docker Compose is unavailable. Install Docker Compose v2, then reopen Transitor.");
            String project = projectName(root);
            CommandResult volume = command(List.of("docker", "volume", "inspect", "--format", "{{.Name}}",
                    project + "_db-data"), root, COMMAND_TIMEOUT);
            if (volume.timedOut() || (volume.exitCode() != 0
                    && !(volume.exitCode() == 1 && volume.output().toLowerCase(java.util.Locale.ROOT).contains("no such volume")))) {
                throw new StartupFailure("Docker could not inspect the local database volume. Check Docker and retry.");
            }
            boolean existingVolume = volume.exitCode() == 0;
            Credentials credentials = credentials(root, project, existingVolume);
            List<String> compose = List.of("docker", "compose", "--project-name", project, "--env-file",
                    root.resolve(".transitor/docker.env").toString(), "-f", root.resolve("compose.yaml").toString());
            if (!existingVolume) {
                prepareDump(root, report);
            }
            report.accept(existingVolume ? "Starting the saved transit database…"
                    : "Starting the transit database. The first import can take several minutes…");
            startContainer(compose, root);
            boolean fetched = !existingVolume;
            long deadline = System.nanoTime() + READY_TIMEOUT.toNanos();
            for (int attempt = 0; attempt < 180 && System.nanoTime() < deadline; attempt++) {
                checkInterrupted();
                CommandResult container = command(append(compose, "ps", "--all", "--quiet", "db"), root, COMMAND_TIMEOUT);
                requireSuccess(container, "Docker could not inspect the transit service. Check Docker and retry.");
                String id = container.output().trim();
                if (!id.matches("[0-9a-fA-F]{12,64}")) {
                    throw new StartupFailure("Docker did not create the transit database container. Check Docker Compose.");
                }
                CommandResult state = command(List.of("docker", "inspect", "--format",
                        "{{.State.Status}}|{{.State.ExitCode}}|{{if .State.Health}}{{.State.Health.Status}}{{end}}", id),
                        root, COMMAND_TIMEOUT);
                requireSuccess(state, "Docker could not inspect database startup. Check Docker and retry.");
                String[] fields = state.output().trim().split("\\|", -1);
                if (fields.length != 3) {
                    throw new StartupFailure("Docker returned an unexpected database status. Check Docker Compose.");
                }
                if ("healthy".equals(fields[2])) {
                    CommandResult port = command(append(compose, "port", "db", "3306"), root, COMMAND_TIMEOUT);
                    requireSuccess(port, "Docker did not publish a local database port.");
                    Matcher address = Pattern.compile("127\\.0\\.0\\.1:([0-9]{1,5})").matcher(port.output().trim());
                    if (!address.matches()) {
                        throw new StartupFailure("The database must publish a port only on 127.0.0.1. Check compose.yaml.");
                    }
                    int number = Integer.parseInt(address.group(1));
                    if (number < 1 || number > 65535) {
                        throw new StartupFailure("Docker returned an invalid local database port.");
                    }
                    DatabaseConfig.ConnectionSettings automatic = new DatabaseConfig.ConnectionSettings(
                            "jdbc:mysql://127.0.0.1:" + number + "/gtfs", "transitor", credentials.password());
                    if (!probe.isReady(automatic)) {
                        throw new StartupFailure("The local transit database is running, but GTFS data is unavailable.");
                    }
                    DatabaseConfig.useAutomatic(automatic);
                    report.accept("Transit database is ready.");
                    return new Result(true, "Local transit database is ready.");
                }
                if ("exited".equals(fields[0]) || "dead".equals(fields[0])) {
                    if ("42".equals(fields[1]) && !fetched) {
                        prepareDump(root, report);
                        fetched = true;
                        startContainer(compose, root);
                        deadline = System.nanoTime() + READY_TIMEOUT.toNanos();
                        attempt = 0;
                        continue;
                    }
                    String reason = switch (fields[1]) {
                        case "42" -> "The GTFS dump is still missing after download. Check Git LFS and restart Transitor.";
                        case "43" -> "GTFS verification failed. Restore the original Git LFS dump before retrying.";
                        case "44" -> "The saved database has incompatible or incomplete data. Review the local database; it was not overwritten.";
                        default -> "The transit database import or startup failed. Inspect the db service with Docker Compose logs.";
                    };
                    throw new StartupFailure(reason);
                }
                if (attempt % 12 == 0) {
                    report.accept("Waiting for transit data. The first import can take several minutes; walking and cycling remain available…");
                }
                sleeper.sleep(Duration.ofSeconds(5));
            }
            throw new StartupFailure("The transit database is still initializing after 15 minutes. "
                    + "Check Docker Compose logs; reopen Transitor after the import finishes.");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new Result(false, "Transit database startup was cancelled.");
        } catch (SQLException exception) {
            return new Result(false, "The transit database could not be reached. Check its connection settings, credentials and GTFS import.");
        } catch (StartupFailure exception) {
            return new Result(false, exception.getMessage());
        } catch (OverlappingFileLockException exception) {
            return new Result(false, "Another Transitor instance is preparing database credentials. Retry shortly.");
        } catch (InvalidPathException exception) {
            return new Result(false, "The project directory path is invalid. Check TRANSITOR_PROJECT_DIR.");
        } catch (IOException exception) {
            return new Result(false, "Transit setup could not run a required command or access local files. "
                    + "Check Docker, Git LFS and the project directory permissions.");
        }
    }

    private void startContainer(List<String> compose, Path root) throws IOException, InterruptedException, StartupFailure {
        requireSuccess(command(append(compose, "up", "-d", "db"), root, Duration.ofMinutes(5)),
                "Docker Compose could not start the database. Check Docker and image download access.");
    }

    private Path findProjectRoot() throws IOException, StartupFailure {
        String override = settings.get("transitor.projectDir", "TRANSITOR_PROJECT_DIR");
        if (override != null && !override.isBlank()) {
            Path candidate = settings.workingDirectory().resolve(override).toAbsolutePath().normalize();
            if (isProjectRoot(candidate)) {
                return candidate.toRealPath();
            }
            throw new StartupFailure("TRANSITOR_PROJECT_DIR must point to the project containing compose.yaml, pom.xml and data/gtfs.");
        }
        for (Path origin : new Path[]{settings.workingDirectory(), settings.codeLocation()}) {
            for (Path candidate = origin; candidate != null; candidate = candidate.getParent()) {
                if (isProjectRoot(candidate)) {
                    return candidate.toRealPath();
                }
            }
        }
        throw new StartupFailure("The Transitor project directory could not be found. Set TRANSITOR_PROJECT_DIR to the checkout.");
    }

    private static boolean isProjectRoot(Path path) {
        return Files.isRegularFile(path.resolve("compose.yaml")) && Files.isRegularFile(path.resolve("pom.xml"))
                && Files.isDirectory(path.resolve("data/gtfs"));
    }

    static String projectName(Path root) {
        return "transitor-" + HexFormat.of().formatHex(sha256().digest(root.toString().getBytes(StandardCharsets.UTF_8))).substring(0, 12);
    }

    private record Credentials(String password) {
        @Override
        public String toString() {
            return "Credentials[redacted]";
        }
    }

    private static Credentials credentials(Path root, String project, boolean existingVolume) throws IOException, StartupFailure {
        Path directory = root.resolve(".transitor");
        Path file = directory.resolve("docker.env");
        if (Files.isSymbolicLink(directory) || Files.isSymbolicLink(file)) {
            throw new StartupFailure("Local database credentials must be regular files inside the project .transitor directory.");
        }
        Files.createDirectories(directory);
        privatePermissions(directory, "rwx------");
        try (FileChannel channel = FileChannel.open(directory.resolve("credentials.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock lock = channel.tryLock()) {
            if (lock == null) {
                throw new StartupFailure("Another Transitor instance is preparing database credentials. Retry shortly.");
            }
            if (!Files.exists(file)) {
                if (existingVolume) {
                    throw new StartupFailure("The saved database exists but .transitor/docker.env is missing. "
                            + "Restore that credentials file; existing database passwords were not changed.");
                }
                byte[] bytes = new byte[32];
                SecureRandom random = new SecureRandom();
                random.nextBytes(bytes);
                String rootPassword = HexFormat.of().formatHex(bytes);
                random.nextBytes(bytes);
                String password = HexFormat.of().formatHex(bytes);
                Path temporary = Files.createTempFile(directory, "docker-", ".tmp");
                try {
                    privatePermissions(temporary, "rw-------");
                    Files.writeString(temporary, "COMPOSE_PROJECT_NAME=" + project + "\nTRANSITOR_DOCKER_ROOT_PASSWORD="
                            + rootPassword + "\nTRANSITOR_DOCKER_PASSWORD=" + password + "\n", StandardCharsets.UTF_8);
                    try {
                        Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE);
                    } catch (AtomicMoveNotSupportedException exception) {
                        Files.move(temporary, file);
                    }
                } finally {
                    Files.deleteIfExists(temporary);
                }
            }
            if (Files.size(file) > 4096) {
                throw new StartupFailure("The local database credentials file is invalid. Restore .transitor/docker.env.");
            }
            privatePermissions(file, "rw-------");
            Properties values = new Properties();
            try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                try {
                    values.load(reader);
                } catch (IllegalArgumentException exception) {
                    throw new StartupFailure("The local database credentials file is malformed. Restore .transitor/docker.env.");
                }
            }
            String password = values.getProperty("TRANSITOR_DOCKER_PASSWORD", "");
            if (!project.equals(values.getProperty("COMPOSE_PROJECT_NAME"))
                    || !password.matches("[0-9a-f]{64}")
                    || !values.getProperty("TRANSITOR_DOCKER_ROOT_PASSWORD", "").matches("[0-9a-f]{64}")) {
                throw new StartupFailure("The local database credentials do not match this project. "
                        + "Review .transitor/docker.env; no passwords were changed.");
            }
            return new Credentials(password);
        }
    }

    private void prepareDump(Path root, Consumer<String> report) throws IOException, InterruptedException, StartupFailure {
        Path dump = root.resolve(DUMP);
        String metadata = Files.isRegularFile(dump) && Files.size(dump) <= 1024
                ? Files.readString(dump, StandardCharsets.UTF_8) : "";
        Matcher pointer = POINTER.matcher(metadata);
        boolean isPointer = pointer.matches();
        if (!isPointer) {
            CommandResult tracked = command(List.of("git", "show", "HEAD:" + DUMP), root, COMMAND_TIMEOUT);
            requireSuccess(tracked, "Git LFS metadata could not be read. Run Transitor from its Git checkout.");
            pointer = POINTER.matcher(tracked.output());
            if (!pointer.matches()) {
                throw new StartupFailure("The tracked GTFS file is not a valid Git LFS pointer. Restore the original dataset metadata.");
            }
        }
        String expectedHash = pointer.group(1);
        long expectedSize;
        try {
            expectedSize = Long.parseLong(pointer.group(2));
        } catch (NumberFormatException exception) {
            throw new StartupFailure("The Git LFS dataset size is invalid.");
        }
        if (isPointer || !Files.exists(dump)) {
            report.accept("Downloading the GTFS dataset with Git LFS. This first download is about 1.1 GB…");
            requireSuccess(command(List.of("git", "lfs", "version"), root, COMMAND_TIMEOUT),
                    "Git LFS is unavailable. Install Git LFS, then reopen Transitor.");
            requireSuccess(command(List.of("git", "lfs", "pull", "--include=" + DUMP, "--exclude="), root, Duration.ofMinutes(10)),
                    "The GTFS download failed or timed out. Check Git LFS, GitHub access and your network, then retry.");
        }
        report.accept("Verifying the downloaded GTFS dataset…");
        if (!Files.isRegularFile(dump) || Files.size(dump) != expectedSize) {
            throw new StartupFailure("The GTFS dataset is incomplete. Check Git LFS and restore the original dump.");
        }
        MessageDigest digest = sha256();
        try (InputStream stream = Files.newInputStream(dump)) {
            byte[] buffer = new byte[1024 * 1024];
            int count;
            while ((count = stream.read(buffer)) >= 0) {
                checkInterrupted();
                digest.update(buffer, 0, count);
            }
        }
        if (!expectedHash.equals(HexFormat.of().formatHex(digest.digest()))) {
            throw new StartupFailure("The GTFS dataset checksum does not match Git LFS metadata. Restore the original dump.");
        }
    }

    private CommandResult command(List<String> args, Path directory, Duration timeout)
            throws InterruptedException, StartupFailure {
        checkInterrupted();
        try {
            return runner.run(args, directory, timeout);
        } catch (IOException exception) {
            throw new StartupFailure(args.get(0).equals("docker")
                    ? "Docker could not be started. Install and start Docker Desktop or the Docker daemon, then reopen Transitor."
                    : "Git or Git LFS could not be started. Install Git and Git LFS, then reopen Transitor.");
        }
    }

    private static void requireSuccess(CommandResult result, String reason) throws StartupFailure {
        if (result.timedOut() || result.exitCode() != 0) {
            throw new StartupFailure(reason);
        }
    }

    private static List<String> append(List<String> prefix, String... arguments) {
        List<String> result = new ArrayList<>(prefix);
        result.addAll(List.of(arguments));
        return result;
    }

    private static void checkInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Java does not provide SHA-256.", exception);
        }
    }

    private static void privatePermissions(Path path, String permissions) throws IOException {
        try {
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(permissions));
        } catch (UnsupportedOperationException ignored) {
            // Windows inherits access controls from the user's project directory.
        }
    }

    private static boolean probeDatabase(DatabaseConfig.ConnectionSettings settings) throws SQLException {
        try (var connection = DatabaseConfig.openConnection(settings);
             var statement = connection.prepareStatement("SELECT 1 FROM points LIMIT 1")) {
            statement.setQueryTimeout(10);
            try (var rows = statement.executeQuery()) {
                return rows.next();
            }
        }
    }

    private static Settings systemSettings() {
        return new Settings() {
            public String get(String property, String environmentVariable) {
                String value = System.getProperty(property);
                return value != null ? value : System.getenv(environmentVariable);
            }
            public Path workingDirectory() {
                return Path.of("").toAbsolutePath();
            }
            public Path codeLocation() {
                try {
                    return Path.of(DatabaseStartup.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                } catch (URISyntaxException | NullPointerException | IllegalArgumentException exception) {
                    return workingDirectory();
                }
            }
        };
    }

    private static CommandResult runCommand(List<String> command, Path directory, Duration timeout)
            throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true);
        builder.environment().put("GIT_TERMINAL_PROMPT", "0");
        builder.environment().put("GCM_INTERACTIVE", "Never");
        Process process = builder.start();
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        Thread reader = new Thread(() -> {
            try (InputStream stream = process.getInputStream()) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = stream.read(buffer)) >= 0) {
                    synchronized (captured) {
                        int remaining = 16384 - captured.size();
                        if (remaining > 0) captured.write(buffer, 0, Math.min(count, remaining));
                    }
                }
            } catch (IOException ignored) {
                // A timed out or cancelled process may close its output stream.
            }
        }, "transitor-command-output");
        reader.setDaemon(true);
        reader.start();
        try {
            boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                stopProcess(process);
            }
            reader.join(1000);
            synchronized (captured) {
                return new CommandResult(finished ? process.exitValue() : -1,
                        captured.toString(StandardCharsets.UTF_8), !finished);
            }
        } catch (InterruptedException exception) {
            stopProcess(process);
            throw exception;
        }
    }

    private static void stopProcess(Process process) {
        process.descendants().forEach(child -> child.destroyForcibly());
        process.destroyForcibly();
    }

    private static final class StartupFailure extends Exception {
        StartupFailure(String message) {
            super(message);
        }
    }
}
