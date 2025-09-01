package chat.server.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChannelRunner implements Runnable {
    private ChatChannel channel;

    @Override
    public void run() {
        this.channel.startChannel();
    }
}
