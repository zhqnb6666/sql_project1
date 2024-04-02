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

            String jsonDirectoryPath = "resources/";
            long startTime, endTime ,totalTime = 0;
            startTime = System.currentTimeMillis();
            LinesTableLoader.clearLinesTable(conn);
            StationsTableLoader.clearStationsTable(conn);
            StationsTableLoader.clearBusesTable(conn);
            StationsTableLoader.clearOutsTable(conn);
            PassengersTableLoader.clearPassengersTable(conn);
            CardsTableLoader.clearCardsTable(conn);
            RidesTableLoader.clearRidesTable(conn);
            LineStationsTableLoader.clearLineStationsTable(conn);
            endTime = System.currentTimeMillis();
            System.out.println("Time for clear the tables" + (endTime - startTime) + "ms");

            // Insert data for each table from corresponding JSON files
            startTime = System.currentTimeMillis();
            LinesTableLoader.insertLines(conn, jsonDirectoryPath + "lines.json");
            endTime = System.currentTimeMillis();
            System.out.println("Time for insert lines" + (endTime - startTime) + "ms");

            totalTime+=endTime-startTime;
            startTime = System.currentTimeMillis();
            StationsTableLoader.insertStationsAndBusesAndOuts(conn, jsonDirectoryPath + "stations.json");
            endTime = System.currentTimeMillis();
            System.out.println("Time for insert stations" + (endTime - startTime) + "ms");

            totalTime+=endTime-startTime;
            startTime = System.currentTimeMillis();
            PassengersTableLoader.insertPassengers(conn, jsonDirectoryPath + "passenger.json");
            endTime = System.currentTimeMillis();
            System.out.println("Time for insert passengers" + (endTime - startTime) + "ms");

            totalTime+=endTime-startTime;
            startTime = System.currentTimeMillis();
            CardsTableLoader.insertCards(conn, jsonDirectoryPath + "cards.json");
            endTime = System.currentTimeMillis();
            System.out.println("Time for insert cards" + (endTime - startTime) + "ms");

            totalTime+=endTime-startTime;
            startTime = System.currentTimeMillis();
            RidesTableLoader.insertRides(conn, jsonDirectoryPath + "ride.json");
            endTime = System.currentTimeMillis();
            System.out.println("Time for insert rides" + (endTime - startTime) + "ms");

            totalTime+=endTime-startTime;
            startTime = System.currentTimeMillis();
            LineStationsTableLoader.insertLineStations(conn,jsonDirectoryPath+"lines.json",jsonDirectoryPath+"stations.json");
            endTime = System.currentTimeMillis();
            System.out.println("Time for insert line stations" + (endTime - startTime) + "ms");
            System.out.println("Total time for insert all tables" + totalTime + "ms");
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
