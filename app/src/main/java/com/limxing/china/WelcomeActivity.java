package com.limxing.china;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.limxing.library.utils.FileUtils;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by limxing on 16/4/10.
 */
public class WelcomeActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        File file = new File(FileUtils.getExternalStoragePath() + "country.xls");
        if (!file.exists()) {
            FileUtils.copyFromAssets(this, "country.xls", "country.xls");
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent=new Intent(WelcomeActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        },3000);

    }
}
