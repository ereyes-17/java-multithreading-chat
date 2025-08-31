package chat.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;

public class Server {

    public static HttpServer startServer(int port) {
        String uri = "http://0.0.0.0:" + port + "/";
        final ResourceConfig rc = new ResourceConfig().register(ChatResource.class);
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(uri), rc);
        System.out.println("Jersey app started at " + uri + "\nPress enter to stop...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.shutdownNow();
        return server;
    }

    // main method removed; use Main.java to start

    @Path("/api/v1/chat")
    public static class ChatResource {
        @POST
        @Path("/new")
        @Produces(MediaType.TEXT_PLAIN)
        public String newChat() {
            return "New chat created.";
        }

        @POST
        @Path("/join")
        @Produces(MediaType.TEXT_PLAIN)
        public String joinChat() {
            return "Joined chat.";
        }

        @POST
        @Path("/leave")
        @Produces(MediaType.TEXT_PLAIN)
        public String leaveChat() {
            return "Left chat.";
        }
    }
}
