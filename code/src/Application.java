import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Application {
    private static Connection conn = null;
    private static Properties loadDBUser() {
        Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(new FileInputStream("resources/dbUser.properties")));
            return properties;
        } catch (IOException e) {
            System.err.println("can not find db user file");
            throw new RuntimeException(e);
        }
    }
    private static void openDB(Properties prop) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (Exception e) {
            System.err.println("Cannot find the Postgres driver. Check CLASSPATH.");
            System.exit(1);
        }
        String url = "jdbc:postgresql://" + prop.getProperty("host") + "/" + prop.getProperty("database");
        try {
            conn = DriverManager.getConnection(url, prop);
            if (conn != null) {
                System.out.println("Successfully connected to the database "
                        + prop.getProperty("database") + " as " + prop.getProperty("user"));
                conn.setAutoCommit(false);
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        try {
            // Establishing a database connection
            openDB(loadDBUser());
            conn.setAutoCommit(false); // Start transaction

            // Assuming JSON files are stored in a known directory
            String jsonDirectoryPath = "resources/";
            LinesTableLoader.clearLinesTable(conn);
            StationsTableLoader.clearStationsTable(conn);
            StationsTableLoader.clearBusesTable(conn);
            PassengersTableLoader.clearPassengersTable(conn);
            CardsTableLoader.clearCardsTable(conn);
            RidesTableLoader.clearRidesTable(conn);
            LineStationsTableLoader.clearLineStationsTable(conn);

            // Insert data for each table from corresponding JSON files
            LinesTableLoader.insertLines(conn, jsonDirectoryPath + "lines.json");
            StationsTableLoader.insertStationsAndBuses(conn, jsonDirectoryPath + "stations.json");
            PassengersTableLoader.insertPassengers(conn, jsonDirectoryPath + "passenger.json");
            CardsTableLoader.insertCards(conn, jsonDirectoryPath + "cards.json");
            RidesTableLoader.insertRides(conn, jsonDirectoryPath + "ride.json");
            LineStationsTableLoader.insertLineStations(conn,jsonDirectoryPath+"lines.json",jsonDirectoryPath+"stations.json");

            conn.commit(); // Commit transaction

            System.out.println("Data successfully loaded into the database.");

        } catch (SQLException e) {
            System.out.println("Database connection or data loading failed.");
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Roll back transaction in case of error
                } catch (SQLException ex) {
                    System.out.println("Failed to roll back the transaction.");
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close(); // Ensure connection is closed
                } catch (SQLException e) {
                    System.out.println("Failed to close the database connection.");
                    e.printStackTrace();
                }
            }
        }
    }
}
