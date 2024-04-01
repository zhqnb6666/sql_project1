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

public class LinesTableLoader {
    public static void clearLinesTable(Connection con) {
        if (con != null) {
            try (Statement stmt0 = con.createStatement()) {
                stmt0.executeUpdate("DROP TABLE IF EXISTS lines CASCADE;");
                con.commit();
                stmt0.executeUpdate("CREATE TABLE lines (\n" +
                        "line_id SERIAL PRIMARY KEY,\n" +
                        "line_name VARCHAR(255) NOT NULL,\n" +
                        "start_time TIME NOT NULL,\n" +
                        "end_time TIME NOT NULL,\n" +
                        "intro TEXT,\n" +
                        "mileage DECIMAL(10, 2),\n" +
                        "color VARCHAR(50),\n" +
                        "first_opening DATE,\n" +
                        "url VARCHAR(255)\n" +
                        ");");
                con.commit();
            } catch (SQLException ex) {
                System.err.println("Error clearing the lines table: " + ex.getMessage());
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back the transaction: " + rollbackEx.getMessage());
                }
                throw new RuntimeException("Failed to clear the lines table.", ex);
            }
        }
    }


    public static void insertLines(Connection conn, String filePath) {
        try {
            String jsonStrings = Files.readString(Paths.get(filePath));
            JSONObject jsonObject = JSONObject.parseObject(jsonStrings, Feature.OrderedField);
            String insertSQL = "INSERT INTO lines (line_name, start_time, end_time, intro, mileage, color, first_opening, url) VALUES (?, TO_TIMESTAMP(?, 'HH24:MI'), TO_TIMESTAMP(?, 'HH24:MI'), ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?);";
            for (String lineName : jsonObject.keySet()) {
                JSONObject line = jsonObject.getJSONObject(lineName);
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setString(1, lineName); // Assuming lineName is the identifier
                pstmt.setString(2, line.getString("start_time"));
                pstmt.setString(3, line.getString("end_time"));
                pstmt.setString(4, line.getString("intro"));
                pstmt.setDouble(5, line.getDoubleValue("mileage"));
                pstmt.setString(6, line.getString("color"));
                pstmt.setString(7, line.getString("first_opening"));
                pstmt.setString(8, line.getString("url"));

                pstmt.executeUpdate();
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
