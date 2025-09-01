package chat.server.channel;

import java.io.IOException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChannelRunner implements Runnable {
    private ChatChannelImpl channel;

    @Override
    public void run() {
        try {
            this.channel.startChannel();
        } catch (IOException e) {
            System.err.println("Could not start channel.");
            System.err.println(String.format("Channel info: ID: %S Thread: %s", this.channel.getId(), this.channel.getChannelThread().getName()));
            e.printStackTrace();
        }
    }
}
