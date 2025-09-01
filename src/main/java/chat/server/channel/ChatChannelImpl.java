package chat.server.channel;

import java.io.IOError;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import chat.server.model.ChatMember;;

@Data
@NoArgsConstructor
public class ChatChannelImpl implements ChatChannel {
    private String id;
    private String name;
    private ServerSocket serverSocket;
    private List<ChatMember> members;
    private Thread channelThread;

    @Override
    public void startChannel() throws IOException {
        Socket socket = this.serverSocket.accept();
        socket.getChannel().open();
    }

    @Override
    public void removeMember(ChatMember member) {

    }

    @Override
    public void addMember(ChatMember member) {

    }
}
