package com.noobjsperson.androtimer;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;

public class MainActivity extends Activity
{
    int frameRate;
    int start = -1;
    int end = -1;
    TextView timeTw;
    VideoView vw;
    MediaPlayer mediaPlayer;
    Uri uri;
    EditText fw;
    private static final int FILE_SELECT_CODE = 0;

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final TextView tw = findViewById(R.id.mainTextView);
        final TextView startTw = findViewById(R.id.startTextView);
        final TextView endTw = findViewById(R.id.endTextView);
        Button startButton = findViewById(R.id.startButton);
        Button endButton = findViewById(R.id.endButton);
        Button chooseButton = findViewById(R.id.chooseVideoButton);
        timeTw = findViewById(R.id.timeTextView);
        fw = findViewById(R.id.finalTextView);
        vw = findViewById(R.id.mainVideoView1);
        vw.setMediaController(new MediaController(this));
        vw.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){

            @Override
            public void onPrepared(MediaPlayer mp)
            {
                mediaPlayer = mp;
            }


        });
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        //noinspection BusyWait
                        Thread.sleep(20); // every 10s
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tw.setText(MessageFormat.format("Current Position: {0}\nCurrent Frame: {1}\nframerate: {2}", vw.getCurrentPosition(), Math.round((double) vw.getCurrentPosition() / 1000 * frameRate), frameRate));
                            }
                        });
                    }
                } catch (InterruptedException ignored) {
                }
            }
        };
        t.start();
        startButton.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View p1)
            {
                start = vw.getCurrentPosition();
                Log.d("START", ""+start);
                startTw.setText(formatTime(start));
                updateTotalTime();
            }


        });
        endButton.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View p1)
            {
                end = vw.getCurrentPosition();
                Log.d("END", ""+end);
                endTw.setText(formatTime(end));
                updateTotalTime();
            }


        });
        chooseButton.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View p1)
            {
                showFileChooser();
            }

        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == FILE_SELECT_CODE){
            if(data == null) return;
            uri = data.getData();
            vw.setVideoURI(uri);
            vw.start();
            MediaExtractor extractor = new MediaExtractor();
            try {
                //Adjust data source as per the requirement if file, URI, etc.
                extractor.setDataSource(this, uri, Collections.EMPTY_MAP);
                int numTracks = extractor.getTrackCount();
                for (int i = 0; i < numTracks; ++i) {
                    MediaFormat format = extractor.getTrackFormat(i);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    if (mime.startsWith("video/")) {
                        if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                            frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                //Release stuff
                extractor.release();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public void seekTo(View view){
        Button button = (Button) view;
        int stepBy = Integer.parseInt(button.getText().toString());
        vw.pause();
        int currentFrame = Math.round((float)vw.getCurrentPosition() / 1000 * frameRate);
        int amount = (int)Math.ceil((double)(currentFrame + stepBy) / frameRate * 1000);
//        Log.d("STEPBY",""+stepBy);
//        Log.d("CURRENTFRAME",""+currentFrame);
//        Log.d("AMOUNT",""+amount);
        if(mediaPlayer == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            mediaPlayer.seekTo(amount,MediaPlayer.SEEK_CLOSEST);
        else
            mediaPlayer.seekTo(amount); // Polyfill for api versions lower than 26 (android versions lower than 8), seeking might be inaccurate if you fall into this category
    }

    private void setClipboard(String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }

    public String formatTime(int duration){
        int hours = duration / 3600000;

        int minutes = (duration % 3600000) / 60000;
        double seconds = (double) duration % 60000 / 1000;

        String minutesStr = String.valueOf(minutes < 10 ? "0" + minutes : minutes);
        String secondsStr = String.valueOf(seconds < 10 ? "0" + seconds : seconds);

        return hours + ":" + minutesStr + ":" + secondsStr;
    }
    public void updateTotalTime(){
        if (start == -1 || end == -1 || start > end) return;
        int startFrame = Math.round((float)start / 1000 * frameRate);
        int endFrame = Math.round((float)end / 1000 * frameRate);
        int frames = endFrame - startFrame;
        int ms = frames * 1000 / frameRate;
        String formatted = formatTime(ms);
        timeTw.setText(formatted);
        String modMessage = "Mod Message: time starts at "+formatTime(start)+" and ends at "+formatTime(end)+" with a framerate of "+frameRate+" fps to get a final time of "+formatted;
        fw.setText(modMessage);
    }
    public void copyModMessage(View view){
        setClipboard(fw.getText().toString());
    }
}
