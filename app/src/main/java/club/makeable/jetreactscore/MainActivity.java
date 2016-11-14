package club.makeable.jetreactscore;

import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements MqttCallback {
    final static String TAG = MainActivity.class.getSimpleName();

    private MQTTClient  mqttClient;
    private TextView    scoreView;
    private TextView    playerView;
    private Button      saveButton;
    private TextView    scoreHintView;
    private ListView    leaderView;
    private int         gameState = 0;  // 0 - not started, 1 - started


    private Handler     mHandler = new Handler(Looper.getMainLooper());
    private GestureDetectorCompat mDetector;
    private MediaPlayer mediaPlayer;

    private ArrayList<GameScore> scores = new ArrayList<GameScore>();
    ScoreAdapter leaderAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDetector = new GestureDetectorCompat(this, new MyGestureListener());

        scoreView = (TextView) findViewById(R.id.scoreboard);
        Typeface custom_font = Typeface.createFromAsset(getApplicationContext().getAssets(),  "fonts/lcd24display.ttf");
        scoreView.setTypeface(custom_font);

        playerView = (TextView) findViewById(R.id.player);
        saveButton = (Button) findViewById(R.id.btnSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // push score to display
                String name = playerView.getText().toString();
                int score = Integer.parseInt(scoreView.getText().toString());
                GameScore game = new GameScore(name, score);
                scores.add(game);

                Collections.sort(scores);
                leaderAdapter.notifyDataSetChanged();

                // push score to backend
                Log.d(TAG, "saving game score to backend");
                saveGameScore(game);

                //reset name
                playerView.setText("");
            }
        });
        scoreHintView = (TextView) findViewById(R.id.scorehint);

        leaderView = (ListView) findViewById(R.id.leaderboard);
        leaderAdapter = new ScoreAdapter(this, R.layout.score_row, scores);
        leaderView.setAdapter(leaderAdapter);

        Collections.sort(scores);
        leaderAdapter.notifyDataSetChanged();

        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // fetch all GameScore
        getAllGameScores();

        mqttClient = new MQTTClient();
        mqttClient.setup(this);

        Log.d(TAG, "hide system status");
        cleanView();

        GameSound.prepare(this);

        mediaPlayer = MediaPlayer.create(this, R.raw.background);
        mediaPlayer.setLooping(true);
    }

    @Override
    protected void onPause() {
        mqttClient.disconnect();
        mqttClient = null;

        mediaPlayer.release();

        super.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private void cleanView() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);
    }

    private void saveGameScore(GameScore score) {
        RequestParams params = new RequestParams();
        params.put("name", score.getName());
        params.put("score", score.getScore());

        ApexClient.post("/pls/apex/makeable/jetreact/score/", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d(TAG, "Game score saved. Status=" + statusCode);

                playerView.setVisibility(View.INVISIBLE);
                saveButton.setVisibility(View.INVISIBLE);

                scoreView.setText(String.valueOf(0));
                scoreHintView.setVisibility(View.VISIBLE);
                scoreHintView.setText("Game not started");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG, "Failed to save game score: " + statusCode);
            }
        });
    }

    private void getAllGameScores() {
        ApexClient.get("/pls/apex/makeable/jetreact/score/", null, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try {
                    Log.d(TAG, "status: " + statusCode);

                    JSONArray items = response.getJSONArray("items");
                    if( items != null && items.length() > 0 ) {
                        // We got score list, clear any existing
                        scores.clear();

                        String name ="";
                        int score = 0;
                        for(int i=0; i< items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            try {
                                name = item.getString("name");
                                score = item.getInt("score");
                            }
                            catch(JSONException je) {
                                Log.d(TAG, "invalid entry of game score");
                            }

                            if( name != null && !"".equalsIgnoreCase(name)) {
                                GameScore gameScore = new GameScore(name, score);
                                scores.add(gameScore);
                            }
                        }

                        Collections.sort(scores);
                        leaderAdapter.notifyDataSetChanged();
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }

            }

        });
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString() );
            cleanView();

            return true;
        }
    }

    //
    // MQTT callback
    //
    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "Mqtt connection lost. Reconnecting...");
        mqttClient.reconnect();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String content = message.toString().trim();
        Log.d(TAG, "Received MQTT message: " + topic + " | " + content );

        if( content.startsWith("point") && gameState == 1) {
            // point=100, or point=-40
            String value = content.substring("point=".length());
            int point = Integer.parseInt(value);

            // current score
            int score = Integer.parseInt(scoreView.getText().toString());

            score = score + point;
            if( score < 0 ) {
                score = 0;
            }

            final int updatedScore = score;
            if(point > 0) {
                GameSound.playAddPoint();
            }
            else if( point < 0 ) {
                GameSound.playMinusPoint();
            }


            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Setting new score: " + updatedScore);
                    scoreView.setText(String.valueOf(updatedScore));
                }
            });

        }
        else if( content.startsWith("game")) {
            // game=started, or game=ended
            String value = content.substring("game=".length());
            Log.d(TAG, "game: " + value );
            if("started".equalsIgnoreCase(value)) {
                GameSound.playStartGame();

                gameState = 1;

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scoreView.setText(String.valueOf(0));
                        playerView.setVisibility(View.INVISIBLE);
                        saveButton.setVisibility(View.INVISIBLE);
                        scoreHintView.setVisibility(View.INVISIBLE);
                    }
                });

                // start background music
                mediaPlayer.start();
            }
            else if("ended".equalsIgnoreCase(value)) {
                GameSound.playEndGame();

                gameState = 0;

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // show fields for input
                        playerView.setVisibility(View.VISIBLE);
                        saveButton.setVisibility(View.VISIBLE);
                        scoreHintView.setVisibility(View.VISIBLE);
                        scoreHintView.setText("Game Over");
                    }
                });
                // stop background music
                mediaPlayer.pause();
            }
        }
        else {
            Log.d(TAG, "Message unexpected");
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "MQTT message delivery confirmed!");
    }
}
