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
            ResultSet resultSet = statement.executeQuery("SELECT latitude, longitude FROM Building WHERE BuildingID=" + id);
            if (resultSet.next()) {
                result ="" + resultSet.getFloat(1) + " " + resultSet.getFloat(2);
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
     * PUT method for creating an instance of Building with a given ID - If the
     * building already exists, replace it with the new building field values. We do this
     * because PUT is idempotent, meaning that running the same PUT several
     * times does not change the database.
     *
     * @param id         the ID for the new building, assumed to be unique
     * @param buildingLine a string representation of the building in the format: name URL latitude longitude
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
     * @param floorID a floor floorID in the pathfinder database
     * @return a string version of the floor URL, given the buildingID and floorID
     */
    @GET
    @Path("/building/{buildingID}/floor/{floorID}")
    @Produces("text/plain")
    public String getFloor(@PathParam("floorID") int floorID, @PathParam("buildingID")int buildingID) {
        String result;
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URI, DB_LOGIN_ID, DB_PASSWORD);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT URL FROM Floor WHERE FloorID=" + floorID + " AND BuildingID=" + buildingID);
            if (resultSet.next()) {
                result = "" + resultSet.getString(1);
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
