# Development

## Build and run

Install JDK 17 or newer, then run from the repository root:

```sh
./mvnw verify
./mvnw javafx:run
```

The Maven wrapper pins Maven 3.9.11 and verifies the downloaded distribution's checksum. Dependencies come from Maven Central. JavaFX 21 is supplied by Maven, so a separate JavaFX SDK is unnecessary. Unix wrapper installation needs `unzip`; Windows uses `mvnw.cmd`.

In IntelliJ IDEA, open `pom.xml` as a Maven project, select a JDK 17-or-newer project SDK, and let Maven resolve the dependencies. Run `com.example.Frontend.UI` or the Maven `javafx:run` goal. The default database setup needs no environment variables in the run configuration.

`verify` compiles the application and runs the JUnit tests. The tests do not need Docker, a database, map tiles, or a graphical desktop. Reports are written to `target/surefire-reports/`; the application JAR is `target/transitor-1.0-SNAPSHOT.jar`. Use the Maven JavaFX launch command: the JAR is not a standalone installer or a dependency-bundled executable.

Bundled datasets load from the classpath, including when the application is launched outside the repository directory. Generated output, local settings, and personal IDE settings are ignored by Git.

## Automatic transit database

The application prepares a local database when it launches with no explicit database settings:

1. Install a current version of Git with Git LFS. If Git LFS is not initialized, run `git lfs install`.
2. Install and start Docker Desktop, using Linux containers. On Linux, Docker Engine and the Compose v2 plugin are equivalent prerequisites. `docker compose version` should work in the environment that starts the application.
3. Launch the JavaFX application. It starts the `db` service from `compose.yaml`, using MariaDB 11.8.6. If the GTFS file is still a Git LFS pointer, it downloads the payload before importing it.

The first download is approximately **1.1 GB**, and the import can take several minutes. Reserve several gigabytes for the image, dump, and database. The interface opens while setup runs: walking, cycling, and accessibility use bundled data, and **Transport** becomes available after the database and transit graph are ready. Check the Transport tooltip and application console for progress or a setup error. If Docker or Git LFS is unavailable, the bundled-data features remain usable.

The database listens on a dynamically assigned **loopback-only port**, avoiding a dependency on port 3306 being free. Transitor generates local credentials in the ignored `.transitor/docker.env` file. It also stores a Compose project name there, based on the checkout's path, so different checkouts have separate database services. Keep this generated file private; it is not needed in Git.

The imported database lives in a persistent Docker volume. Subsequent launches reuse it. After correcting a failed download or import, launch the application again to retry setup. Closing the application leaves the database container available for the next launch.

From the repository root, stop the container after closing the application with:

```sh
docker compose --env-file .transitor/docker.env stop db
```

This keeps the imported data. Starting the JavaFX application again starts the service. To inspect database logs:

```sh
docker compose --env-file .transitor/docker.env logs db
```

The generated environment file exists after automatic setup has prepared the local service configuration; it is not a file to create by hand.

### Project location and opting out

Automatic setup looks for the repository from the working directory and the application's class location. A normal Maven or IntelliJ launch uses the checkout automatically. If launching from a custom location, set `TRANSITOR_PROJECT_DIR` to the checkout root containing `pom.xml`, `compose.yaml`, and `data/gtfs`. The equivalent JVM property is `transitor.projectDir`.

To leave transit off without attempting Docker startup, set `TRANSITOR_DB_AUTO_START=false` in the process environment or `-Dtransitor.db.autoStart=false` in the application's JVM options. Walking, cycling, and accessibility remain available. This opt-out controls automatic provisioning; explicit settings for an existing database still take precedence.

## Using the interface

Enter a starting postcode and a target postcode, then choose **Walk** or **Bike**. For example, use `6211AB` and `6211AS`. Distance and estimated travel time appear below the routing controls. **Heatmap** opens the accessibility view.

