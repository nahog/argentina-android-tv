package nahog.argentinatv;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nahog on 2015/11/26.
 */
public class Tuner {

    List<Channel> channels = new ArrayList<Channel>();
    int channelIndex = 0;

    public void addChannel(Channel channel) {
        this.channels.add(channel);
    }

    public Channel getChannelByName(String name) {
        for (Channel channel : this.channels) {
            if (channel.getName().equals(name))
                return channel;
        }
        return null;
    }

    public Channel getCurrentChannel() {
        if (channels.isEmpty())
            return null;
        return channels.get(channelIndex);
    }

    public String getDescription() {
        StringBuilder description = new StringBuilder();
        for (Channel channel : this.channels) {
            description.append(channel.getName() + (this.getCurrentChannel() == channel ? " ◁" : "") + "\n");
        }
        return description.toString();
    }

    public int getCurrentChannelIndex() {
        return channelIndex;
    }

    public void setChannelIndex(int index) {
        if (index >= 0 && index <= this.channels.size() - 1)
            this.channelIndex = index;
    }

    public Channel getNextChannel() {
        if (channels.isEmpty())
            return null;
        if (channelIndex >= channels.size() - 1)
            channelIndex = 0;
        else
            channelIndex++;
        return channels.get(channelIndex);
    }

    public Channel getPreviousChannel() {
        if (channels.isEmpty())
            return null;
        if (channelIndex == 0)
            channelIndex = channels.size() - 1;
        else
            channelIndex--;
        return channels.get(channelIndex);
    }

}
