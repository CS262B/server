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
@Path("/pathfinder/")
public class PathfinderWebService {
    private static String DB_URI = "jdbc:postgresql://localhost:5432/pathfinder";
    private static String DB_LOGIN_ID = "postgres";
    private static String DB_PASSWORD = "postgres";

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
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URI, DB_LOGIN_ID, DB_PASSWORD);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Building WHERE BuildingID=" + id);
            if (resultSet.next()) {
                result = "" + resultSet.getInt(1) + " " + resultSet.getString(2) + " " + resultSet.getString(3) + " " + resultSet.getFloat(4) + " " + resultSet.getFloat(5);
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
     * PUT method for creating an instance of Person with a given ID - If the
     * player already exists, replace them with the new player field values. We do this
     * because PUT is idempotent, meaning that running the same PUT several
     * times does not change the database.
     *
     * @param id         the ID for the new player, assumed to be unique
     * @param buildingLine a string representation of the player in the format: emailAddress name
     * @return status message
     */
    @PUT
    @Path("/building/{id}")
    @Consumes("text/plain")
    @Produces("text/plain")
    public String putBuilding(@PathParam("id") int id, String buildingLine) {
        String result;
        StringTokenizer st = new StringTokenizer(buildingLine);
        String name = st.nextToken(), URL = st.nextToken();
        double latitude = Double.parseDouble(st.nextToken()), longitude = Double.parseDouble(st.nextToken());
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URI, DB_LOGIN_ID, DB_PASSWORD);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM building WHERE BuildingID=" + id);
            if (resultSet.next()) {
                statement.executeUpdate("UPDATE building SET BuildingName='" + name + "' URL='" + URL + "' latitude=" + latitude + " longitude=" + longitude + " WHERE BuildingID=" + id);
                result = "Building " + id + " updated...";
            } else {
                statement.executeUpdate("INSERT INTO building VALUES (" + id + ", '" + name + "', '" + URL + "', " + latitude + ", " + longitude + ")");
                result = "Building " + id + " added...";
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
     * @param buildingLine a string representation of the player in the format: emailAddress name
     * @return status message
     */
    @POST
    @Path("/building")
    @Consumes("text/plain")
    @Produces("text/plain")
    public String postBuilding(String buildingLine) {
        String result;
        StringTokenizer st = new StringTokenizer(buildingLine);
        int id = -1;
        String emailAddress = st.nextToken(), name = st.nextToken();
        try {
            Class.forName("org.postgresql.Driver");
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
            result = "Building " + id + " added...";
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
    @Path("/building/{id}")
    @Produces("text/plain")
    public String deletePlayer(@PathParam("id") int id) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URI, DB_LOGIN_ID, DB_PASSWORD);
            Statement statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM Building WHERE BuildingID=" + id);
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
            System.out.println("Visit: http://localhost:9998/pathfinder");
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
