import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class PassengersTableLoader {
    public static void clearPassengersTable(Connection con) {
        if (con != null) {
            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate("DROP TABLE IF EXISTS passengers;");
                con.commit();

                stmt.executeUpdate("CREATE TABLE passengers (\n" +
                        "passenger_id SERIAL PRIMARY KEY,\n" +
                        "name VARCHAR(255) NOT NULL,\n" +
                        "id_number VARCHAR(255) UNIQUE NOT NULL,\n" +
                        "phone_number VARCHAR(255),\n" +
                        "gender VARCHAR(10),\n" +
                        "district VARCHAR(255)\n" +
                        ");");
                con.commit();
            } catch (SQLException ex) {
                System.err.println("Error clearing the passengers table: " + ex.getMessage());
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back the transaction: " + rollbackEx.getMessage());
                }
                throw new RuntimeException("Failed to clear the passengers table.", ex);
            }
        }
    }


    public static void insertPassengers(Connection conn, String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONArray passengers = JSON.parseArray(content);

            String insertSQL = "INSERT INTO passengers (name, id_number, phone_number, gender, district) VALUES (?, ?, ?, ?, ?);";

            PreparedStatement pstmt = conn.prepareStatement(insertSQL);

            for (JSONObject passenger : passengers.toJavaList(JSONObject.class)) {
                pstmt.setString(1, passenger.getString("name"));
                pstmt.setString(2, passenger.getString("id_number"));
                pstmt.setString(3, passenger.getString("phone_number"));
                pstmt.setString(4, passenger.getString("gender"));
                pstmt.setString(5, passenger.getString("district"));

                pstmt.executeUpdate();
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
