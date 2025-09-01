package chat.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMember {
    private String name;
    private Long id;
}
