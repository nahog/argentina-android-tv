package nahog.argentinatv;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends Activity {

    VideoView videoView;
    ImageView imageView;
    TextView textView;
    TextView channelView;
    CountDownTimer timer;
    static String defaultChannels = "El Trece|rtsp://stream.eltrecetv.com.ar/live13/13tv/13tv1\nCanal 26|rtsp://live-edge01.telecentro.net.ar:80/live/26hd-360\nTN|rtsp://stream.tn.com.ar/live/tnhd1\nTN|rtsp://stream.tn.com.ar/live/tnhd2\nTN|rtsp://stream.tn.com.ar/live/tnhd3\nTN|rtsp://stream.tn.com.ar/live/tnmovil1\nTN|rtsp://stream.tn.com.ar/live/tnmovil2\nTN|rtsp://stream.tn.com.ar/live/tnmovil2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setContentView(R.layout.activity_main);

        textView = (TextView)this.findViewById(R.id.text);
        textView.getBackground().setAlpha(200);
        channelView = (TextView)this.findViewById(R.id.channels);
        channelView.getBackground().setAlpha(200);
        imageView = (ImageView)this.findViewById(R.id.image);
        videoView = (VideoView)this.findViewById(R.id.video);
        videoView.setMediaController(null);

        String channelsFileContent = getChannelsFromFile();

        final Tuner tuner = new Tuner();

        parseChannels(channelsFileContent, tuner);

        tuner.setChannelIndex(getSavedChannelIndex());

        final Channel currentChannel = tuner.getCurrentChannel();
        textView.setVisibility(View.INVISIBLE);
        channelView.setVisibility(View.INVISIBLE);

        final CountDownTimer timer = new CountDownTimer(5000, 100) {
            public void onTick(long millisUntilFinished) {}
            public void onFinish() {
                channelView.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);
            }
        };
        changeStream(timer, tuner, currentChannel);
        videoView.requestFocus();

        videoView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()!=KeyEvent.ACTION_DOWN)
                    return true;

                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    changeStream(timer, tuner, tuner.getNextChannel());
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    changeStream(timer, tuner, tuner.getPreviousChannel());
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    final Channel currentChannel = tuner.getCurrentChannel();
                    String currentStream = currentChannel.getCurrentStream();
                    String nextStream = currentChannel.getNextStream();
                    if (currentStream != nextStream) {
                        changeStream(timer, tuner, currentChannel);
                    }
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    final Channel currentChannel = tuner.getCurrentChannel();
                    String currentStream = currentChannel.getCurrentStream();
                    String nextStream = currentChannel.getPreviousStream();
                    if (currentStream != nextStream) {
                        changeStream(timer, tuner, currentChannel);
                    }
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    timer.cancel();
                    Channel currentChannel = tuner.getCurrentChannel();
                    textView.setText(currentChannel.getDescription());
                    channelView.setText(tuner.getDescription());
                    textView.setVisibility(View.VISIBLE);
                    channelView.setVisibility(View.VISIBLE);
                    timer.start();
                }
                return true;
            }
        });
    }

    @NonNull
    private String getChannelsFromFile() {
        File channelsFile = new File(getExternalFilesDir(null), "channels.txt");
        FileInputStream in = null;
        try {
            in = new FileInputStream(channelsFile);
        } catch (FileNotFoundException e) {
            createChannelsFile(MainActivity.defaultChannels);
        }
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
        FileOutputStream stream = null;
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

    private void parseChannels(String channelsFileContent, Tuner tuner) {
        for (String line : channelsFileContent.split("\\n")) {
            String[] lineParts = line.split("\\|");
            Channel channel = tuner.getChannelByName(lineParts[0]);
            if (channel != null) {
                channel.addStream(lineParts[1]);
            } else {
                channel = new Channel(lineParts[1], lineParts[0]);
                tuner.addChannel(channel);
            }
        }
    }

    private void changeStream(final CountDownTimer timer, final Tuner tuner, final Channel channel) {
        timer.cancel();
        videoView.setVideoURI(Uri.parse(channel.getCurrentStream()));
        final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this, getString(R.string.tuner), getString(R.string.tuning) + " " + channel.getDescription() + "...", true);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                progressDialog.dismiss();
                finishStreamChange(mp, channel, timer, tuner);
            }
        });
    }

    private void finishStreamChange(MediaPlayer mp, Channel currentChannel, CountDownTimer timer, Tuner tuner) {
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
