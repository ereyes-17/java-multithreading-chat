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
import chat.server.model.ChatMember;
import chat.server.util.ChannelUtils;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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
        String uri = "http://127.0.0.1:" + port;

        final ResourceConfig rc = new ResourceConfig()
            .register(ChatResource.class)
            .register(HealthResource.class);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(uri), rc);
        System.out.println("Jersey app started at " + uri + "\nServer is running. Press Ctrl+C to stop.");

        // Initialize the ports
        ports = new ArrayList<>();

        // Initialize channels
        channels = new ArrayList<>();

        // Server will keep running until process is killed (Ctrl+C)
        return server;
    }

    @Path("/api/v1/health")
    public static class HealthResource {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String healthCheck() {
            return "Server is healthy.";
        }
    }

    @Path("/api/v1/chat")
    public static class ChatResource {
        @POST
        @Path("/new")
        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.TEXT_PLAIN)
        public String newChat(String requestBody) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                System.out.println("Received request to create a new chat channel: " + requestBody);
                String clientName = ChannelUtils.mapClientRequest(requestBody).get(ChannelUtils.CLIENT_NAME_KEY);
                System.out.println("Creating a new channel for client: " + clientName);

                ChatChannelImpl channel = createNewChannel();
                if (channel == null) {
                    System.out.println("Channel creation failed.");
                    return objectMapper.writeValueAsString(Map.of("error", "CHANNEL_CREATION_FAILED"));
                }
                channels.add(channel);
                executorService.submit(new ChannelRunner(channel));
                ChannelResponse response = new ChannelResponse(
                    channel.getId(),
                    DEFAULT_HOST_IP,
                    channel.getServerSocket().getLocalPort()
                );
                return objectMapper.writeValueAsString(response);
            } catch (Exception e) {
                System.out.println("Error in newChat endpoint: " + e.getMessage());
                e.printStackTrace();
                try {
                    return objectMapper.writeValueAsString(Map.of("error", "INTERNAL_SERVER_ERROR"));
                } catch (JsonProcessingException ex) {
                    return "{\"error\":\"INTERNAL_SERVER_ERROR\"}";
                }
            }
        }

        @POST
        @Path("/join")
        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.TEXT_PLAIN)
        public String joinChat(String requestBody) {
            Map<String, String> clientData = ChannelUtils.mapClientRequest(requestBody);
            if (clientData.get(ChannelUtils.CHANNEL_ID_KEY) == null) {
                return "{\"error\": \"CHANNEL_ID_MISSING\"}";
            }
            // verify channel id exists
            Optional<ChatChannelImpl> channel = channels.stream().filter(c -> c.getId().equals(clientData.get(ChannelUtils.CHANNEL_ID_KEY))).findFirst();
            ObjectMapper objectMapper = new ObjectMapper();
            if (channel.isPresent()) {
                // Provide information needed to join the channel
                ChannelResponse channelResponse = new ChannelResponse(
                    channel.get().getId(),
                    DEFAULT_HOST_IP,
                    channel.get().getServerSocket().getLocalPort()
                );
                try {
                    return objectMapper.writeValueAsString(channelResponse);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return "{\"error\":\"INTERNAL_SERVER_ERROR\"}";
                }
            } else {
                return "{\"error\": \"CHANNEL_NOT_FOUND\"}";
            }
        }

        @POST
        @Path("/leave/{channelId}")
        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.TEXT_PLAIN)
        public String leaveChat(@PathParam("channelId") String channelId, String requestBody) {
            Map<String, String> clientData = ChannelUtils.mapClientRequest(requestBody);
            Optional<ChatChannelImpl> channelOptional = channels.stream().filter(c -> c.getId().equals(clientData.get(ChannelUtils.CHANNEL_ID_KEY))).findFirst();
            ChatChannelImpl channel;
            if (channelOptional.isPresent()) {
                channel = channelOptional.get();
            } else {
                return "No such channel.";
            }
            List<ChatMember> chatMembers = channel.getMembers();
            Optional<ChatMember> chatMemberOptional = chatMembers.stream().filter(m -> m.getId().equals(clientData.get(ChannelUtils.CLIENT_ID_KEY))).findFirst();
            if (chatMemberOptional.isPresent()) {
                // might wanna thread this
                ChatMember chatMember = chatMemberOptional.get();
                try {
                    chatMember.getSocket().getOutputStream().close();
                    chatMember.getSocket().getInputStream().close();
                    chatMember.getSocket().close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                channel.removeMember(chatMemberOptional.get());
                return "Removed from channel.";
            }
            return "Left chat.";
        }

        @POST
        @Path("/delete/{channelId}")
        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.TEXT_PLAIN)
        public String deleteChannel(@PathParam("channelId") String channelId, String requestBody) {
            return null;
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
