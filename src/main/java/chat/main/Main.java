package chat.main;

import chat.server.Server;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage:");
            System.out.println("  To start server: java Main server <port>");
            System.out.println("  To start client: java Main client <clientName> [groupId]");
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
            String clientName = args[1];
            String groupId = args.length > 2 ? args[2] : null;
            System.out.println("Starting client with name: " + clientName + (groupId != null ? ", group: " + groupId : ""));
            // TODO: Implement client logic here
        } else {
            System.out.println("Unknown mode: " + mode);
        }
    }
}
