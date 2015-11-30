package nahog.argentinatv;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nahog on 2015/11/26.
 */
public class Channel {

    List<String> streamsUrls = new ArrayList<String>();
    int streamIndex = 0;
    String name = "Channel";

    public Channel(String baseStream, String name) {
        this.streamsUrls.add(baseStream);
        this.name = name;
    }

    public void addStream(String stream) {
        this.streamsUrls.add(stream);
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.name + (this.streamIndex == 0 ? "" : "/" + this.streamIndex);
    }

    public String getCurrentStream() {
        return this.streamsUrls.get(streamIndex);
    }

    public String getNextStream() {
        if (this.streamIndex >= this.streamsUrls.size() - 1)
            this.streamIndex = 0;
        else
            this.streamIndex++;
        return streamsUrls.get(this.streamIndex);
    }

    public String getPreviousStream() {
        if (this.streamIndex == 0)
            this.streamIndex = this.streamsUrls.size() - 1;
        else
            this.streamIndex--;
        return this.streamsUrls.get(this.streamIndex);
    }

}
