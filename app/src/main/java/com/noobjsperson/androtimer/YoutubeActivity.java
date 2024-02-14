package com.noobjsperson.androtimer;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeActivity extends Activity {
    String api_key = "AIzaSyBl60XtWxwnwUlrRs5mTozKkz0ZMEQjI9U";
    private void switchToMainActivity() {
        Intent switchActivityIntent = new Intent(this, MainActivity.class);
        startActivity(switchActivityIntent);
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);
        YouTubePlayerFragment YoutubeFragment = new YouTubePlayerFragment();
        FragmentManager manager = getFragmentManager();
        Button loadButton = findViewById(R.id.chooseVideoButton);
        final YouTubePlayer[] ytPlayer = new YouTubePlayer[1];
        EditText urlText = findViewById(R.id.urltext);
        Button goBackButton = findViewById(R.id.goback);
        manager.beginTransaction()
                .replace(R.id.ytPlayer, YoutubeFragment)
                .addToBackStack(null)
                .commit();
        YoutubeFragment.initialize(api_key, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                ytPlayer[0] = youTubePlayer;
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.d("Initialization Error","Youtube Player Failed to Load");
            }
        });
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToMainActivity();
            }
        });
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pattern pattern = Pattern.compile("youtu\\.be/(.*?)(?:\\?|&|\\/|$)");
                Log.d("Regexp", pattern.pattern());
                Log.d("Match Input", String.valueOf(urlText.getText()));
                Matcher matcher = pattern.matcher(String.valueOf(urlText.getText()));
                if (matcher.find()){
                    String ytId = matcher.group(1);
                    Log.d("Match Group", ytId);
                    ytPlayer[0].loadVideo(ytId);
                    ytPlayer[0].play();
                } else {
                    Log.d("Error","Not Matched");
                }
            }
        });

    }
}