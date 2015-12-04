package nahog.argentinatv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tuner implements Iterable<Channel> {

    private List<Channel> channels = new ArrayList<>();
    private int channelIndex = 0;

    @Override
    public Iterator<Channel> iterator() {
        return this.channels.iterator();
    }

    public void addChannel(Channel channel) {
        this.channels.add(0, channel);
    }

    public int getChannelCount() {
        return this.channels.size();
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

    public int getCurrentChannelIndex() {
        return channelIndex;
    }

    public void setChannelIndex(int index) {
        if (index >= 0 && index <= this.channels.size() - 1)
            this.channelIndex = index;
    }

    public Channel nextChannel() {
        if (channels.isEmpty())
            return null;
        if (channelIndex >= channels.size() - 1)
            channelIndex = 0;
        else
            channelIndex++;
        return channels.get(channelIndex);
    }

    public Channel previousChannel() {
        if (channels.isEmpty())
            return null;
        if (channelIndex == 0)
            channelIndex = channels.size() - 1;
        else
            channelIndex--;
        return channels.get(channelIndex);
    }
}
