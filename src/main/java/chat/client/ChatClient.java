package chat.client;

import java.util.List;
import java.util.Map;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.DataOutputAsStream;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ChatClient {
    private String id = UUID.randomUUID().toString();
    private String name;
    private String channelId;
    private Socket socket;
    private String serverUrl;
    private CloseableHttpClient httpClient;
    private int channelPort = 0;

    public void connectToChannel(boolean joinChannel) throws UnknownHostException, IOException {
        if (!joinChannel) {
            return;
        }

        socket = new Socket("127.0.0.1", this.channelPort);
        // let channel know we in here dough
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        String nextMessage = String.format("{\"name\":\"%s\",\"id\":\"%s\",\"message\":\"%s\"}", this.name, this.id, "");
        dataOutputStream.writeUTF(nextMessage);
        dataOutputStream.flush();
        //dataOutputStream.close();
        Scanner scanner = new Scanner(System.in);

        // Thread for reading messages from the socket
        Thread readerThread = new Thread(() -> {
            try {
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                while (true) {
                    String incoming = dataInputStream.readUTF();
                    System.out.println("[Server]: " + incoming);
                }
            } catch (IOException e) {
                System.out.println("Connection closed by server.");
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();

        // Main thread for user input and sending messages
        try {
            while (true) {
                System.out.print("Your message: ");
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("--finish--")) {
                    break;
                }
                nextMessage = String.format("{\"name\":\"%s\",\"id\":\"%s\",\"message\":\"%s\"}", this.name, this.id, message);
                dataOutputStream.writeUTF(nextMessage);
                dataOutputStream.flush();
            }
        } finally {
            // Interrupt reader thread and close socket to end blocking readUTF
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
            readerThread.interrupt();
            scanner.close();
        }
    }

    public ChatClient(int port) {
        this.serverUrl = "http://localhost:" + port;
        this.httpClient = HttpClients.createDefault();
    }
    
    public boolean startInteractive() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your name: ");
        this.name = scanner.nextLine();

        System.out.println("Hello, " + this.name + "!");
        System.out.println("Do you want to create a new channel or join an existing one? (new/join)");
        String action = scanner.nextLine().trim().toLowerCase();

        String channelId = null;
        String requestBody = null;
        String url = null;
        if (action.equals("new")) {
            System.out.println("Creating a new channel...");
            // Send a simple string for text/plain
            requestBody = String.format("{\"name\":\"%s\",\"id\":\"%s\"}", this.name, this.id);
            url = this.serverUrl + "/api/v1/chat/new";
        } else if (action.equals("join")) {
            System.out.print("Enter the channel id to join: ");
            channelId = scanner.nextLine().trim();
            requestBody = String.format("{\"name\":\"%s\",\"id\":\"%s\",\"channelId\":\"%s\"}", this.name, this.id, channelId);
            url = this.serverUrl + "/api/v1/chat/join";
        } else {
            System.out.println("Invalid option. Exiting.");
            scanner.close();
            System.exit(1);
        }

        String response = null;
        try {
            System.out.println("Sending request to server: " + url + " with request body: " + requestBody);
            response = sendPostRequest(requestBody, url);
            this.channelPort = Integer.parseInt(mapServerResponse(response).get("port"));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        scanner.close();
        System.out.println("Server response: " + response);

        if (action.equals("join")) {
            return true;
        }
        return false;
    }

    private String sendPostRequest(String requestBody, String url) throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(requestBody, ContentType.TEXT_PLAIN));
        httpPost.addHeader("Content-Type", ContentType.TEXT_PLAIN.getMimeType());
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    private Map<String, String> mapServerResponse(String serverResponse) throws JsonMappingException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode actualObj = objectMapper.readTree(serverResponse);
        Map<String, String> map = new HashMap<>();
        map.put("port", actualObj.get("port").asText());
        return map;
    }
}
