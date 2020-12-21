
package org.techtown.ordermak.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import org.techtown.ordermak.MainActivity;
import org.techtown.ordermak.R;

public class myAuthActivity extends AppCompatActivity {

    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_auth);

        //자동로그인 인증
        if (FirstAuth.getUserID(myAuthActivity.this).length() == 0) {
            // call Login Activity
            intent = new Intent(myAuthActivity.this, LoginActivity.class);
            startActivity(intent);
            this.finish();
        } else {
            // Call Next Activity
            intent = new Intent(myAuthActivity.this, MainActivity.class);
            intent.putExtra("ID", FirstAuth.getUserID(this).toString());
            startActivity(intent);
            this.finish();
        }



    }
}