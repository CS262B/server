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
 * Class PathfinderWebService implements the RESTful web service for the Pathfinder app
 */
// The Java class will be hosted at the URI path "/pathfinder"
@Path("/pathfinder")
public class PathfinderWebService {
    private static String DB_URI = "jdbc:postgresql://localhost:5432/pathfinder";
    private static String DB_LOGIN_ID = "postgres";
    private static String DB_PASSWORD = "postgres";

    /**
     * getBuilding outputs a String containing the row of the Building table 
     * specified by the given id.
     * 
     * @param: id, a building ID in the pathfinder database
     * @return: a String version of the building record, if any, with the given id
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
                result ="" + resultSet.getInt(1) + " " + resultSet.getString(2) + " " + resultSet.getFloat(3) + " " + resultSet.getFloat(4);
            } else {
                result = "null";
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
     * getBuildings pulls the entire Building table from the pathfinder database
     * and outputs it as plaintext
     * 
     * @return: a String containing the Building table, with each value separated by 
     *          commas and each row separated by new lines
     */
    @GET
    @Path("/buildings")
    @Produces("text/plain")
    public String getBuildings() {
        String result = "";
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URI, DB_LOGIN_ID, DB_PASSWORD);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Building");
            while (resultSet.next()) {
                result += resultSet.getInt(1) + "," + resultSet.getString(2) + "," + resultSet.getFloat(3) + "," + resultSet.getFloat(4) + "\n";
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            result = e.getMessage().toString();
        }
        return result;
    }
    
    /**
     * getFloor returns a String containing a row of the Floor table 
     * based on the given buildingID and floorID
     * 
     * @param: buildingID, the BuildingID for the query
     * @param: floorID, the FloorID for the query
     * 
     * @return: a String giving the information in the row of the Floor table that was queried
     */
    @GET
    @Path("/building/{buildingID}/floor/{floorID}")
    @Produces("text/plain")
    public String getFloor(@PathParam("buildingID") int buildingID, @PathParam("floorID") int floorID) {
        String result;
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URI, DB_LOGIN_ID, DB_PASSWORD);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Floor WHERE Floor.BuildingID=" + buildingID + "AND Floor.FloorID=" + floorID);
            if (resultSet.next()) {
                result ="" + resultSet.getInt(1) + " " + resultSet.getInt(2) + " " + resultSet.getInt(3) + " " + resultSet.getString(4);
            } else {
                result = "null";
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    //create the web service and run it
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
