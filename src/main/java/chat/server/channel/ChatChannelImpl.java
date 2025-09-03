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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import chat.server.model.ChatMember;
import chat.server.util.ChannelUtils;

@Data
@NoArgsConstructor
public class ChatChannelImpl implements ChatChannel {
    private String id;
    private String name;
    private String channelMemberAdminId;
    private ServerSocket serverSocket;
    private volatile List<ChatMember> members = new ArrayList<>();
    private Thread channelThread;
    private ExecutorService executorService = Executors.newCachedThreadPool();

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
            String clientInfoMessage = dataInputStream.readUTF();
            //System.out.println(clientInfoMessage);
            //dataInputStream.close();
            
            Map<String, String> clientMessageMap = ChannelUtils.mapClientMessage(clientInfoMessage);
            String clientName = clientMessageMap.get(ChannelUtils.CLIENT_NAME_KEY);
            String clientId = clientMessageMap.get(ChannelUtils.CLIENT_ID_KEY);
            System.out.println(String.format("Name: %s, ID: %s", clientName, clientId));

            ChatMember newChatMember = new ChatMember(clientId, clientName, socket, this.id);
            this.members.add(newChatMember);
            //members.add(newChatMember);
            //for (ChatMember member : members) {
            //    System.out.println(member.toString());
            //}
            executorService.submit(new ChatMemberHandler(newChatMember, this));

            // broadcast the new member has joined
            /*for (ChatMember member : members) {
                if (!member.getId().equals(clientId)) {
                    DataOutputStream memberOutputStream = new DataOutputStream(new BufferedOutputStream(member.getSocket().getOutputStream()));
                    String joinMessage = "Chat Member: " + clientName + " has joined the chat.";
                    memberOutputStream.writeUTF(joinMessage);
                    //memberOutputStream.flush();
                }
            }*/
            //dataInputStream.close();
        }
    }

    @Override
    public void removeMember(ChatMember member) {

    }

    @Override
    public void addMember(ChatMember member) {
        members.add(member);
    }
}
