package chat.client;

import java.util.List;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.Scanner;

import com.fasterxml.jackson.core.io.DataOutputAsStream;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ChatClient {
    private String id;
    private String name;
    private String channelId;
    private Socket socket;

    private void connectToChannel(String host, int port) throws UnknownHostException, IOException {
        socket = new Socket(host, port);

        while(true) {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    
        }
    }
    
    public void startInteractive() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        System.out.println("Hello, " + name + "!");
        System.out.println("Do you want to create a new channel or join an existing one? (new/join)");
        String action = scanner.nextLine().trim().toLowerCase();

        String channelId = null;
        if (action.equals("new")) {
            System.out.println("Creating a new channel...");
            // Here you would call the server to create a new channel and get the channelId
            channelId = "<new-channel-id>";
            System.out.println("New channel created with id: " + channelId);
        } else if (action.equals("join")) {
            System.out.print("Enter the channel id to join: ");
            channelId = scanner.nextLine();
            System.out.println("Joining channel with id: " + channelId);
        } else {
            System.out.println("Invalid option. Exiting.");
            scanner.close();
        }

        List<String> channelIds = new ArrayList<>();
        channelIds.add(channelId);
        ChatClient client = new ChatClient(null, name, channelIds);
        // Continue with client logic (e.g., chat loop)
        scanner.close();
    }

    private String submitNewRequest() {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest hRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/api/v1/chat/new"))
            .POST(HttpRequest.BodyPublishers.ofString("Hello, server!"))
            .build();
        httpClient.send(hRequest, HttpResponse.BodyHandlers.ofString());
    }
}
