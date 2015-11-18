package example;
import com.sun.net.httpserver.HttpServer;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.StringTokenizer;

import javax.ws.rs.*;

/**
 * Created by drd26 on 11/18/2015.
 */
// The Java class will be hosted at the URI path "/pathfinder"
@Path("/monopoly/")
public class HelloWorld {
    // The Java method will process HTTP GET requests
    @GET
    // The Java method will produce content identified by the MIME Media type "text/plain"
    @Produces("text/plain")
    public String getClichedMessage() {
        // Return some cliched textual content
        return "Hello, World!";
    }

    private static String DB_URI = "jdbc:sqlserver://THOMPSON:5432";
    private static String DB_LOGIN_ID = "CALVINAD\\drd26";
    private static String DB_PASSWORD = "";

    /**
     * @param id a player id in the monopoly database
     * @return a string version of the player record, if any, with the given id
     */
    @GET
    @Path("/building/{id}")
    @Produces("text/plain")
    public String getBuilding(@PathParam("id") int id) {
        String result;
        try {
//            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URI, DB_LOGIN_ID, DB_PASSWORD);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Building WHERE id=" + id);
            if (resultSet.next()) {
                result = "" + resultSet.getString(1) + " " + resultSet.getString(2) + " " + resultSet.getFloat((3) + " " + resultSet.getFloat(4));
                //result = resultSet.getInt(1) + " " + resultSet.getString(3) + " " + resultSet.getString(2);
            } else {
                result = "nothing found...";
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    /**
     * @return a string representation of the player records in the Player table
     */
    @GET
    @Path("/players")
    @Produces("text/plain")
    public String getPlayers() {
        String result = "";
        try {
            //Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URI, DB_LOGIN_ID, DB_PASSWORD);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Player");
            while (resultSet.next()) {
                result += resultSet.getInt(1) + " " + resultSet.getString(3) + " " + resultSet.getString(2) + "\n";
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    /**
     * PUT method for creating an instance of Person with a given ID - If the
     * player already exists, replace them with the new player field values. We do this
     * because PUT is idempotent, meaning that running the same PUT several
     * times does not change the database.
     *
     * @param id         the ID for the new player, assumed to be unique
     * @param playerLine a string representation of the player in the format: emailAddress name
     * @return status message
     */
    @PUT
    @Path("/player/{id}")
    @Consumes("text/plain")
    @Produces("text/plain")
    public String putPlayer(@PathParam("id") int id, String playerLine) {
        String result;
        StringTokenizer st = new StringTokenizer(playerLine);
        String emailAddress = st.nextToken(), name = st.nextToken();
        try {
            //Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URI, DB_LOGIN_ID, DB_PASSWORD);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Player WHERE id=" + id);
            if (resultSet.next()) {
                statement.executeUpdate("UPDATE Player SET emailaddress='" + emailAddress + "' name='" + name + "' WHERE id=" + id);
                result = "Player " + id + " updated...";
            } else {
                statement.executeUpdate("INSERT INTO Player VALUES (" + id + ", '" + emailAddress + "', '" + name + "')");
                result = "Player " + id + " added...";
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    /**
     * POST method for creating an instance of Person with a new, unique ID
     * number. We do this because POST is not idempotent, meaning that running
     * the same POST several times creates multiple objects with unique IDs but
     * with the same values.
     * <p/>
     * The method creates a new, unique ID by querying the player table for the
     * largest ID and adding 1 to that. Using a sequence would be a better solution.
     *
     * @param playerLine a string representation of the player in the format: emailAddress name
     * @return status message
     */
    @POST
    @Path("/player")
    @Consumes("text/plain")
    @Produces("text/plain")
    public String postPlayer(String playerLine) {
        String result;
        StringTokenizer st = new StringTokenizer(playerLine);
        int id = -1;
        String emailAddress = st.nextToken(), name = st.nextToken();
        try {
            //Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URI, DB_LOGIN_ID, DB_PASSWORD);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT MAX(ID) FROM Player");
            if (resultSet.next()) {
                id = resultSet.getInt(1) + 1;
            }
            statement.executeUpdate("INSERT INTO Player VALUES (" + id + ", '" + emailAddress + "', '" + name + "')");
            resultSet.close();
            statement.close();
            connection.close();
            result = "Player " + id + " added...";
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    /**
     * DELETE method for deleting and instance of player with the given ID. If
     * the player doesn't exist, then don't delete anything. DELETE is idempotent, so
     * sending the same command multiple times should result in the same side
     * effect, though the return value may be different.
     *
     * @param id the ID of the player to be returned
     * @return a simple text confirmation message
     */
    @DELETE
    @Path("/player/{id}")
    @Produces("text/plain")
    public String deletePlayer(@PathParam("id") int id) {
        try {
            //Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URI, DB_LOGIN_ID, DB_PASSWORD);
            Statement statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM Player WHERE id=" + id);
            statement.close();
            connection.close();
        } catch (Exception e) {
            return e.getMessage();
        }
        return "Player " + id + " deleted...";
    }

    public static void main(String[] args) throws IOException {
        HttpServer server;
        try {
            server = HttpServerFactory.create("http://localhost:9998/");
            server.start();
            System.out.println("Server running");
            System.out.println("Visit: http://localhost:9998/Pathfinder");
            System.out.println("Hit return to stop...");
            System.in.read();
            System.out.println("Stopping server");
            server.stop(0);
            System.out.println("Server stopped");

        } catch(ArrayIndexOutOfBoundsException e){
            System.out.println("Caught ArrayIndexOutOfBoundsException!");
            System.out.println("Exception message: " + e.getMessage());
        }

    }
}
