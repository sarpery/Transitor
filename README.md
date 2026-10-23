# Transitor

A JavaFX journey planner and amenity-accessibility explorer for Maastricht, built as a university group project. Compare walking, cycling, and scheduled public-transport journeys between postcodes, then explore the nearby amenities on an interactive map.

**My contribution:** I worked primarily on the JavaFX interface. The routing algorithms, transit data, and accessibility model are part of the team's combined work. Full team credits are below.

![Transitor's JavaFX interface showing a walking route in Maastricht](docs/images/transitor.png)

## Try it

You need **JDK 17 or newer**. The included Maven wrapper downloads the pinned build tools on first use; Unix systems also need `unzip`.

```sh
./mvnw verify
./mvnw javafx:run
```

On Windows, use `mvnw.cmd` instead. A graphical desktop is required to display the application.

Walking, cycling, and accessibility exploration use the bundled datasets and **do not require a database**. Enter **6211AB** as the starting postcode and **6211AS** as the target, then click **Walk**. Map tiles need an internet connection.

Public-transport routing is optional. It uses a local MySQL-compatible database populated with the included historical GTFS dump. See [development and database setup](docs/development.md).

## What it does

- Plan walking and cycling routes between Maastricht postcodes.
- Explore scheduled bus journeys, including walking connections and route identifiers.
- Display estimated travel distance and duration for a selected route.
- Visualize amenity-accessibility scores and a city-wide heatmap.
- Choose a departure time for public-transport journeys.

The original JavaFX interface uses an FXML layout with walking, cycling, transit, and heatmap controls. Separate map layers draw routes and accessibility results.

## Project layout

```text
src/main/java/com/example/
  Frontend/                  JavaFX views, controller, and map layers
  Controller/                Connections between the interface and routing logic
  Backend/                   Algorithms, graph models, resource and database access
src/main/resources/
  com/example/Frontend/      FXML and interface image assets
  data/                      Bundled postcode, graph, and amenity datasets
src/test/java/               Automated tests
data/gtfs/                  Optional database dump, stored with Git LFS
docs/                      Setup notes, architecture, and screenshots
```

Build output belongs in `target/` and is ignored by Git. Dependencies are declared in `pom.xml`; no JAR files need to be copied into the project manually.

See [architecture notes](docs/architecture.md) for how the interface, routing, and data layers fit together.

## Scope and limitations

This is an educational project, not a live travel service. The transit dump dates from **June 2024**; schedules may be out of date. Walking and cycling summary distances and times are estimates based on the original model, not turn-by-turn navigation or live traffic.

Accessibility scores summarize the project's weighted amenity model. They are useful for exploring that model, and are not a validated measure of a neighbourhood's social or economic conditions. Map backgrounds come from OpenStreetMap.

## Team

Originally developed by university project group 10:

- Ahmed Ahmed Adel Amin Yehia Zakaria Metwally
- Aleksandr Voloshin
- Karol Plandowski
- Kennedy Fernandes
- Khaled Ismail
- Sarper Yüksel
- Ty Barański

This repository retains the group's original routing and accessibility work, with subsequent improvements to portability, project organization, and developer setup.
