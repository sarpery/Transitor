package com.example.Backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.Backend.Graph.AdjacencyListGraph;

/** Loads the optional GTFS graph without preventing offline features from starting. */
public class DatabaseToGraph {
    private static volatile AdjacencyListGraph<String> graph = new AdjacencyListGraph<>();
    private static volatile boolean available;
    private static volatile String unavailableReason = "Transit database is not configured.";

    public boolean graphTheDatabase() {
        synchronized (DatabaseToGraph.class) {
            if (available) {
                return true;
            }
            if (!DatabaseConfig.isConfigured()) {
                unavailableReason = "Transit database is not configured.";
                return false;
            }
            AdjacencyListGraph<String> loaded = new AdjacencyListGraph<>();
            try (Connection connection = DatabaseConfig.openConnection()) {
                try (PreparedStatement query = connection.prepareStatement("SELECT point_id FROM points");
                     ResultSet rows = query.executeQuery()) {
                    while (rows.next()) {
                        loaded.addVertex(rows.getString("point_id"));
                    }
                }
                if (loaded.getVertecesList().isEmpty()) {
                    unavailableReason = "Transit database contains no route data.";
                    return false;
                }
                try (PreparedStatement query = connection.prepareStatement("SELECT * FROM single_bus_rides");
                     ResultSet rows = query.executeQuery()) {
                    while (rows.next()) {
                        loaded.addEdge(rows.getString("start_stop"), rows.getString("end_stop"),
                                rows.getString("arrival_time"), rows.getString("departure_time"),
                                rows.getInt("distance"), rows.getString("trip_id"));
                    }
                }
                try (PreparedStatement query = connection.prepareStatement("SELECT * FROM walking_ages");
                     ResultSet rows = query.executeQuery()) {
                    while (rows.next()) {
                        String start = rows.getString("pt1");
                        String end = rows.getString("pt2");
                        String departure = rows.getDouble("pt1_lan") + ":" + rows.getDouble("pt1_lon");
                        String arrival = rows.getDouble("pt2_lan") + ":" + rows.getDouble("pt2_lon");
                        loaded.addEdge(start, end, arrival, departure, 0, "null:" + start + "-" + end);
                        loaded.addEdge(end, start, departure, arrival, 0, "null:" + end + "-" + start);
                    }
                }
                graph = loaded;
                available = true;
                unavailableReason = "";
                return true;
            } catch (SQLException exception) {
                // Connection strings and driver errors can contain private configuration.
                unavailableReason = "Transit database could not be loaded. Check the connection and GTFS import.";
                return false;
            }
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public String getUnavailableReason() {
        return unavailableReason;
    }

    public AdjacencyListGraph<String> getGraph() {
        return graph;
    }
}
