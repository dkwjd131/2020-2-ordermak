package org.techtown.ordermak.activity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.techtown.ordermak.R;
import org.techtown.ordermak.data.JoinData;
import org.techtown.ordermak.data.JoinResponse;
import org.techtown.ordermak.network.RetrofitClient;
import org.techtown.ordermak.network.ServiceApi;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static android.speech.tts.TextToSpeech.ERROR;

public class JoinActivity extends AppCompatActivity {

    private TextToSpeech tts;

    private EditText mNameView;
    private EditText mPhoneView;
    private EditText mIDView;
    private EditText mPwdView;
    private Button mJoinButton;
    private ProgressBar mProgressView;
    private ServiceApi service;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_join);

        mNameView=(EditText)findViewById(R.id.join_name);
        mPhoneView=(EditText)findViewById(R.id.join_phone);
        mIDView=(EditText)findViewById(R.id.join_id);
        mPwdView=(EditText)findViewById(R.id.join_pwd);
        mJoinButton=(Button)findViewById(R.id.join_button);
        mProgressView=(ProgressBar)findViewById(R.id.join_progress);

        service= RetrofitClient.getClient().create(ServiceApi.class);

        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!=ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
                tts.speak("회원가입 화면입니다.",TextToSpeech.QUEUE_FLUSH,null);
            }
        });

        mJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptJoin();
            }
        });
    }

    private void attemptJoin(){
        mNameView.setError(null);
        mPhoneView.setError(null);
        mIDView.setError(null);
        mPwdView.setError(null);

        String name=mNameView.getText().toString();
        String phone=mPhoneView.getText().toString();
        String id=mIDView.getText().toString();
        String pwd=mPwdView.getText().toString();


        startJoin(new JoinData(name,phone,id,pwd));
        showProgress(true);
    }
    private void startJoin(JoinData data){
        service.userJoin(data).enqueue(new Callback<JoinResponse>() {
            @Override
            public void onResponse(Call<JoinResponse> call, final Response<JoinResponse> response) {
                JoinResponse result=response.body();
                Toast.makeText(JoinActivity.this,result.getMessage(),Toast.LENGTH_SHORT).show();
                showProgress(false);


                if (result.getCode() == 200) {
                    finish();
                }
            }

            @Override
            public void onFailure(Call<JoinResponse> call, Throwable t) {
                Toast.makeText(JoinActivity.this,"회원가입 에러발생",Toast.LENGTH_SHORT).show();
                Log.e("회원가입 에러 발생",t.getMessage());
                t.printStackTrace();
                showProgress(false);
            }
        });
    }
    private void showProgress(boolean show){
        mProgressView.setVisibility(show? View.VISIBLE:View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        tts.stop();
        //tts.shutdown();
    }

    @Override
    protected void onPause() {
        super.onPause();
        tts.stop();
        //tts.shutdown();
    }
}
