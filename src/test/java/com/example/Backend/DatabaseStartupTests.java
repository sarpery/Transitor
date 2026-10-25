package com.example.Backend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseStartupTests {
    @TempDir
    Path directory;

    @AfterEach
    void clearTestInterruption() {
        Thread.interrupted();
        DatabaseConfig.useAutomatic(null);
    }

    @Test
    void explicitDatabaseSettingsTakePrecedenceOverDocker() {
        FakeSettings settings = new FakeSettings(directory, directory);
        settings.values.put("transitor.db.url", "jdbc:mysql://localhost:3307/coursework");
        settings.values.put("transitor.db.user", "coursework-user");
        settings.values.put("transitor.db.password", "manual-test-password");
        List<String> progress = new ArrayList<>();
        DatabaseStartup.Result result = startup(noCommands(), config -> {
            assertEquals("jdbc:mysql://localhost:3307/coursework", config.url());
            assertEquals("coursework-user", config.user());
            assertEquals("manual-test-password", config.password());
            return true;
        }, settings).start(progress::add);
        assertTrue(result.ready(), result.reason());
        assertTrue(progress.stream().noneMatch(message -> message.contains("manual-test-password")));
    }

    @Test
    void partialManualSettingsFailWithoutSilentlyStartingDocker() {
        FakeSettings settings = new FakeSettings(directory, directory);
        settings.values.put("transitor.db.url", "jdbc:mysql://localhost:3307/coursework");
        DatabaseStartup.Result result = startup(noCommands(), noProbe(), settings).start(message -> { });
        assertFalse(result.ready());
        assertTrue(result.reason().toLowerCase().contains("config")
                || result.reason().contains("TRANSITOR_DB"), result.reason());
    }

    @Test
    void automaticStartupCanBeDisabledWithoutDockerOrProjectFiles() {
        FakeSettings settings = new FakeSettings(directory, directory);
        settings.values.put("transitor.db.autoStart", "false");
        DatabaseStartup.Result result = startup(noCommands(), noProbe(), settings).start(message -> { });
        assertFalse(result.ready());
        assertTrue(result.reason().toLowerCase().contains("disabled"), result.reason());
    }

    @Test
    void unreachableManualDatabaseDoesNotFallBackToDockerOrExposeDriverDetails() {
        FakeSettings settings = new FakeSettings(directory, directory);
        settings.values.put("transitor.db.url", "jdbc:mysql://localhost:3307/coursework");
        settings.values.put("transitor.db.user", "coursework-user");
        settings.values.put("transitor.db.password", "private-test-password");
        List<String> progress = new ArrayList<>();
        DatabaseStartup.Result result = startup(noCommands(), config -> {
            throw new SQLException("A driver error containing private-test-password");
        }, settings).start(progress::add);
        assertFalse(result.ready());
        assertFalse(result.reason().contains("private-test-password"));
        assertTrue(progress.stream().noneMatch(message -> message.contains("private-test-password")));
    }

    @Test
    void dockerFailureIsDetectedBeforeFetchingTheLargeLfsDump() throws Exception {
        Path project = createProject(directory.resolve("project"), "small SQL fixture\n");
        List<List<String>> commands = new ArrayList<>();
        DatabaseStartup.CommandRunner runner = (command, cwd, timeout) -> {
            commands.add(List.copyOf(command));
            assertEquals(project.toRealPath(), cwd.toRealPath());
            assertEquals("docker", command.get(0));
            return new DatabaseStartup.CommandResult(1, "Docker daemon unavailable", false);
        };
        DatabaseStartup.Result result = startup(runner, noProbe(),
                new FakeSettings(project, project.resolve("target/classes"))).start(message -> { });
        assertFalse(result.ready());
        assertTrue(result.reason().toLowerCase().contains("docker"), result.reason());
        assertFalse(commands.isEmpty());
        assertTrue(commands.stream().noneMatch(command -> command.contains("lfs")));
        assertFalse(Files.exists(project.resolve(".transitor/docker.env")));
    }

    @Test
    void missingDockerExecutableGivesAnActionableFailureWithoutFetchingData() throws Exception {
        Path project = createProject(directory.resolve("project"), "small SQL fixture\n");
        DatabaseStartup.CommandRunner runner = (command, cwd, timeout) -> {
            assertEquals("docker", command.get(0));
            throw new IOException("Cannot run program docker");
        };
        DatabaseStartup.Result result = startup(runner, noProbe(),
                new FakeSettings(project, project)).start(message -> { });
        assertFalse(result.ready());
        assertTrue(result.reason().contains("Docker"), result.reason());
        assertTrue(result.reason().toLowerCase().contains("install"), result.reason());
        assertFalse(Files.exists(project.resolve(".transitor/docker.env")));
    }

    @Test
    void unknownVolumeInspectionFailureDoesNotCreateCredentialsOrInitializeData() throws Exception {
        String sql = "CREATE TABLE sample (id INTEGER);\n";
        Path project = createProject(directory.resolve("project"), sql);
        DockerFixture docker = new DockerFixture(project, sql);
        DatabaseStartup.CommandRunner runner = (command, cwd, timeout) -> {
            if (command.get(1).equals("volume")) {
                return new DatabaseStartup.CommandResult(1, "Permission denied", false);
            }
            return docker.run(command, cwd, timeout);
        };
        DatabaseStartup.Result result = startup(runner, noProbe(),
                new FakeSettings(project, project)).start(message -> { });
        assertFalse(result.ready());
        assertTrue(result.reason().toLowerCase().contains("volume"), result.reason());
        assertFalse(Files.exists(project.resolve(".transitor/docker.env")));
        assertEquals(0, docker.lfsPulls);
        assertEquals(0, docker.composeStarts);
    }

    @Test
    void projectCanBeDiscoveredFromCompiledClassesWhenWorkingElsewhere() throws Exception {
        Path project = createProject(directory.resolve("project"), "small SQL fixture\n");
        Path elsewhere = Files.createDirectories(directory.resolve("elsewhere"));
        Path classes = Files.createDirectories(project.resolve("target/classes"));
        List<Path> workingDirectories = new ArrayList<>();
        DatabaseStartup.CommandRunner runner = (command, cwd, timeout) -> {
            workingDirectories.add(cwd.toRealPath());
            return new DatabaseStartup.CommandResult(1, "Docker daemon unavailable", false);
        };
        DatabaseStartup.Result result = startup(runner, noProbe(),
                new FakeSettings(elsewhere, classes)).start(message -> { });
        assertFalse(result.ready());
        assertFalse(workingDirectories.isEmpty(), "Code-source project discovery should reach the Docker check.");
        assertTrue(workingDirectories.stream().allMatch(project.toRealPath()::equals));
    }

    @Test
    void timedOutPrerequisiteReturnsWithoutFetchingOrWaitingAgain() throws Exception {
        Path project = createProject(directory.resolve("project"), "small SQL fixture\n");
        DatabaseStartup.CommandRunner runner = (command, cwd, timeout) -> {
            assertTrue(timeout.compareTo(Duration.ZERO) > 0);
            assertTrue(timeout.compareTo(Duration.ofMinutes(1)) <= 0,
                    "Prerequisite commands should have a short bounded timeout.");
            return new DatabaseStartup.CommandResult(-1, "", true);
        };
        DatabaseStartup.Result result = assertTimeout(Duration.ofSeconds(2),
                () -> startup(runner, noProbe(), new FakeSettings(project, project)).start(message -> { }));
        assertFalse(result.ready());
    }

    @Test
    void interruptedCommandCancelsStartupAndRestoresTheInterruptFlag() throws Exception {
        Path project = createProject(directory.resolve("project"), "small SQL fixture\n");
        DatabaseStartup.CommandRunner runner = (command, cwd, timeout) -> {
            throw new InterruptedException("Test cancellation");
        };
        DatabaseStartup.Result result = startup(runner, noProbe(),
                new FakeSettings(project, project)).start(message -> { });
        assertFalse(result.ready());
        assertTrue(result.reason().toLowerCase().contains("cancel"), result.reason());
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void composeMustBeAvailableBeforeFetchingTheDump() throws Exception {
        Path project = createProject(directory.resolve("project"), "small SQL fixture\n");
        List<List<String>> commands = new ArrayList<>();
        DatabaseStartup.CommandRunner runner = (command, cwd, timeout) -> {
            commands.add(List.copyOf(command));
            assertEquals("docker", command.get(0));
            return new DatabaseStartup.CommandResult(command.get(1).equals("info") ? 0 : 1, "", false);
        };
        DatabaseStartup.Result result = startup(runner, noProbe(),
                new FakeSettings(project, project)).start(message -> { });
        assertFalse(result.ready());
        assertTrue(result.reason().toLowerCase().contains("compose"), result.reason());
        assertTrue(commands.stream().noneMatch(command -> command.contains("lfs")));
    }

    @Test
    void explicitProjectDirectorySupportsLaunchingOutsideTheCheckout() throws Exception {
        Path project = createProject(directory.resolve("project"), "small SQL fixture\n");
        Path elsewhere = Files.createDirectories(directory.resolve("elsewhere"));
        FakeSettings settings = new FakeSettings(elsewhere, elsewhere);
        settings.values.put("transitor.projectDir", project.toString());
        List<Path> observed = new ArrayList<>();
        DatabaseStartup.CommandRunner runner = (command, cwd, timeout) -> {
            observed.add(cwd);
            return new DatabaseStartup.CommandResult(1, "Docker unavailable", false);
        };
        startup(runner, noProbe(), settings).start(message -> { });
        assertFalse(observed.isEmpty());
        assertTrue(observed.stream().allMatch(project.toRealPath()::equals));
    }

    @Test
    void coldImportVerifiesThePointerThenWarmStartupReusesItsSecretsAndVolume() throws Exception {
        String sql = "CREATE TABLE sample (id INTEGER);\n";
        Path project = createProject(directory.resolve("project with spaces"), sql);
        DockerFixture docker = new DockerFixture(project, sql);
        FakeSettings settings = new FakeSettings(project, project.resolve("target/classes"));
        List<String> progress = new ArrayList<>();
        List<DatabaseConfig.ConnectionSettings> connections = new ArrayList<>();
        DatabaseStartup.DatabaseProbe probe = configuration -> {
            connections.add(configuration);
            return true;
        };

        DatabaseStartup.Result first = startup(docker, probe, settings).start(progress::add);
        assertTrue(first.ready(), first.reason());
        assertEquals(1, docker.lfsPulls);
        assertEquals(1, docker.composeStarts);
        Path secrets = project.resolve(".transitor/docker.env");
        String original = Files.readString(secrets);
        Map<String, String> bindings = readBindings(original);
        String password = bindings.get("TRANSITOR_DOCKER_PASSWORD");
        assertNotNull(password);
        assertTrue(password.matches("[0-9a-f]{64}"));
        assertTrue(bindings.get("TRANSITOR_DOCKER_ROOT_PASSWORD").matches("[0-9a-f]{64}"));
        assertNotEquals(password, bindings.get("TRANSITOR_DOCKER_ROOT_PASSWORD"));
        assertEquals(password, connections.get(0).password());
        assertEquals("transitor", connections.get(0).user());
        assertTrue(connections.get(0).url().startsWith("jdbc:mysql://127.0.0.1:49153/gtfs"));
        assertTrue(docker.commands.stream().flatMap(List::stream).noneMatch(argument -> argument.contains(password)));
        assertTrue(progress.stream().noneMatch(message -> message.contains(password)));

        // Simulate a new checkout where Git leaves a pointer but the Docker volume is intact.
        Files.writeString(docker.dump, pointerFor(sql));
        DatabaseConfig.useAutomatic(null);
        docker.volumeExists = true;
        docker.commands.clear();
        docker.lfsPulls = 0;
        docker.composeStarts = 0;
        DatabaseStartup.Result second = startup(docker, probe, settings).start(progress::add);
        assertTrue(second.ready(), second.reason());
        assertEquals(original, Files.readString(secrets), "Warm startup must not rotate persisted credentials.");
        assertEquals(0, docker.lfsPulls);
        assertEquals(1, docker.composeStarts);
        assertTrue(docker.commands.stream().noneMatch(command -> command.get(0).equals("git")));
        assertEquals(password, connections.get(1).password());
        for (List<String> command : docker.commands) {
            if (command.contains("--env-file")) {
                assertEquals(project.resolve(".transitor/docker.env").toString(),
                        command.get(command.indexOf("--env-file") + 1));
                assertEquals(project.resolve("compose.yaml").toString(),
                        command.get(command.indexOf("-f") + 1));
            }
        }
    }

    @Test
    void checksumMismatchStopsBeforeStartingAContainer() throws Exception {
        String sql = "CREATE TABLE sample (id INTEGER);\n";
        Path project = createProject(directory.resolve("project"), sql);
        DockerFixture docker = new DockerFixture(project, sql);
        docker.downloadedSql = "X" + sql.substring(1);
        DatabaseStartup.Result result = startup(docker, noProbe(),
                new FakeSettings(project, project)).start(message -> { });
        assertFalse(result.ready());
        assertTrue(result.reason().toLowerCase().contains("checksum")
                || result.reason().toLowerCase().contains("sha")
                || result.reason().toLowerCase().contains("integrity"), result.reason());
        assertEquals(1, docker.lfsPulls);
        assertEquals(0, docker.composeStarts);
    }

    @Test
    void existingVolumeWithoutItsCredentialFileIsPreserved() throws Exception {
        String sql = "CREATE TABLE sample (id INTEGER);\n";
        Path project = createProject(directory.resolve("project"), sql);
        DockerFixture docker = new DockerFixture(project, sql);
        docker.volumeExists = true;
        DatabaseStartup.Result result = startup(docker, noProbe(),
                new FakeSettings(project, project)).start(message -> { });
        assertFalse(result.ready());
        assertTrue(result.reason().toLowerCase().contains("credential")
                || result.reason().contains("docker.env"), result.reason());
        assertFalse(Files.exists(project.resolve(".transitor/docker.env")));
        assertEquals(0, docker.composeStarts);
        assertEquals(0, docker.lfsPulls);
    }

    @Test
    void incompleteExistingVolumeFetchesTheDumpOnlyAfterTheImporterRequestsIt() throws Exception {
        String sql = "CREATE TABLE sample (id INTEGER);\n";
        Path project = createProject(directory.resolve("project"), sql);
        DockerFixture docker = new DockerFixture(project, sql);
        FakeSettings settings = new FakeSettings(project, project);
        assertTrue(startup(docker, configuration -> true, settings).start(message -> { }).ready());
        Files.writeString(docker.dump, pointerFor(sql));
        DatabaseConfig.useAutomatic(null);
        docker.volumeExists = true;
        docker.lfsPulls = 0;
        docker.composeStarts = 0;
        docker.nextInspectResults.add("exited|42|");
        DatabaseStartup.Result result = startup(docker, configuration -> true, settings).start(message -> { });
        assertTrue(result.ready(), result.reason());
        assertEquals(1, docker.lfsPulls);
        assertEquals(2, docker.composeStarts);
    }

    @Test
    void anInvalidCredentialFileIsNotReplacedForAnExistingVolume() throws Exception {
        String sql = "CREATE TABLE sample (id INTEGER);\n";
        Path project = createProject(directory.resolve("project"), sql);
        DockerFixture docker = new DockerFixture(project, sql);
        FakeSettings settings = new FakeSettings(project, project);
        assertTrue(startup(docker, configuration -> true, settings).start(message -> { }).ready());
        Path secrets = project.resolve(".transitor/docker.env");
        String previous = Files.readString(secrets);
        String invalid = previous.replaceFirst("(?m)^TRANSITOR_DOCKER_PASSWORD=.*$", "TRANSITOR_DOCKER_PASSWORD=invalid");
        Files.writeString(secrets, invalid);
        docker.volumeExists = true;
        docker.composeStarts = 0;
        DatabaseConfig.useAutomatic(null);
        DatabaseStartup.Result result = startup(docker, noProbe(), settings).start(message -> { });
        assertFalse(result.ready());
        assertEquals(invalid, Files.readString(secrets));
        assertEquals(0, docker.composeStarts);
    }

    @Test
    void missingDumpCanBeFetchedUsingItsCommittedLfsPointer() throws Exception {
        String sql = "CREATE TABLE sample (id INTEGER);\n";
        Path project = createProject(directory.resolve("project"), sql);
        DockerFixture docker = new DockerFixture(project, sql);
        Files.delete(docker.dump);
        DatabaseStartup.Result result = startup(docker, configuration -> true,
                new FakeSettings(project, project)).start(message -> { });
        assertTrue(result.ready(), result.reason());
        assertEquals(1, docker.lfsPulls);
        assertEquals(sql, Files.readString(docker.dump));
        assertTrue(docker.commands.stream().anyMatch(command -> command.contains("show")));
    }

    @Test
    void waitingForImportCanBeCancelledWithoutWaitingForTheReadinessDeadline() throws Exception {
        String sql = "CREATE TABLE sample (id INTEGER);\n";
        Path project = createProject(directory.resolve("project"), sql);
        DockerFixture docker = new DockerFixture(project, sql);
        docker.defaultInspectResult = "running|0|starting";
        DatabaseStartup startup = new DatabaseStartup(docker, noProbe(),
                new FakeSettings(project, project), duration -> {
                    throw new InterruptedException("Test cancellation during import");
                });
        DatabaseStartup.Result result = startup.start(message -> { });
        assertFalse(result.ready());
        assertTrue(result.reason().toLowerCase().contains("cancel"), result.reason());
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void anImportThatNeverBecomesHealthyHasABoundedReadinessWait() throws Exception {
        String sql = "CREATE TABLE sample (id INTEGER);\n";
        Path project = createProject(directory.resolve("project"), sql);
        DockerFixture docker = new DockerFixture(project, sql);
        docker.defaultInspectResult = "running|0|starting";
        int[] waits = {0};
        DatabaseStartup startup = new DatabaseStartup(docker, noProbe(),
                new FakeSettings(project, project), duration -> {
                    assertTrue(duration.compareTo(Duration.ZERO) > 0);
                    assertTrue(++waits[0] <= 200, "Readiness polling must stop at its bounded retry limit.");
                });
        DatabaseStartup.Result result = assertTimeout(Duration.ofSeconds(2),
                () -> startup.start(message -> { }));
        assertFalse(result.ready());
        assertTrue(waits[0] > 0);
        assertTrue(result.reason().toLowerCase().contains("initializ")
                || result.reason().toLowerCase().contains("timed out"), result.reason());
    }

    private static Map<String, String> readBindings(String contents) {
        Map<String, String> bindings = new HashMap<>();
        for (String line : contents.split("\\R")) {
            if (line.isBlank() || line.startsWith("#")) continue;
            int equals = line.indexOf('=');
            bindings.put(line.substring(0, equals), line.substring(equals + 1));
        }
        return bindings;
    }

    private static final class DockerFixture implements DatabaseStartup.CommandRunner {
        private final Path dump;
        private final String expectedPointer;
        private final List<List<String>> commands = new ArrayList<>();
        private final List<String> nextInspectResults = new ArrayList<>();
        private String downloadedSql;
        private String defaultInspectResult = "running|0|healthy";
        private boolean volumeExists;
        private int lfsPulls;
        private int composeStarts;

        private DockerFixture(Path project, String sql) throws Exception {
            dump = project.resolve("data/gtfs/dump-gtfs-202406251610.sql");
            expectedPointer = pointerFor(sql);
            downloadedSql = sql;
        }

        @Override
        public DatabaseStartup.CommandResult run(List<String> command, Path cwd, Duration timeout) throws IOException {
            commands.add(List.copyOf(command));
            assertTrue(timeout.compareTo(Duration.ZERO) > 0, "Every external command needs a timeout.");
            if (command.get(0).equals("git")) {
                if (command.contains("show")) return successful(expectedPointer);
                if (command.contains("version")) return successful("git-lfs/3.6.1");
                if (command.contains("pull")) {
                    lfsPulls++;
                    Files.writeString(dump, downloadedSql);
                    return successful("");
                }
                fail("Unexpected Git command in startup fixture: " + command);
            }
            if (command.size() > 2 && command.get(1).equals("volume")) {
                return new DatabaseStartup.CommandResult(volumeExists ? 0 : 1, volumeExists ? command.get(command.size() - 1) : "Error response from daemon: no such volume", false);
            }
            if (command.get(1).equals("info")) return successful("27.5.1");
            if (command.get(1).equals("inspect")) {
                return successful(nextInspectResults.isEmpty() ? defaultInspectResult : nextInspectResults.remove(0));
            }
            if (command.get(1).equals("compose")) {
                if (command.contains("version")) return successful("2.32.4");
                if (command.contains("up")) {
                    composeStarts++;
                    return successful("");
                }
                if (command.contains("ps")) return successful("0123456789abcdef0123456789abcdef");
                if (command.contains("port")) return successful("127.0.0.1:49153");
            }
            fail("Unexpected external command in startup fixture: " + command);
            return null;
        }

        private static DatabaseStartup.CommandResult successful(String output) {
            return new DatabaseStartup.CommandResult(0, output, false);
        }
    }

    private static DatabaseStartup startup(DatabaseStartup.CommandRunner runner,
                                           DatabaseStartup.DatabaseProbe probe,
                                           DatabaseStartup.Settings settings) {
        return new DatabaseStartup(runner, probe, settings, duration -> {
            fail("This test should not wait for a database.");
        });
    }

    private static DatabaseStartup.CommandRunner noCommands() {
        return (command, cwd, timeout) -> {
            fail("Unexpected external command: " + command.get(0));
            return null;
        };
    }

    private static DatabaseStartup.DatabaseProbe noProbe() {
        return settings -> {
            fail("Unexpected database connection attempt.");
            return false;
        };
    }

    private static Path createProject(Path root, String sql) throws Exception {
        Files.createDirectories(root.resolve("data/gtfs"));
        Files.writeString(root.resolve("pom.xml"), "<project/>\n");
        Files.writeString(root.resolve("compose.yaml"), "services:\n  db:\n    image: mariadb:11.4\n");
        Files.writeString(root.resolve("data/gtfs/dump-gtfs-202406251610.sql"), pointerFor(sql));
        return root;
    }

    private static String pointerFor(String sql) throws Exception {
        byte[] bytes = sql.getBytes(StandardCharsets.UTF_8);
        String sha256 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        return "version https://git-lfs.github.com/spec/v1\noid sha256:" + sha256 + "\nsize " + bytes.length + "\n";
    }

    private static final class FakeSettings implements DatabaseStartup.Settings {
        private final Map<String, String> values = new HashMap<>();
        private final Path workingDirectory;
        private final Path codeLocation;

        private FakeSettings(Path workingDirectory, Path codeLocation) {
            this.workingDirectory = workingDirectory;
            this.codeLocation = codeLocation;
        }

        @Override
        public String get(String property, String variable) {
            return values.containsKey(property) ? values.get(property) : values.get(variable);
        }

        @Override
        public Path workingDirectory() {
            return workingDirectory;
        }

        @Override
        public Path codeLocation() {
            return codeLocation;
        }
    }
}
