package chat.server.channel;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

import chat.server.model.ChatMember;
import chat.server.util.ChannelUtils;

@Data
@NoArgsConstructor
public class ChatChannelImpl implements ChatChannel {
    private String id;
    private String name;
    private ServerSocket serverSocket;
    private List<ChatMember> members;
    private Thread channelThread;
    private String MESSAGE_BROADCAST = "From <clientName>:\n+<message>";

    public ChatChannelImpl(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    @Override
    public void startChannel() throws IOException {
        this.channelThread = Thread.currentThread();
        
        while (true) {
            Socket socket = serverSocket.accept();
            // the channel should display each client message
            // this way, we avoid clients directly connecting to each other over the socker
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            
            Map<String, String> clientMessageMap = ChannelUtils.mapClientMessage(dataInputStream.readUTF());
            String clientMessage = clientMessageMap.get(ChannelUtils.CLIENT_MESSAGE_KEY);
            String clientName = clientMessageMap.get(ChannelUtils.CLIENT_NAME_KEY);
            String clientId = clientMessageMap.get(ChannelUtils.CLIENT_ID_KEY);

            String broadcastMessage = MESSAGE_BROADCAST.replace("<clientName>", clientName).replace("<message>", clientMessage);
            dataOutputStream.writeUTF(broadcastMessage);
            dataOutputStream.flush();
        }
    }

    @Override
    public void removeMember(ChatMember member) {

    }

    @Override
    public void addMember(ChatMember member) {

    }
}
