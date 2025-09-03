package chat.server.channel;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import chat.server.model.ChatMember;
import lombok.Getter;

public class ChatMemberHandler implements Runnable {
    @Getter
    private final ChatMember currentMember;
    private final Socket socket;
    private String threadName;
    private final ChatChannelImpl channel;

    private String MESSAGE_BROADCAST = "From <clientName>:\n\t<message>\n";
    private String NEW_MEMBER_GREETING = "Welcome to chat <channelId>! There are <memNum> members here!\n" +
            "Enjoy your stay!";

    public ChatMemberHandler(ChatMember member, ChatChannelImpl channel) {
        this.currentMember = member;
        this.socket = member.getSocket();
        this.channel = channel;
    }

    @Override
    public void run() {
        this.threadName = Thread.currentThread().getName();
        System.out.println(String.format("Thread %s is handling chat member: %s", this.threadName, this.currentMember.getName()));
        // welcome the new member!
        DataOutputStream newMemberOutputStream;
        try {
            newMemberOutputStream = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
            String greeting = NEW_MEMBER_GREETING.replace("<channelId>", this.channel.getId()).replace("<memNum>", String.valueOf(this.channel.getMembers().size()));
            newMemberOutputStream.writeUTF(greeting);
            newMemberOutputStream.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        Thread.currentThread().interrupt();
        while (true) {
            try {
                DataInputStream dataInputStream = new DataInputStream(this.socket.getInputStream());

                String message = dataInputStream.readUTF();
                String broadcastMessage = MESSAGE_BROADCAST.replace("<clientName>", this.currentMember.getName()).replace("<message>", message);

                // send to all members except the current member
                for (ChatMember member : this.channel.getMembers()) {
                    if (!member.getId().equals(this.currentMember.getId())) {
                        DataOutputStream memberOutputStream = new DataOutputStream(new BufferedOutputStream(member.getSocket().getOutputStream()));
                        memberOutputStream.writeUTF(broadcastMessage);
                        //memberOutputStream.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
