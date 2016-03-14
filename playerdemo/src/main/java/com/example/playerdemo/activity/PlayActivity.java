package com.example.playerdemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.lecloud.lecloudsdkdemo.R;
import com.lecloud.leutils.LeLog;
import com.lecloud.log.LogDog;
import com.letv.simple.utils.LetvParamsUtils;
import com.letv.simple.utils.LetvSimplePlayBoard;
import com.letv.skin.v4.V4PlaySkin;
import com.letv.universal.iplay.ISplayer;

public class PlayActivity extends Activity {
    public final static String DATA = "data";

    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    private V4PlaySkin skin;
    private LetvSimplePlayBoard playBoard;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        loadDataFromIntent();// load data
        skin = (V4PlaySkin) findViewById(R.id.videobody);

        LeLog.clearLogCate();

        playBoard = new LetvSimplePlayBoard();
        playBoard.init(this, bundle, skin);

        initBtn();
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            bundle = intent.getBundleExtra("data");
            if (bundle == null) {
                Toast.makeText(this, "no data", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playBoard != null) {
            playBoard.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (playBoard != null) {
            playBoard.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playBoard != null) {
            playBoard.onDestroy();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (playBoard != null) {
            playBoard.onConfigurationChanged(newConfig);
        }
    }

    int count = 0;

    private void initBtn() {
        findViewById(R.id.btn2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playBoard != null) {
                    ISplayer player = playBoard.getPlayer();
                    if (player != null) {
                        player.stop();
                        String uuid = "";
                        String vuid = "";
                        
                        if(count>65535){
                            count=0;
                        }
                        count++;
                        if (count % 2 == 0) {
                            uuid = "7a4f55c18a";
                            vuid = "769312c218";
                        } else {
                            uuid = "cd5f283012";
                            vuid = "323dd5d802";
                        }
                        player.setParameter(player.getPlayerId(), LetvParamsUtils.setVodParams(uuid, vuid, "", "151398", "", false));
                        player.prepareAsync();
                    }
                }
            }
        });
    }
}
