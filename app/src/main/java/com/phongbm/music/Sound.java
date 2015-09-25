package com.phongbm.music;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import com.phongbm.ahihi.R;

public class Sound {
    public static boolean PLAY_SOUND = true;
    private SoundPool soundPool;
    private Context context;
    private AudioManager audioManager;
    private final static int MAX_SOUNDS = 10;
    private float lengthMusic;
    private float volume;
    private int[] soundID = new int[]{R.raw.message_sent, R.raw.message_tone};

    public Sound(Context context, float volumeValue) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(MAX_SOUNDS)
                    .build();
        } else {
            soundPool = new SoundPool(MAX_SOUNDS, AudioManager.STREAM_MUSIC, 0);
        }
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        lengthMusic = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        lengthMusic = lengthMusic * 1.0F / audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        this.context = context;
        this.loadSound();
        volume = lengthMusic / 2;
        this.setVolume(volumeValue);
    }

    public void loadSound() {
        for (int i = 0; i < soundID.length; i++) {
            soundID[i] = soundPool.load(context, soundID[i], i + 1);
        }
    }

    public void setVolume(float volumeValue) {
        volume = volumeValue * lengthMusic;
    }

    public void playMessageSent() {
        if (PLAY_SOUND) {
            soundPool.play(soundID[0], volume, volume, 1, 0, 1.0F);
        }
    }

    public void playMessageTone() {
        if (PLAY_SOUND) {
            soundPool.play(soundID[1], volume, volume, 1, 0, 1.0F);
        }
    }

}