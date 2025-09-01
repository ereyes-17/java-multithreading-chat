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
    private volatile List<ChatMember> members;
    private Thread channelThread;

    private String NEW_MEMBER_GREETING = "Welcome to chat <channelId>! There are <memNum> members here!\n" +
            "Enjoy your stay!";

    public ChatChannelImpl(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    @Override
    public void startChannel() throws IOException {
        this.channelThread = Thread.currentThread();
        
        while (true) {
            Socket socket = serverSocket.accept();
            // at this point, we have a new client connection
            // we need details on the new client
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            
            Map<String, String> clientMessageMap = ChannelUtils.mapClientMessage(dataInputStream.readUTF());
            String clientName = clientMessageMap.get(ChannelUtils.CLIENT_NAME_KEY);
            String clientId = clientMessageMap.get(ChannelUtils.CLIENT_ID_KEY);

            ChatMember newChatMember = new ChatMember(clientId, clientName, socket, this.id);
            members.add(newChatMember);

            // broadcast the new member has joined
            for (ChatMember member : members) {
                if (!member.getId().equals(clientId)) {
                    DataOutputStream memberOutputStream = new DataOutputStream(new BufferedOutputStream(member.getSocket().getOutputStream()));
                    String joinMessage = "Chat Member: " + clientName + " has joined the chat.";
                    memberOutputStream.writeUTF(joinMessage);
                    memberOutputStream.flush();
                }
            }

            // welcome the new member!
            DataOutputStream newMemberOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            String greeting = NEW_MEMBER_GREETING.replace("<channelId>", this.id).replace("<memNum>", String.valueOf(this.members.size()));
            newMemberOutputStream.writeUTF(greeting);
            newMemberOutputStream.flush();

        }
    }

    @Override
    public void removeMember(ChatMember member) {

    }

    @Override
    public void addMember(ChatMember member) {

    }
}
