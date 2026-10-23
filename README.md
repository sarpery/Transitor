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

Walking, cycling, and accessibility exploration use the bundled datasets and **do not require a database**. Try **6211AB → 6211AS**, or use the example action in the interface. Map tiles need an internet connection.

Public-transport routing is optional. It uses a local MySQL-compatible database populated with the included historical GTFS dump. See [development and database setup](docs/development.md).

## What it does

- Plan walking and cycling routes between Maastricht postcodes.
- Explore scheduled bus journeys, including walking connections and route identifiers.
- Compare estimated travel distance and duration in a readable journey summary.
- Visualize amenity-accessibility scores and a city-wide heatmap.
- Pan, zoom, and reset the map; switch travel modes without restarting the application.

The JavaFX interface separates its FXML layout from its CSS styling. Route and accessibility calculations run in background tasks, with progress feedback and recoverable validation errors.

## Project layout

```text
src/main/java/com/example/
  Frontend/                  JavaFX views, controller, and map layers
  Controller/                Connections between the interface and routing logic
  Backend/                   Algorithms, graph models, resource and database access
src/main/resources/
  com/example/Frontend/      FXML and stylesheet
  data/                      Bundled postcode, graph, and amenity datasets
src/test/java/               Automated tests
data/gtfs/                  Optional database dump, stored with Git LFS
docs/                      Setup notes, architecture, and screenshots
```

Build output belongs in `target/` and is ignored by Git. Dependencies are declared in `pom.xml`; no JAR files need to be copied into the project manually.

See [architecture notes](docs/architecture.md) for how the interface, routing, and data layers fit together.

## Scope and limitations

This is an educational project, not a live travel service. The transit dump dates from **June 2024**; schedules may be out of date. Walking and cycling summary distances and times are estimates based on the original model, not turn-by-turn navigation or live traffic.

Accessibility scores summarize the project's weighted amenity model. They are useful for exploring that model, and are not a validated measure of a neighbourhood's social or economic conditions. OpenStreetMap provides the map background; its attribution is shown in the interface.

## Team

Originally developed by university project group 10:

- Ahmed Ahmed Adel Amin Yehia Zakaria Metwally
- Aleksandr Voloshin
- Karol Plandowski
- Kennedy Fernandes
- Khaled Ismail
- Sarper Yüksel
- Ty Barański

This repository retains the group's original routing and accessibility work, with subsequent improvements to presentation, portability, project organization, and developer setup.