When transit is ready, select an hour and minute (or click **Current Time**) and choose **Transport**. The interface retains the original university project's layout and route-number images.

## Using an existing database

Use explicit connection settings if you already manage a MySQL-compatible server. Setting any manual database option selects this mode; incomplete settings produce a configuration error instead of silently starting Docker. Automatic credentials are not used for a manually configured server.

1. Install MySQL 8 or a compatible server. The supplied dump has also been imported with MariaDB 11.8.
2. Fetch the historical dump if needed:

   ```sh
   git lfs install
   git lfs pull --include="data/gtfs/dump-gtfs-202406251610.sql"
   ```

   A small file beginning with `version https://git-lfs.github.com/spec/v1` is a pointer, not an importable database dump.

3. Create an empty database named `gtfs`, then import the dump into it:

   ```sh
   mysql -u YOUR_ADMIN_USER -p -e 'CREATE DATABASE gtfs;'
   mysql -u YOUR_ADMIN_USER -p gtfs < data/gtfs/dump-gtfs-202406251610.sql
   ```

   The dump includes table definitions and views and is intended for a fresh local database. Do not import it over an existing database containing work you need to keep. Grant a local application account read access to the imported database, and verify that it can query the dump's views (`points`, `single_bus_rides`, and `walking_ages`).

4. Set all three connection variables before launching. For Bash or zsh, copy `.env.example` to `.env`, uncomment and fill in the three manual database settings, then export them:

   ```sh
   cp .env.example .env
   # Edit .env with your local settings, then:
   set -a
   . ./.env
   set +a
   ./mvnw javafx:run
   ```

   The application reads process environment variables; it does not automatically read `.env`. Keep `.env` private. On Windows, set the same variables in your shell or IntelliJ run configuration. Quote values containing shell characters when editing a shell-loaded `.env` file.

| Variable | Purpose |
| --- | --- |
| `TRANSITOR_DB_URL` | JDBC URL, for example `jdbc:mysql://localhost:3306/gtfs` |
| `TRANSITOR_DB_USER` | Local application database account |
| `TRANSITOR_DB_PASSWORD` | Password for that account |

Equivalent JVM properties are `transitor.db.url`, `transitor.db.user`, and `transitor.db.password`. These explicit connection settings override automatic Docker provisioning. To return to the default automatic setup, remove the manual variables and JVM properties from the launch environment.

## Data and network use

- `src/main/resources/data/MassZipLatLon.txt`: postcode coordinates.
- `src/main/resources/data/FilteredDistances.txt`: the original routing graph data.
- `src/main/resources/data/geojson/`: tourism, shop, and other amenity features.
- `data/gtfs/`: the original SQL dump tracked through Git LFS.
- `.transitor/`: ignored settings generated for the local Docker database.

The first automatic transit setup needs access to GitHub/Git LFS and the container registry for the MariaDB image. Map backgrounds are fetched from `tile.openstreetmap.org`. The original coordinate-lookup helper uses `computerscience.dacs.unimaas.nl`; routes between bundled postcodes do not depend on that endpoint. Corporate or restricted networks may need to allow the destinations used by these services. Never disable TLS verification to resolve a connection problem.

To avoid downloading the SQL dump during the initial clone, use `GIT_LFS_SKIP_SMUDGE=1 git clone ...`; automatic startup will fetch it when preparing transit. Set the automatic-start opt-out as well if you intend to use only the bundled-data features.

For a cloud machine without a desktop, run a virtual display such as Xvfb and set `DISPLAY` before launching JavaFX. This repository's CI runs the non-graphical test suite. GUI smoke checks require a display and are separate from those unit tests.

CI also runs `bash docker/test-import-guards.sh` against Docker to check missing or invalid dumps and preservation of existing data. This check uses a temporary database and removes only its own containers and volume. It does not download the GTFS dump. A full first-run import and a subsequent transit journey should also be checked after changes to database startup.
