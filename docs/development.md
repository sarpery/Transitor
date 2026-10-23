# Development

## Build and run

Install JDK 17 or newer, then run from the repository root:

```sh
./mvnw verify
./mvnw javafx:run
```

The Maven wrapper pins Maven 3.9.11 and verifies the downloaded distribution's checksum. Dependencies come from Maven Central. JavaFX 21 is supplied by Maven, so a separate JavaFX SDK is unnecessary. Unix wrapper installation needs `unzip`; Windows uses `mvnw.cmd`.

`verify` compiles the application and runs the JUnit tests. The tests do not need a database, map tiles, or a graphical desktop. Reports are written to `target/surefire-reports/`; the application JAR is `target/transitor-1.0-SNAPSHOT.jar`. Use the Maven JavaFX launch command: the JAR is not a standalone installer or a dependency-bundled executable.

Bundled datasets load from the classpath, including when the application is launched outside the repository directory. Generated output, local environment files, and personal IDE settings are ignored by Git.

## Using the interface

Enter a starting postcode and a target postcode, then choose **Walk** or **Bike**. For example, use `6211AB` and `6211AS`. Distance and estimated travel time appear below the routing controls. **Heatmap** opens the accessibility view.

For a configured transit database, select an hour and minute (or click **Current Time**) and choose **Transport**. The interface retains the original university project's layout and route-number images.

## Optional transit database

The interface supports walking, cycling, and accessibility exploration without a database. To enable public-transport journeys:

1. Install MySQL 8 or a compatible server. The supplied dump has also been tested with MariaDB 11.8.
2. Install Git LFS and fetch the historical dump:

   ```sh
   git lfs install
   git lfs pull --include="data/gtfs/dump-gtfs-202406251610.sql"
   ```

   The SQL payload is approximately **1.1 GB**. A small file beginning with `version https://git-lfs.github.com/spec/v1` is a pointer, not an importable database dump. Reserve several gigabytes for the imported database.

3. Create an empty database named `gtfs`, then import the dump into it:

   ```sh
   mysql -u YOUR_ADMIN_USER -p -e 'CREATE DATABASE gtfs;'
   mysql -u YOUR_ADMIN_USER -p gtfs < data/gtfs/dump-gtfs-202406251610.sql
   ```

   The dump includes table definitions and views and is intended for a fresh local database. Do not import it over an existing database containing work you need to keep. Grant a local application account read access to the imported database, and verify that it can query the dump's views (`points`, `single_bus_rides`, and `walking_ages`).

4. Copy `.env.example` to `.env`, enter your local database URL, username, and password, and export them before launching. The application reads process environment variables; it does not automatically read `.env`.

   ```sh
   cp .env.example .env
   # Edit .env with your local settings, then:
   set -a
   . ./.env
   set +a
   ./mvnw javafx:run
   ```

   Keep `.env` private. It is ignored by Git. On Windows, set the same variables in your shell or IDE run configuration.

| Variable | Purpose |
| --- | --- |
| `TRANSITOR_DB_URL` | JDBC URL, for example `jdbc:mysql://localhost:3306/gtfs` |
| `TRANSITOR_DB_USER` | Local application database account |
| `TRANSITOR_DB_PASSWORD` | Password for that account |

Equivalent JVM properties are `transitor.db.url`, `transitor.db.user`, and `transitor.db.password`. Leave database configuration unset to use the application without transit. Incomplete settings or an unavailable database should leave the other modes usable.

To clone only the lightweight application initially, use `GIT_LFS_SKIP_SMUDGE=1 git clone ...`; fetch the dump later when you want to enable transit.

## Data and network use

- `src/main/resources/data/MassZipLatLon.txt`: postcode coordinates.
- `src/main/resources/data/FilteredDistances.txt`: the original routing graph data.
- `src/main/resources/data/geojson/`: tourism, shop, and other amenity features.
- `data/gtfs/`: the original SQL dump tracked through Git LFS.

Map backgrounds are fetched from `tile.openstreetmap.org`. The original coordinate-lookup helper uses `computerscience.dacs.unimaas.nl`; routes between bundled postcodes do not depend on that endpoint. Corporate or restricted networks may need to allow these hosts. Never disable TLS verification to resolve a connection problem.

For a cloud machine without a desktop, run a virtual display such as Xvfb and set `DISPLAY` before launching JavaFX. This repository's CI runs the non-graphical test suite. GUI smoke checks require a display and are separate from those unit tests.
