package nahog.argentinatv;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {

    private VideoView videoView;
    private TextView textView;
    private TextView channelView;

    private static boolean isInMenu = false;
    private static int menuIndex = -1;

    private static Channel defaultChannel = new Channel("rtsp://stream.eltrecetv.com.ar/live13/13tv/13tv1", "El Trece");
    private static String defaultChannels = "El Trece|rtsp://stream.eltrecetv.com.ar/live13/13tv/13tv1\n" +
                                            "TN|rtsp://stream.tn.com.ar/live/tnhd1\n" +
                                            "TN|rtsp://stream.tn.com.ar/live/tnhd2\n" +
                                            "TN|rtsp://stream.tn.com.ar/live/tnhd3\n" +
                                            "TN|rtsp://stream.tn.com.ar/live/tnmovil1\n" +
                                            "TN|rtsp://stream.tn.com.ar/live/tnmovil2\n" +
                                            "TN|rtsp://stream.tn.com.ar/live/tnmovil2\n" +
                                            "C5N|http://c5n.stweb.tv:1935/c5n/live_media/playlist.m3u8\n" +
                                            "Canal 26|rtsp://live-edge01.telecentro.net.ar:80/live/26hd-360\n" +
                                            "Magazine|rtsp://stream.mgzn.tv/live/mgzntv/mgzntv\n" +
                                            "Rural|rtsp://streamrural.cmd.com.ar:554/liverural/crural/rural1\n" +
                                            "TV Publica|http://live-edge01.telecentro.net.ar/live/smil:tvp.smil/master.m3u8\n" +
                                            "TV Publica|http://live-edge01.telecentro.net.ar/live/smil:tvp.smil/chunklist_w1219135172_b2628000_sleng.m3u8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setContentView(R.layout.activity_main);

        textView = (TextView)this.findViewById(R.id.text);
        textView.getBackground().setAlpha(200);
        channelView = (TextView)this.findViewById(R.id.channels);
        channelView.getBackground().setAlpha(200);
        videoView = (VideoView)this.findViewById(R.id.video);
        videoView.setMediaController(null);

        String channelsFileContent = getChannelsFromFile();

        final Tuner tuner = new Tuner();

        parseChannels(channelsFileContent, tuner);

        tuner.setChannelIndex(getSavedChannelIndex());

        textView.setVisibility(View.INVISIBLE);
        channelView.setVisibility(View.INVISIBLE);

        final CountDownTimer timer = new CountDownTimer(5000, 100) {
            public void onTick(long millisUntilFinished) {}
            public void onFinish() {
                textView.setVisibility(View.INVISIBLE);
            }
        };
        changeStream(timer, tuner, tuner.getCurrentChannel());
        videoView.requestFocus();

        videoView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                // Bypass volume up and down so they work as normal inside the app.
                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
                    keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
                    keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
                    return false;
                }

                // Prevent gettin two events for each action.
                if (event.getAction() != KeyEvent.ACTION_DOWN)
                    return true;

                Channel currentChannel = tuner.getCurrentChannel();

                if (MainActivity.isInMenu)
                {
                    // Move inside the channel guide
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (MainActivity.menuIndex >= tuner.getChannelCount() - 1)
                            MainActivity.menuIndex = 0;
                        else
                            MainActivity.menuIndex++;
                        channelView.setText(Html.fromHtml(getChannelGridMenu(currentChannel, tuner)));
                    }

                    //  Move inside the channel guide
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (MainActivity.menuIndex < 0)
                            MainActivity.menuIndex = tuner.getChannelCount() - 1;
                        else
                            MainActivity.menuIndex--;
                        channelView.setText(Html.fromHtml(getChannelGridMenu(currentChannel, tuner)));
                    }

                    // Access the channel list and info with the guide button or dpad center button.
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        tuner.setChannelIndex(MainActivity.menuIndex);
                        Channel newChannel = tuner.getCurrentChannel();
                        if (!currentChannel.equals(newChannel))
                            changeStream(timer, tuner, newChannel);
                        MainActivity.isInMenu = false;
                        MainActivity.menuIndex = -1;
                        textView.setVisibility(View.INVISIBLE);
                        channelView.setVisibility(View.INVISIBLE);
                    }
                }
                else {
                    // "Tune" next channel with channel up button or dpad up
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_CHANNEL_UP) {
                        changeStream(timer, tuner, tuner.nextChannel());
                    }

                    // "Tune" previous channel with channel down button or dpad down
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN) {
                        changeStream(timer, tuner, tuner.previousChannel());
                    }

                    // If the channel has different streams, move to the next stream on dpad right.
                    if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        String currentStream = currentChannel.getCurrentStream();
                        String nextStream = currentChannel.getNextStream();
                        if (!currentStream.equals(nextStream)) {
                            changeStream(timer, tuner, currentChannel);
                        }
                    }

                    // If the channel has different streams, move to the previous stream on dpad left.
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        String currentStream = currentChannel.getCurrentStream();
                        String nextStream = currentChannel.getPreviousStream();
                        if (!currentStream.equals(nextStream)) {
                            changeStream(timer, tuner, currentChannel);
                        }
                    }

                    // Access the channel list and info with the guide button or dpad center button.
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        MainActivity.isInMenu = true;
                        textView.setText(currentChannel.getDescription());
                        channelView.setText(Html.fromHtml(getChannelGridMenu(currentChannel, tuner)));
                        textView.setVisibility(View.VISIBLE);
                        channelView.setVisibility(View.VISIBLE);
                    }
                }

                // Do not process any other event.
                return true;
            }
        });
    }

    private String getChannelGridMenu(Channel selectedChannel, Tuner tuner) {
            StringBuilder description = new StringBuilder();
            int currentIndex = 0;
            for (Channel channel : tuner) {
                String channelLine = channel.getName();
                if (selectedChannel.equals(channel)) {
                    if (MainActivity.menuIndex == -1)
                        MainActivity.menuIndex = currentIndex;
                }
                if (currentIndex == MainActivity.menuIndex)
                    channelLine += " ◁";
                channelLine = "<span>" + channelLine + "</span><br>\n";
                description.append(channelLine);
                currentIndex++;
            }
            return description.toString();
    }

    /// Channels are loaded from a file with a simple format: <CHANNEL_NAME>|<CHANNEL_URL>
    /// using a default channels file preloaded with some Argentina's IPTV channels.
    /// The channel is stored on the external storage inside app folder
    /// (i.e. /sdcard/nahog.argentinatv/files/channels.xml)
    @NonNull
    private String getChannelsFromFile() {
        File channelsFile = new File(getExternalFilesDir(null), "channels.txt");
        FileInputStream in;
        if (!channelsFile.exists())
            createChannelsFile(MainActivity.defaultChannels);
        int length = (int)channelsFile.length();
        byte[] bytes = new byte[length];
        try {
            in = new FileInputStream(channelsFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return MainActivity.defaultChannels;
        }
        try {
            int result = in.read(bytes);
            if (result == 0)
                return MainActivity.defaultChannels;
        } catch (IOException e) {
            e.printStackTrace();
            return MainActivity.defaultChannels;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new String(bytes);
    }

    /// Create a new channels file if does not exists in the external storage.
    private void createChannelsFile(String channelsFileContent) {
        File newChannelsFile =  new File(getExternalFilesDir(null), "channels.txt");
        try {
            boolean result = newChannelsFile.createNewFile();
            if (!result)
                return;
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(newChannelsFile);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return;
        }
        try {
            stream.write(channelsFileContent.getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /// Parse the channel files, trying to be fail safe and ignoring broken channels
    /// and if nothing is found try to use the default channel.
    private void parseChannels(String channelsFileContent, Tuner tuner) {
        for (String line : channelsFileContent.split("\\n")) {
            try {
                String[] lineParts = line.split("\\|");
                Channel channel = tuner.getChannelByName(lineParts[0]);
                if (channel != null) {
                    channel.addStream(lineParts[1]);
                    //channel.addStream("http://techslides.com/demos/sample-videos/small.mp4?stream=" + channel.getStreamCount()); // Test stream
                } else {
                    channel = new Channel(lineParts[1], lineParts[0]);
                    //channel = new Channel("http://techslides.com/demos/sample-videos/small.mp4", lineParts[0]); // Test stream
                    tuner.addChannel(channel);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (tuner.getChannelCount() == 0)
            tuner.addChannel(MainActivity.defaultChannel);
    }

    /// Set the video view source to the new channel stream
    private void changeStream(final CountDownTimer timer, final Tuner tuner, final Channel channel) {
        timer.cancel();
        videoView.setVideoURI(Uri.parse(channel.getCurrentStream()));
        final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this, getString(R.string.tuner), getString(R.string.tuning) + " " + channel.getDescription() + "...", true);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                progressDialog.dismiss();
                finishChangeStream(mp, channel, timer, tuner);
            }
        });
    }

    private void finishChangeStream(MediaPlayer mp, Channel currentChannel, CountDownTimer timer, Tuner tuner) {
        textView.setText(currentChannel.getDescription());
        textView.setVisibility(View.VISIBLE);
        mp.start();
        timer.start();
        SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.saved_channel), tuner.getCurrentChannelIndex());
        editor.apply();
    }

    private int getSavedChannelIndex() {
        SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt(getString(R.string.saved_channel), 0);
    }

}
