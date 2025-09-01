package chat.server.model;

import java.net.Socket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMember {
    private String name;
    private String id;
    private Socket socket;
    private String channelId;
}
