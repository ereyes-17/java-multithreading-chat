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
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

import com.fasterxml.jackson.core.io.DataOutputAsStream;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ChatClient {
    private String id;
    private String name;
    private String channelId;
    private Socket socket;
    private String serverUrl;

    private void connectToChannel(String host, int port) throws UnknownHostException, IOException {
        socket = new Socket(host, port);

        while(true) {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    
        }
    }

    public ChatClient(int port) {
        this.serverUrl = "http://localhost:" + port;
    }
    
    public void startInteractive() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        System.out.println("Hello, " + name + "!");
        System.out.println("Do you want to create a new channel or join an existing one? (new/join)");
        String action = scanner.nextLine().trim().toLowerCase();

        String channelId = null;
        String requestBody = null;
        String url = null;
        if (action.equals("new")) {
            System.out.println("Creating a new channel...");
            // Here you would call the server to create a new channel and get the channelId
            requestBody = "{\"name\": \"" + name + "\", \"id\": \"" + UUID.randomUUID().toString() + "\"}";
            url = this.serverUrl + "/api/v1/chat/new";
        } else if (action.equals("join")) {
            channelId = scanner.nextLine().trim();
            requestBody = "{\"name\": \"" + name + "\", \"id\": \"" + UUID.randomUUID().toString() + "\", \"channelId\": \"" + channelId + "\"}";
            url = this.serverUrl + "/api/v1/chat/join";
        } else {
            System.out.println("Invalid option. Exiting.");
            scanner.close();
            System.exit(1);
        }

        String response = null;
        try {
            System.out.println("Sending request to server: " + url + " with response body: " + requestBody);
            response = submitNewRequest(requestBody, url);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        scanner.close();
        System.out.println("Server response: " + response);
        System.exit(1);
    }

    private String submitNewRequest(String requestBody, String url) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest hRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Content-Length", String.valueOf(requestBody.length()))
            .build();

        HttpResponse<String> response = httpClient.send(hRequest, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
