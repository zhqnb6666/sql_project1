import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class RidesTableLoader {

    public static void clearRidesTable(Connection con) {
        if (con != null) {
            try (Statement stmt = con.createStatement()) {
                // Drop the existing rides table if it exists
                stmt.executeUpdate("DROP TABLE IF EXISTS rides;");
                con.commit();

                // Create the rides table afresh
                stmt.executeUpdate("CREATE TABLE rides (\n" +
                        "ride_id SERIAL PRIMARY KEY,\n" +
                        "user_id VARCHAR(255) NOT NULL,\n" +
                        "start_station VARCHAR(255) NOT NULL,\n" +
                        "end_station VARCHAR(255) NOT NULL,\n" +
                        "price DECIMAL(5,2) NOT NULL,\n" +
                        "start_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,\n" +
                        "end_time TIMESTAMP WITHOUT TIME ZONE NOT NULL\n" +
                        ");");
                con.commit();
            } catch (SQLException ex) {
                System.err.println("Error clearing the rides table: " + ex.getMessage());
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back the transaction: " + rollbackEx.getMessage());
                }
                throw new RuntimeException("Failed to clear the rides table.", ex);
            }
        }
    }

//    public static void insertRides(Connection conn, String filePath) {
//        try {
//            String jsonStrings = Files.readString(Paths.get(filePath));
//            JSONObject jsonObject = JSON.parseObject(jsonStrings, Feature.OrderedField);
//
//            String insertSQL = "INSERT INTO rides (ride_id, user_id, start_station, end_station, price, start_time, end_time) VALUES (?, ?, ?, ?, ?, TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS'));";
//
//            PreparedStatement pstmt = conn.prepareStatement(insertSQL);
//
//            for (String rideId : jsonObject.keySet()) {
//                JSONObject ride = jsonObject.getJSONObject(rideId);
//
//                pstmt.setString(1, rideId);
//                pstmt.setString(2, ride.getString("user"));
//                pstmt.setString(3, ride.getString("start_station"));
//                pstmt.setString(4, ride.getString("end_station"));
//                pstmt.setDouble(5, ride.getDouble("price"));
//                pstmt.setString(6, ride.getString("start_time"));
//                pstmt.setString(7, ride.getString("end_time"));
//
//                pstmt.executeUpdate();
//            }
//        } catch (IOException | SQLException e) {
//            e.printStackTrace();
//        }
//    }

    public static void insertRides(Connection conn, String filePath) {
        try {
            String jsonStrings = Files.readString(Paths.get(filePath));
            JSONArray rides = JSON.parseArray(jsonStrings);

            String insertSQL = "INSERT INTO rides (user_id, start_station, end_station, price, start_time, end_time) VALUES (?, ?, ?, ?, TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS'));";

            PreparedStatement pstmt = conn.prepareStatement(insertSQL);

            for (JSONObject ride : rides.toJavaList(JSONObject.class)) {
                pstmt.setString(1, ride.getString("user"));
                pstmt.setString(2, ride.getString("start_station"));
                pstmt.setString(3, ride.getString("end_station"));
                pstmt.setBigDecimal(4, ride.getBigDecimal("price"));
                pstmt.setString(5, ride.getString("start_time"));
                pstmt.setString(6, ride.getString("end_time"));
                pstmt.executeUpdate();
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }


}
