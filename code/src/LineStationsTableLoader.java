import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class LineStationsTableLoader {

    public static void clearLineStationsTable(Connection con) {
        if (con != null) {
            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate("DROP TABLE IF EXISTS line_stations;");
                con.commit();
                stmt.executeUpdate("CREATE TABLE line_stations (\n" +
                        "line_station_id SERIAL PRIMARY KEY,\n" +
                        "line_id INTEGER NOT NULL,\n" +
                        "station_id INTEGER NOT NULL,\n" +
                        "FOREIGN KEY (line_id) REFERENCES lines(line_id),\n" +
                        "FOREIGN KEY (station_id) REFERENCES stations(station_id)\n" +
                        ");");
                con.commit();
            } catch (SQLException ex) {
                System.err.println("Error clearing the line_stations table: " + ex.getMessage());
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back the transaction: " + rollbackEx.getMessage());
                }
                throw new RuntimeException("Failed to clear the line_stations table.", ex);
            }
        }
    }

    public static void insertLineStations(Connection conn, String lineFilePath, String stationFilePath) {
        try {
            // Parse the lines JSON file.
            String linesJsonString = Files.readString(Paths.get(lineFilePath));
            JSONObject linesJsonObject = JSON.parseObject(linesJsonString, Feature.OrderedField);

            // Parse the stations JSON file.
            String stationsJsonString = Files.readString(Paths.get(stationFilePath));
            JSONObject stationsJsonObject = JSON.parseObject(stationsJsonString, Feature.OrderedField);


            // SQL to find station ID based on the English name.
            String stationIdQuery = "SELECT station_id FROM stations WHERE station_english_name = ?;";

            // SQL to find line ID based on the line name.
            String lineIdQuery = "SELECT line_id FROM lines WHERE line_name = ?;";

            // SQL to insert into line_stations table.
            String insertSQL = "INSERT INTO line_stations (line_id, station_id) VALUES (?, ?);";

            for (String lineName : linesJsonObject.keySet()) {
                JSONObject line = linesJsonObject.getJSONObject(lineName);
                JSONArray stationNames = line.getJSONArray("stations");

                // Get line ID.
                PreparedStatement lineStmt = conn.prepareStatement(lineIdQuery);
                lineStmt.setString(1, lineName);
                ResultSet lineRs = lineStmt.executeQuery();
                if (!lineRs.next()) continue; // Skip if line ID not found
                int lineId = lineRs.getInt("line_id");

                for (Object stationNameObj : stationNames) {
                    String stationName = (String) stationNameObj;
                    if (!stationsJsonObject.containsKey(stationName)) continue; // Skip if station not found

                    // Get station ID.
                    PreparedStatement stationStmt = conn.prepareStatement(stationIdQuery);
                    stationStmt.setString(1, stationName);
                    ResultSet stationRs = stationStmt.executeQuery();
                    if (!stationRs.next()) continue; // Skip if station ID not found
                    int stationId = stationRs.getInt("station_id");

                    // Insert into line_stations.
                    PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
                    insertStmt.setInt(1, lineId);
                    insertStmt.setInt(2, stationId);
                    insertStmt.executeUpdate();
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }


}
