import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class StationsTableLoader {

    public static void clearStationsTable(Connection con) {
        if (con != null) {
            try (Statement stmt = con.createStatement()) {
                // Drop the existing stations table if it exists
                stmt.executeUpdate("DROP TABLE IF EXISTS stations CASCADE;");
                con.commit();

                // Create the stations table afresh
                stmt.executeUpdate("CREATE TABLE stations (\n" +
                        "station_id SERIAL PRIMARY KEY,\n" +
                        "station_english_name VARCHAR(255) NOT NULL,\n" +
                        "district VARCHAR(255),\n" +
                        "intro TEXT,\n" +
                        "chinese_name VARCHAR(255) NOT NULL\n" +
                        ");");
                con.commit();
            } catch (SQLException ex) {
                System.err.println("Error clearing the stations table: " + ex.getMessage());
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back the transaction: " + rollbackEx.getMessage());
                }
                throw new RuntimeException("Failed to clear the stations table.", ex);
            }
        }
    }

    public static void clearBusesTable(Connection con) throws SQLException {
        // Assuming CASCADE on stations table will automatically clear related bus entries.
        // If not, you should explicitly drop and recreate the buses table here.
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS buses CASCADE;");
            stmt.executeUpdate("CREATE TABLE buses (\n" +
                    "bus_id SERIAL PRIMARY KEY,\n" +
                    "station_id INTEGER NOT NULL REFERENCES stations(station_id),\n" +
                    "bus_name VARCHAR(255),\n" +
                    "bus_info TEXT,\n" +
                    "chukou VARCHAR(255)\n" +
                    ");");
        }
    }

    public static void insertStationsAndBuses(Connection conn, String filePath) {
        try {
            String jsonStrings = Files.readString(Paths.get(filePath));
            JSONObject jsonObject = JSON.parseObject(jsonStrings, Feature.OrderedField);

            String stationInsertSQL = "INSERT INTO stations (station_english_name, district, intro, chinese_name) VALUES (?, ?, ?, ?) RETURNING station_id;";
            String busInsertSQL = "INSERT INTO buses (station_id, bus_name, bus_info, chukou) VALUES (?, ?, ?, ?);";

            for (String stationName : jsonObject.keySet()) {
                JSONObject station = jsonObject.getJSONObject(stationName);
                PreparedStatement stationPstmt = conn.prepareStatement(stationInsertSQL, Statement.RETURN_GENERATED_KEYS);

                stationPstmt.setString(1, stationName);
                stationPstmt.setString(2, station.getString("district"));
                stationPstmt.setString(3, station.getString("intro"));
                stationPstmt.setString(4, station.getString("chinese_name"));

                int affectedRows = stationPstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating station failed, no rows affected.");
                }

                try (ResultSet generatedKeys = stationPstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long stationId = generatedKeys.getLong(1);

                        JSONArray busInfos = station.getJSONArray("bus_info");
                        for (int i = 0; i < busInfos.size(); i++) {
                            JSONObject busInfo = busInfos.getJSONObject(i);
                            JSONArray busOutInfos = busInfo.getJSONArray("busOutInfo");

                            for (int j = 0; j < busOutInfos.size(); j++) {
                                JSONObject busOutInfo = busOutInfos.getJSONObject(j);
                                PreparedStatement busPstmt = conn.prepareStatement(busInsertSQL);
                                busPstmt.setLong(1, stationId);
                                busPstmt.setString(2, busOutInfo.getString("busName"));
                                busPstmt.setString(3, busOutInfo.getString("busInfo"));
                                busPstmt.setString(4, busInfo.getString("chukou"));
                                busPstmt.executeUpdate();
                            }
                        }
                    } else {
                        throw new SQLException("Creating station failed, no ID obtained.");
                    }
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

//    public static void insertStations(Connection conn, String filePath) {
//        try {
//            // Reads the entire JSON file content as a String
//            String jsonStrings = Files.readString(Paths.get(filePath));
//            // Parses the String into a JSONObject with ordered fields
//            JSONObject jsonObject = JSONObject.parseObject(jsonStrings, Feature.OrderedField);
//
//            // Prepare the SQL statement for inserting station data
//            String insertSQL = "INSERT INTO stations (station_english_name, district, bus_info, out_info, intro, chinese_name) VALUES (?, ?, ?, ?, ?, ?);";
//
//            PreparedStatement pstmt = conn.prepareStatement(insertSQL);
//
//            // Iterate through each key (station name) in the JSONObject
//            for (String stationName : jsonObject.keySet()) {
//                JSONObject station = jsonObject.getJSONObject(stationName);
//
//                pstmt.setString(1, stationName);
//                pstmt.setString(2, station.getString("district"));
//                pstmt.setString(3, station.getString("bus_info")); // Consider JSON array or object to text conversion if needed
//                pstmt.setString(4, station.getString("out_info")); // Consider JSON array or object to text conversion if needed
//                pstmt.setString(5, station.getString("intro"));
//                pstmt.setString(6, station.getString("chinese_name"));
//
//                // Execute the update
//                pstmt.executeUpdate();
//            }
//        } catch (IOException | SQLException e) {
//            e.printStackTrace();
//        }
//    }
}
