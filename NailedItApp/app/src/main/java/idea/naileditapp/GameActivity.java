package idea.naileditapp;

import android.app.Activity;
import android.content.*;
import android.hardware.*;
import android.media.*;
import android.os.*;
import android.view.View;
import android.widget.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;


public class GameActivity extends Activity implements SensorEventListener {

    private static final int GUESS = 0;
    private static final int CORRECT = 1;
    private static final int PASS = 2;
    public static final int CORRECT_ANGLE = -7;
    public static final int PASS_ANGLE = 9;

    private Camera camera;
    private CameraPreview cameraPreview;

    private MediaRecorder mediaRecorder = new MediaRecorder();

    private MediaPlayer correctPlayer;
    private MediaPlayer wrongPlayer;
    private MediaPlayer countDownPlayer;
    private MediaPlayer finishedPlayer;
    private SensorManager sensorManager;
    private Sensor sensor;

    private FrameLayout cameraFl;
    private TextView stateTv;
    private TextView timeTv;
    private View overlayView;

    private int state;
    private boolean gameStarted = false;

    private int time = 10;

    private Handler handler = new Handler();

    private List<Card> cards = new ArrayList<Card>(10);
    private List<Card> playedCards = new ArrayList<Card>(10);
    private boolean enableStateChange = false;

    private Random random = new Random();

    private boolean recording = false;

    private String video;
    private boolean countDownStarted = false;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);
        cameraFl = (FrameLayout) findViewById(R.id.cameraFl);
        stateTv = (TextView) findViewById(R.id.stateTv);
        timeTv = (TextView) findViewById(R.id.timeTv);
        overlayView = findViewById(R.id.overlayoutView);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        cameraPreview = new CameraPreview(this, camera);
        cameraFl.addView(cameraPreview);
        correctPlayer = MediaPlayer.create(this, R.raw.right);
        wrongPlayer = MediaPlayer.create(this, R.raw.pass);
        countDownPlayer = MediaPlayer.create(this, R.raw.countdown);
        finishedPlayer = MediaPlayer.create(this, R.raw.finished);
        for (String animal : getResources().getStringArray(getIntent().getIntExtra("listId", 0))) {
            cards.add(new Card(animal));
        }
        handler.postDelayed(stateChangeRunnable, 1000);
    }

    private void startRecording() {
        try {
            recording = true;
            camera.unlock();
            mediaRecorder.setCamera(camera);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setProfile(CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_FRONT, CamcorderProfile.QUALITY_HIGH));
            setOutputMediaFile();
            mediaRecorder.setOutputFile(video);
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (recording) {
            mediaRecorder.stop();
            mediaRecorder.release();
        }
        camera.lock();
        camera.stopPreview();
        camera.release();
    }

    private void setOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Sharades");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        video = new File(mediaStorageDir.getPath() + File.separator +
                "VID_" + timeStamp + ".mp4").toString();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float rollAngle = event.values[2];
        if (enableStateChange) {
            if (gameStarted) {
                enableStateChange = false;
                handler.postDelayed(stateChangeRunnable, 500);
                int oldState = state;
                if (rollAngle < CORRECT_ANGLE) {
                    state = CORRECT;
                } else if (rollAngle > PASS_ANGLE) {
                    state = PASS;
                } else {
                    state = GUESS;
                }
                if (oldState != state) {
                    if (state == GUESS) {
                        overlayView.setBackgroundColor(getResources().getColor(R.color.transparent_blue));
                        int number = random.nextInt(cards.size());
                        Card card = cards.get(number);
                        cards.remove(number);
                        playedCards.add(card);
                        stateTv.setText(playedCards.get(playedCards.size() - 1).getWord());
                    } else if (state == PASS) {
                        wrongPlayer.start();
                        overlayView.setBackgroundColor(getResources().getColor(R.color.transparent_red));
                        stateTv.setText("Pass");
                        playedCards.get(playedCards.size() - 1).setResult(false);
                        playedCards.get(playedCards.size() - 1).setTime(System.currentTimeMillis() - startTime);
                        startTime = System.currentTimeMillis();
                    } else {
                        correctPlayer.start();
                        overlayView.setBackgroundColor(getResources().getColor(R.color.transparent_green));
                        stateTv.setText("Correct");
                        playedCards.get(playedCards.size() - 1).setResult(true);
                        playedCards.get(playedCards.size() - 1).setTime(System.currentTimeMillis() - startTime);
                        startTime = System.currentTimeMillis();
                    }
                }
            } else {
                if (rollAngle < -1) {
                } else if (rollAngle > 1) {
                } else {
                    if (countDownStarted == false) {
                        countDownStarted = true;
                        stateTv.setText("Get Ready!");
                        handler.post(countDownRunnable);
                    }
                }
            }
        }


    }

    private void startGame() {
        startTime = System.currentTimeMillis();
        int number = random.nextInt(cards.size());
        Card card = cards.get(number);
        cards.remove(number);
        playedCards.add(card);
        stateTv.setText(playedCards.get(playedCards.size() - 1).getWord());
        timeTv.setText(getTime());
        gameStarted = true;
        startRecording();
        handler.postDelayed(stateChangeRunnable, 500);
        handler.postDelayed(timeRunnable, 500);
    }

    private String getTime() {
        if (time > 59) {
            return "1:00";
        } else if (time < 10) {
            return "0:0" + time;
        } else {
            return "0:" + time;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private Runnable countDownRunnable = new Runnable() {

        private int countdown = 3;

        @Override
        public void run() {
            if (countdown == 3) {
                countDownPlayer.start();
            }
            if (countdown == 0) {
                startGame();
            } else {
                timeTv.setText(String.valueOf(countdown));
                countdown--;
                handler.postDelayed(this, 1000);
            }
        }
    };

    private Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            time--;
            timeTv.setText(getTime());
            if (time == 0) {
                int correct = 0;
                for (Card card : playedCards) {
                    if (card.isResult()) {
                        correct++;
                    }
                }
                sensorManager.unregisterListener(GameActivity.this);
                wrongPlayer.release();
                correctPlayer.release();
                countDownPlayer.release();
                stopRecording();
                final Intent intent = new Intent(GameActivity.this, FinishedGameActivity.class);
                intent.putExtra("video", video);
                intent.putExtra("score", "You got " + correct + " out of " + playedCards.size() + " correct");
                Type listType = new TypeToken<List<Card>>() {}.getType();
                Gson gson = new Gson();
                intent.putExtra("cards", gson.toJson(playedCards, listType));
                finishedPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        finishedPlayer.release();
                        startActivity(intent);
                        finish();
                    }
                });
                finishedPlayer.start();
            } else {
                handler.postDelayed(this, 1000);
            }
        }
    };

    private Runnable stateChangeRunnable = new Runnable() {
        @Override
        public void run() {
            enableStateChange = true;
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onBackPressed() {
        wrongPlayer.release();
        correctPlayer.release();
        countDownPlayer.release();
        stopRecording();
        handler.removeCallbacks(stateChangeRunnable);
        handler.removeCallbacks(timeRunnable);
        handler.removeCallbacks(countDownRunnable);
        super.onBackPressed();
    }
}