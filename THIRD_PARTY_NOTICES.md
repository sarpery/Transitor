# Licensing and third-party notices

## Original project work

Transitor was developed by the university project group credited in [README.md](README.md#team). The repository does not contain an agreement establishing permission to license all contributors' work or resolving any university ownership requirements. A license for the team's original code remains pending that confirmation.

Any future license for original code must distinguish it from the dependencies, datasets, images, and other third-party material below.

## Identified third-party terms

| Component | Evidence and terms |
| --- | --- |
| Gluon Maps 2.0.0-ea+6 | The dependency's `META-INF/LICENSE` contains GNU GPL version 3. See the [upstream project and licensing information](https://github.com/gluonhq/maps). A permissive license for Transitor's original code would not remove GPL obligations for distributions incorporating this library. |
| MySQL Connector/J 8.0.33 | Its published Maven POM identifies GPL version 2 with the Universal FOSS Exception. See [Connector/J licensing](https://dev.mysql.com/doc/connector-j/en/connector-j-licensing.html). |
| Maven wrapper scripts | `mvnw` and `mvnw.cmd` retain their Apache License 2.0 headers. |
| OpenStreetMap GeoJSON data | Each file in `src/main/resources/data/geojson/` states that its OpenStreetMap data is available under the Open Database License. Attribution: © OpenStreetMap contributors. See [OpenStreetMap copyright and licensing](https://www.openstreetmap.org/copyright). |
| OpenStreetMap map background | The application and its screenshots display OpenStreetMap cartography. Attribution: © OpenStreetMap contributors. The map material retains its own terms; see the [OpenStreetMap copyright page](https://www.openstreetmap.org/copyright). |

These notes identify terms verified during the presentation update; they are not an exhaustive inventory of every transitive dependency. Dependencies retain their respective licenses.

## Provenance still to confirm

The repository does not establish redistribution terms for the historical GTFS SQL dump, the postcode and distance datasets, or the crest and bus-number PNG assets. No new permission for those materials is granted by these notices. Confirm their original sources and terms before including them in a broader licensing statement.
