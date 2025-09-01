package chat.client.util;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChatClientUtils {
    public static Map<String, String> mapServerNewResponse(String serverResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode actualObj = objectMapper.readTree(serverResponse);
            return Map.ofEntries(
                Map.entry("channelId", actualObj.get("channelId").asText()),
                Map.entry("host", actualObj.get("host").asText()),
                Map.entry("port", actualObj.get("port").asText())
            );
        } catch (Exception e) {
            System.out.println("Client could not parse server response " + serverResponse);
            e.printStackTrace();
            return null;
        }
    }
}
