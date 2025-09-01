package chat.server.channel;

import java.io.IOException;
import java.util.List;

import chat.server.model.ChatMember;

public interface ChatChannel {
    public void startChannel() throws IOException;
    public void removeMember(ChatMember member);
    public void addMember(ChatMember member);
}
