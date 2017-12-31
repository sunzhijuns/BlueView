package com.szj.blueview;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;

/**
 * Created by sunzhijun on 2018/1/1.
 */

public class SoundActivity extends Activity {
    SoundPool sp;
    HashMap<Integer,Integer> hm;
    int currStreamId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);
        initSoundPool();
        Button b1 = findViewById(R.id.Button01);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(1,0);
            }
        });
        Button b2 = findViewById(R.id.Button02);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.stop(currStreamId);
                Toast.makeText(SoundActivity.this, "停止播放即时音效", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playSound(int sound, int loop) {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        float streamVolumeCurrent = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float streamVolumeMax = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = streamVolumeCurrent/streamVolumeMax;
        currStreamId = sp.play(hm.get(sound),volume,volume,1,loop,1.0f);

    }

    private void initSoundPool() {
        sp = new SoundPool(4, AudioManager.STREAM_MUSIC,0);
        hm = new HashMap<Integer,Integer>();
        hm.put(1,sp.load(this,R.raw.musictest,1));
    }
}
