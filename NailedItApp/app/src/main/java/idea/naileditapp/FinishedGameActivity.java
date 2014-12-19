package idea.naileditapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import com.facebook.*;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.*;

public class FinishedGameActivity extends Activity {

    private ImageView videoIv;
    private TextView scoreTv;
    private LinearLayout answersLl;
    private String videoPath;
    private LoginButton loginButton;
    private Session session;
    private List<Card> playedCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finished_game_activity);
        scoreTv = (TextView) findViewById(R.id.scoreTv);
        videoIv = (ImageView) findViewById(R.id.videoIv);
        videoPath = getIntent().getStringExtra("video");
        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoPath,
                MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
        videoIv.setImageBitmap(thumbnail);
        answersLl = (LinearLayout) findViewById(R.id.answersLl);
        scoreTv.setText(getIntent().getStringExtra("score"));
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Card>>(){}.getType();
        playedCards = gson.fromJson(getIntent().getStringExtra("cards"), listType);
        LayoutInflater inflater = (LayoutInflater) getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        for (Card card : playedCards) {
            TextView textView = (TextView) inflater.inflate(R.layout.answer_text, null, false);
            textView.setText(card.getWord());
            if (card.isResult()) {
                textView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                textView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
            answersLl.addView(textView);
        }
        loginButton = (LoginButton) findViewById(R.id.authButton);
        // loginButton.setReadPermissions("public_profile", "user_videos");
        loginButton.setPublishPermissions("publish_actions");

        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(this);
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
            }
        }
        if (session != null && session.isOpened()) {
            // If the session is open, make an API call to get user data
            // and define a new callback to handle the response
            Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    // If the response is successful
                    if (session == Session.getActiveSession()) {
                        if (user != null) {
                            onLoggedIn(user.getId());
                        }
                    }
                }
            });
            Request.executeBatchAsync(request);
        } else {
        }
    }

    public void onShareClick(View view) {
  /*      Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        Uri screenshotUri = Uri.parse(videoPath);

        sharingIntent.setType("video/mp4");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
        startActivity(Intent.createChooser(sharingIntent, "Share image using"));*/
        File file=new File(videoPath);
        try {
            Request audioRequest = Request.newUploadVideoRequest(session, file, new Request.Callback() {

                @Override
                public void onCompleted(Response response) {
                    // TODO Auto-generated method stub

                    if(response.getError()==null)
                    {
                        Toast.makeText(FinishedGameActivity.this, "Video Shared Successfully", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(FinishedGameActivity.this, response.getError().getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            audioRequest.executeAsync();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void onLoggedIn(String uid) {

    }

    private void longLog(String string) {
        int maxLogSize = 3000;
        for(int i = 0; i <= string.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i+1) * maxLogSize;
            end = end > string.length() ? string.length() : end;
            Log.v("Result", string.substring(start, end));
        }
    }

    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState sessionState, Exception e) {

        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    public void videoOnClick(View view) {
        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra("video", videoPath);
        Type listType = new TypeToken<List<Card>>() {}.getType();
        Gson gson = new Gson();
        intent.putExtra("cards", gson.toJson(playedCards, listType));
        startActivity(intent);
    }

}
