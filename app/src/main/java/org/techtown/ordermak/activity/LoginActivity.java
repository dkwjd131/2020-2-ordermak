package org.techtown.ordermak.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.techtown.ordermak.MainActivity;
import org.techtown.ordermak.R;
import org.techtown.ordermak.data.LoginData;
import org.techtown.ordermak.data.LoginResponse;
import org.techtown.ordermak.network.RetrofitClient;
import org.techtown.ordermak.network.ServiceApi;
import org.techtown.ordermak.data.PreferencManager;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.speech.tts.TextToSpeech.ERROR;

public class LoginActivity extends AppCompatActivity {

    private TextToSpeech tts;

    private EditText mIDView; //아이디입력 칸
    private EditText mPwdView; //비밀번호입력 칸
    private Button mIDLoginButton;
    private Button mJoinButton;
    private ProgressBar mProgressView;
    private ServiceApi service;
    private CheckBox autoLogin;

    //자동로그인에 필요한 객체
   SharedPreferences setting;
   SharedPreferences.Editor editor;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mIDView=(EditText)findViewById(R.id.login_id);
        mPwdView=(EditText)findViewById(R.id.login_password);
        mIDLoginButton=(Button)findViewById(R.id.login_button);
        mJoinButton=(Button)findViewById(R.id.join_button);
        mProgressView=(ProgressBar)findViewById(R.id.login_progress);
        autoLogin=(CheckBox)findViewById(R.id.autoLoginCheck);


        //서버 통신 위한 객체
        service = RetrofitClient.getClient().create(ServiceApi.class);

        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!=ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
                tts.speak("로그인 화면입니다. 아이디가 없다면 회원가입 해주세요.",TextToSpeech.QUEUE_FLUSH,null);
            }
        });


        //로그인 버튼 누를시 attemptLogin() 함수 호출
        mIDLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //이게 원래 코드
                attemptLogin();

            }
        });
        //회원가입 버튼 누를시 회원가입 activity로 이동
        mJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),JoinActivity.class);
                startActivity(intent);
            }
        });
   
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //로그인 버튼 눌렀을 떄 호출되는 함수
    private void attemptLogin(){
        mIDView.setError(null);
        mPwdView.setError(null);


        String id=mIDView.getText().toString();
        String pwd=mPwdView.getText().toString();


        startLogin(new LoginData(id,pwd));
        showProgress(true);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!=ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
                tts.speak("로그인 화면입니다. 아이디가 없다면 회원가입 해주세요.",TextToSpeech.QUEUE_FLUSH,null);
            }
        });
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



    //로그인 실행 함수
    private void startLogin(final LoginData data){

        service.userLogin(data).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                final LoginResponse result=response.body();

                String message=result.getMessage();

                //Toast.makeText(LoginActivity.this, message,Toast.LENGTH_LONG).show();

                //계정 일치했을때 만 로그인화면에서 홈화면으로 전환되도록 하는 코드
                if(result.getCode()==200) {
                    Toast.makeText(LoginActivity.this,message, Toast.LENGTH_SHORT).show();


                    if(autoLogin.isChecked()){
                        FirstAuth.setUserName(LoginActivity.this,data.getUserID(),data.getUserPwd());
                    }
                    //로그인 성공 메시지 음성으로 출력 하는 코드
                    tts = new TextToSpeech(LoginActivity.this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != ERROR)
                                tts.setLanguage(Locale.KOREAN);
                            tts.speak(result.getMessage().toString(), TextToSpeech.QUEUE_FLUSH, null);
                        }


                    });
                    showProgress(false);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    //로그인 성공시 아이디 주문하기 화면으로 넘겨주기
                    intent.putExtra("ID", data.getUserID());
                    //화면 전환 액티비
                    startActivity(intent);
                    //임의로 추가한 코드(이게 들어가는게 맞는지 모르겠네)
                    finish();
                }

                //result.getCode()==204는 존재하지 않는 계정일 때
                else if(result.getCode()==204) {
                    Toast.makeText(getApplicationContext(),result.getMessage(), Toast.LENGTH_LONG).show();
                    showProgress(false);
                    return;
                }

                //비밀번호 틀렸을 때 인데, nodejs 쪽에서 바꿔야 할 것 같다. 그에 맞춰서 코드 변경해야 할듯
                else if(result.getCode()==204){
                    Toast.makeText(getApplicationContext(),result.getMessage(),Toast.LENGTH_LONG).show();
                    showProgress(false);
                    return;
                }
                //result.getCode()==404는 에러 발생 코드
                else if(result.getCode()==404){
                    Toast.makeText(getApplicationContext(),result.getMessage(),Toast.LENGTH_LONG).show();
                    showProgress(false);
                    return;
                }

            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this,"로그인 에러 발생",Toast.LENGTH_SHORT).show();
                Log.e("로그인 에러 발생",t.getMessage());
                t.printStackTrace();
                showProgress(false);
            }
        });
    }
    private void showProgress(boolean show){
        mProgressView.setVisibility(show? View.VISIBLE:View.GONE);
    }

}
