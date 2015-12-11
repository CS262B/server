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

    @GET
    @Path("/")
    // The Java method will produce content identified by the MIME Media type "text/plain"
    @Produces("text/plain")
    public String getWelcomeMessage() {
        // Return some cliched textual content
        return "Welcome to the Pathfinder Database";
    }

    /**
     * @param id a building ID in the pathfinder database
     * @return a string version of the building record, if any, with the given id
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
