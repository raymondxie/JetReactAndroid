package club.makeable.jetreactscore;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;

/**
 * Created by yuhxie on 11/2/16.
 */

public class GameSound {
    protected static SoundPool soundPool = null;
    protected static AudioManager audioManager = null;
    protected static HashMap<String, Integer> sounds = new HashMap<String, Integer>();

    public static void prepare(Context ctx) {
        audioManager = (AudioManager)ctx.getSystemService(Context.AUDIO_SERVICE);
        soundPool = new SoundPool(5, AudioManager.STREAM_ALARM, 0);

        int startId = soundPool.load(ctx, R.raw.start, 1);
        int endId = soundPool.load(ctx, R.raw.end, 1);
        int pointId = soundPool.load(ctx, R.raw.point, 1);
        int minusId = soundPool.load(ctx, R.raw.minus, 1);
        int bgId = soundPool.load(ctx, R.raw.background, 1);

        sounds.put("start", startId);
        sounds.put("end", endId);
        sounds.put("point", pointId);
        sounds.put("minus", minusId);
        sounds.put("background", bgId);

    }

    public static void playStartGame() {
        play("start");
    }

    public static void playEndGame() {
        play("end");
    }

    public static void playAddPoint() {
        play("point");
    }

    public static void playMinusPoint() {
        play("minus");
    }

    public static void playBackground() {
        play("background");
    }

    private static void play(String soundName) {
        float vol = audioManager.getStreamVolume(
                AudioManager.STREAM_ALARM);
        float maxVol = audioManager.getStreamMaxVolume(
                AudioManager.STREAM_ALARM);

        // float leftVolume = val/maxVol;
        float leftVolume = 0.4f;
        float rightVolume = 0.4f;


        int priority = 1;
        int no_loop = 0;
        float normal_playback_rate = 1f;
        soundPool.play(sounds.get(soundName),
                leftVolume,
                rightVolume,
                priority,
                no_loop,
                normal_playback_rate);
    }

    public static void stop() {

    }
}
