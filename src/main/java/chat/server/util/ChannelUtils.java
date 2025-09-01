package chat.server.util;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChannelUtils {
    public static String CLIENT_MESSAGE_KEY = "clientMessage";
    public static String CLIENT_NAME_KEY = "clientName";
    public static String CLIENT_ID_KEY = "clientId";

    public static Map<String, String> mapClientMessage(String clientMessage) {
        // client message should be JSON format
        /*
         * e.g
         * { "name": "joe", "id": "1010101", "message": "Good morning!"}
         */
        Map<String, String> mapping = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode actualObj = mapper.readTree(clientMessage);

            String clientName = actualObj.get("name").textValue();
            String clientId = actualObj.get("id").textValue();
            String message = actualObj.get("message").textValue();

            mapping = new HashMap<>();
            mapping.put(CLIENT_NAME_KEY, clientName);
            mapping.put(CLIENT_ID_KEY, clientId);
            mapping.put(CLIENT_MESSAGE_KEY, message);
        } catch (JsonProcessingException | JsonMappingException e) {
            System.out.println(String.format("Could not parse clientMessage %s", clientMessage));
        }

        return mapping;
    }
}
