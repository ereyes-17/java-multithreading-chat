package chat.main;

import chat.client.ChatClient;
import chat.server.Server;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage:");
            System.out.println("  To start server: java Main server <port>");
            System.out.println("  To start client: java Main client <clientName> [channelId]");
            System.exit(1);
        }

        String mode = args[0];
        if (mode.equalsIgnoreCase("server")) {
            int port;
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Port must be an integer.");
                return;
            }
            System.out.println("Starting server on port " + port + "...");
            Server.startServer(port);
        } else if (mode.equalsIgnoreCase("client")) {
            String targetServerPort = args[1];
            ChatClient chatClient = new ChatClient(Integer.parseInt(targetServerPort));
            boolean joinChannel = chatClient.startInteractive();
            try {
                chatClient.connectToChannel(joinChannel);
            } catch(Exception e) {
                e.printStackTrace();
            }
            
        } else {
            System.out.println("Unknown mode: " + mode);
        }
    }
}
