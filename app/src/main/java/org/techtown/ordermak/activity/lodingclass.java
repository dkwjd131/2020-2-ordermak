package org.techtown.ordermak.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import org.techtown.ordermak.R;

public class lodingclass extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loadingclass);

        Handler handler=new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run(){
                Intent intent;
                intent = new Intent(getBaseContext(),myAuthActivity.class);
                startActivity(intent);
                finish();
            }
        },2000);

    }
}
