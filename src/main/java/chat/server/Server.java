package chat.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import chat.server.channel.ChannelRunner;
import chat.server.channel.ChatChannel;
import chat.server.channel.ChatChannelImpl;
import chat.server.model.ChannelResponse;
import chat.server.util.ChannelUtils;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO: Need a thread that will delete a channel if it is running, but there are no chat members active

public class Server {
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static String DEFAULT_HOST_IP = "127.0.0.1";
    private static List<ChatChannelImpl> channels;
    private static List<Integer> ports;

    public static HttpServer startServer(int port) {
        String uri = "http://0.0.0.0:" + port + "/";

        final ResourceConfig rc = new ResourceConfig()
            .register(ChatResource.class)
            .register(ObjectMapperProvider.class)
            .register(SerializationFeature.class);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(uri), rc);
        System.out.println("Jersey app started at " + uri + "\nPress enter to stop...");

        // Initialize the ports
        ports = new ArrayList<>();

        // Initialize channels
        channels = new ArrayList<>();

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
        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.APPLICATION_JSON)
        public String newChat(String requestBody) throws JsonProcessingException {
            /* This endpoint needs to create a new channel
                Increment to the next available port number
                Run the channel in a new thread
                requestBody contains the raw string sent by the client
            */
            System.out.println("Creating a new channel for client: " + ChannelUtils.mapClientMessage(requestBody).get("clientName"));

            ChatChannelImpl channel = createNewChannel();
            channels.add(channel);

            //executorService.submit(new ChannelRunner(channel));

            // Provide new channel info
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(new ChannelResponse(
                channel.getId(),
                DEFAULT_HOST_IP,
                channel.getServerSocket().getLocalPort()
            ));
        }

        @POST
        @Path("/join")
        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.APPLICATION_JSON)
        public String joinChat(String requestBody) {
            Map<String, String> clientData = ChannelUtils.mapClientRequest(requestBody);
            // verify channel id exists
            Optional<ChatChannelImpl> channel = channels.stream().filter(c -> c.getId().equals(clientData.get(ChannelUtils.CHANNEL_ID_KEY))).findFirst();

            if (channel.isPresent()) {
                // Provide information needed to join the channel
                ChannelResponse channelResponse = new ChannelResponse(
                    channel.get().getId(),
                    DEFAULT_HOST_IP,
                    channel.get().getServerSocket().getLocalPort()
                );
                return channelResponse.toString();
            } else {
                return "{\"error\": \"CHANNEL_NOT_FOUND\"}";
            }
        }

        @POST
        @Path("/leave")
        @Produces(MediaType.TEXT_PLAIN)
        public String leaveChat() {
            return "Left chat.";
        }
    }

    private static int getNextAvailablePort() {
        if (ports.size() == 0) {
            ports.add(8001); // only up from here
            return 8001;
        }
        int nextAvailablePort = ports.get(ports.size() - 1) + 1;
        ports.add(nextAvailablePort);
        return nextAvailablePort;
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
