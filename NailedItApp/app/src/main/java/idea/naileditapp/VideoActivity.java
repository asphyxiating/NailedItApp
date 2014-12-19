package idea.naileditapp;

import android.app.Activity;
import android.os.*;
import android.widget.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class VideoActivity extends Activity {

    private TextView wordTv;
    private Handler handler = new Handler();
    private List<Card> playedCards;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_activity);
        VideoView videoView = (VideoView) findViewById(R.id.videoView);
        wordTv = (TextView) findViewById(R.id.wordTv);
        String videoPath = getIntent().getStringExtra("video");
        videoView.setVideoPath(videoPath);
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Card>>() {
        }.getType();
        playedCards = gson.fromJson(getIntent().getStringExtra("cards"), listType);
        videoView.start();
        wordTv.setText(playedCards.get(count).getWord());
        if (playedCards.get(count).isResult()) {
            wordTv.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            wordTv.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        handler.postDelayed(runnable, playedCards.get(count).getTime());
    }

    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            count++;
            wordTv.setText(playedCards.get(count).getWord());
            handler.postDelayed(this, playedCards.get(count).getTime());
            if (playedCards.get(count).isResult()) {
                wordTv.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                wordTv.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        }
    };
}
