package chat.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import chat.server.channel.ChannelRunner;
import chat.server.channel.ChatChannel;
import chat.server.channel.ChatChannelImpl;
import chat.server.model.ChannelResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO: Need a thread that will delete a channel if it is running, but there are no chat members active

public class Server {
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static String DEFAULT_HOST_IP = "127.0.0.1";
    private static List<ChatChannelImpl> channelImpls;
    private static List<Integer> ports;

    public static HttpServer startServer(int port) {
        String uri = "http://0.0.0.0:" + port + "/";

        final ResourceConfig rc = new ResourceConfig().register(ChatResource.class);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(uri), rc);
        System.out.println("Jersey app started at " + uri + "\nPress enter to stop...");

        // Initialize the ports
        ports = new ArrayList<>();
        ports.add(8001); // we'll increment from here!

        // Initialize channels
        channelImpls = new ArrayList<>();

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.shutdownNow();
        return server;
    }

    @Path("/api/v1/chat")
    public static class ChatResource {
        @POST
        @Path("/new")
        @Produces(MediaType.APPLICATION_JSON)
        public ChannelResponse newChat() {
            /* This endpoint needs to create a new channel
                Increment to the next available port number
                Run the channel in a new thread
            */
            ChatChannelImpl channel = createNewChannel();
            executorService.submit(new ChannelRunner(channel));
            
            return new ChannelResponse(
                channel.getId(), 
                DEFAULT_HOST_IP, 
                channel.getServerSocket().getLocalPort()
            );
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

    private static int getNextAvailablePort() {
        return ports.get(ports.getLast()) + 1;
    }

    private static String generateChannelId() {
        return UUID.randomUUID().toString();
    }

    private static ChatChannelImpl createNewChannel() {
        ChatChannelImpl newChannel = null;
        try {
            newChannel = new ChatChannelImpl(getNextAvailablePort());
            newChannel.setId(generateChannelId());
        } catch (IOException e) {
            System.out.println("Could not create new channel.");
            e.printStackTrace();
        }
        return newChannel;
    }
}
