package nahog.argentinatv;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class Channel {

    List<String> streamsUrls = new ArrayList<>();
    int streamIndex = 0;
    String name = "Channel";
    Context context;

    public Channel(String baseStream, String name, Context context) {
        this.streamsUrls.add(baseStream);
        this.name = name;
        this.context = context;
    }

    public void addStream(String stream) {
        this.streamsUrls.add(stream);
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.name + ((this.streamsUrls.size() <= 1) ? "" : " (" + (this.streamIndex + 1) + " " + context.getString(R.string.of) + " " + this.streamsUrls.size() + " " + context.getString(R.string.streams) + ")");

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
