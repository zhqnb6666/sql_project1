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

public class CardsTableLoader {
    public static void clearCardsTable(Connection con) {
        if (con != null) {
            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate("DROP TABLE IF EXISTS cards;");
                con.commit();

                stmt.executeUpdate("CREATE TABLE cards (\n" +
                        "card_id SERIAL PRIMARY KEY,\n" +
                        "code VARCHAR(255) UNIQUE NOT NULL,\n" +
                        "money DECIMAL(10,2),\n" +
                        "create_time DATE\n" +
                        ");");
                con.commit();
            } catch (SQLException ex) {
                System.err.println("Error clearing the cards table: " + ex.getMessage());
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back the transaction: " + rollbackEx.getMessage());
                }
                throw new RuntimeException("Failed to clear the cards table.", ex);
            }
        }
    }

//    public static void insertCards(Connection conn, String filePath) {
//        try {
//            String jsonStrings = Files.readString(Paths.get(filePath));
//            JSONObject jsonObject = JSON.parseObject(jsonStrings, Feature.OrderedField);
//
//            String insertSQL = "INSERT INTO cards (code, money, create_time) VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'));";
//
//            PreparedStatement pstmt = conn.prepareStatement(insertSQL);
//
//            for (String code : jsonObject.keySet()) {
//                JSONObject card = jsonObject.getJSONObject(code);
//
//                pstmt.setString(1, code);
//                pstmt.setBigDecimal(2, card.getBigDecimal("money"));
//                pstmt.setString(3, card.getString("create_time"));
//
//                pstmt.executeUpdate();
//            }
//        } catch (IOException | SQLException e) {
//            e.printStackTrace();
//        }
//    }
public static void insertCards(Connection conn, String filePath) {
    try {
        String jsonStrings = Files.readString(Paths.get(filePath));
        JSONArray cards = JSON.parseArray(jsonStrings);

        String insertSQL = "INSERT INTO cards (code, money, create_time) VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'));";

        PreparedStatement pstmt = conn.prepareStatement(insertSQL);

        for (JSONObject card : cards.toJavaList(JSONObject.class)) {
            pstmt.setString(1, card.getString("code"));
            pstmt.setBigDecimal(2, card.getBigDecimal("money"));
            pstmt.setString(3, card.getString("create_time")); // Assuming the date is in 'YYYY-MM-DD' format

            pstmt.executeUpdate();
        }
    } catch (IOException | SQLException e) {
        e.printStackTrace();
    }
}



}
