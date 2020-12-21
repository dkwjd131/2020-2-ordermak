package org.techtown.ordermak.activity;

import java.util.ArrayList;
import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.techtown.ordermak.R;
import org.techtown.ordermak.data.KeywordData;
import org.techtown.ordermak.data.KeywordResponse;
import org.techtown.ordermak.data.categoryData;
import org.techtown.ordermak.network.RetrofitClient;
import org.techtown.ordermak.network.ServiceApi;

import static android.speech.tts.TextToSpeech.ERROR;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class menu_keyword extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    GestureDetector gDetector;
    TextToSpeech tts;
    TextView textView4;
    TextView sttTextView;
    String uuid;
    String userID;

    Intent SttIntent;
    SpeechRecognizer mRecognizer;

    private ServiceApi service;

    private ProgressBar mProgressView;

    //퍼미션 상수 정의
    final int PERMISSION=1;

    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_MAX_OFF_PATH = 500;
    private static final int SWIPE_THRESHOLD_VELOCITY = 50;

    private static final String notice="두 손가락으로 화면을 한번 터치하면 음성인식이 시작됩니다. 검색하려는 키워드를 말해주세요. 이전화면 돌아가기는 왼쪽 드래그 해주세요";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_keyword);


        service = RetrofitClient.getClient().create(ServiceApi.class);
        //퍼미션 체크
        if(Build.VERSION.SDK_INT>=23){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO},PERMISSION);
        }

        textView4=findViewById(R.id.textView4);


        sttTextView=findViewById(R.id.SttTextView);

        //음성인식 객체
        //사용자에게 음성을 요구하고 음성 인식기를 통해 전송하는 활동을 시작
        SttIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //음성인식을 위한 음성 인식기의 의도에 사용되는 여분의 키
        SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        //음성을 번역할 언어를 설정
        SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
        //SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US");

        mRecognizer=SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(recognitionListener);

        this.gDetector=new GestureDetector(this,this);
        //더블 탭 이벤트를 감지하고자 seOnDoubleTap() 메소드 호출
        gDetector.setOnDoubleTapListener(this);

        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
                tts.speak(textView4.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
                try{
                    TimeUnit.SECONDS.sleep(3);
                }catch (InterruptedException e){
                    //Toast.makeText(getApplicationContext(),"여기서 에러!!",Toast.LENGTH_SHORT).show();
                }

                tts.speak(notice,TextToSpeech.QUEUE_FLUSH,null);
            }
        });

        Intent getIntent=getIntent();
        uuid= getIntent.getStringExtra("uuid");
        userID= getIntent.getStringExtra("userID");

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

    //화면 재시작할때 음성인식 다시 시작
    @Override
    protected void onRestart() {
        super.onRestart();
        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
                tts.speak(textView4.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
                try{
                    TimeUnit.SECONDS.sleep(3);
                }catch (InterruptedException e){
                    //Toast.makeText(getApplicationContext(),"여기서 에러!!",Toast.LENGTH_SHORT).show();
                }

                tts.speak(notice,TextToSpeech.QUEUE_FLUSH,null);
            }
        });
    }

    //액티비티 종료시 코드
    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.stop();
        tts.shutdown();
    }

    private RecognitionListener recognitionListener=new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            tts.stop();
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int error) {


        }

        @Override
        public void onResults(Bundle results) {

            String key=" ";
            key=SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult=results.getStringArrayList(key);

            String[] rs=new String[mResult.size()];
            mResult.toArray(rs);

            sttTextView.setText(rs[0]);


            KeywordData keywordData=new KeywordData(rs[0]);
            categoryData cd = new categoryData(uuid, rs[0]);


            service.keywordSearch(cd).enqueue(new Callback<KeywordResponse>() {
                @Override
                public void onResponse(Call<KeywordResponse> call, Response<KeywordResponse> response) {
                    final KeywordResponse result=response.body();

                    ArrayList<Map<String,String>> arr=(ArrayList) result.getMessage();

                    //String temp=arr.get(0).toString();
                    //Toast.makeText(menu_keyword.this,temp,Toast.LENGTH_LONG).show();


                    Intent sendIntent=new Intent(menu_keyword.this,menu_keyword_result.class);
                    sendIntent.putExtra("info", arr);
                    sendIntent.putExtra("uuid",uuid);
                    sendIntent.putExtra("userID",userID);
                    startActivity(sendIntent);





                    /*
                    tts=new TextToSpeech(menu_keyword.this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != ERROR)
                                tts.setLanguage(Locale.KOREAN);
                            tts.speak(result.getMessage(),TextToSpeech.QUEUE_FLUSH,null);
                        }
                    });

                     */
                    //showProgress(false);
                }

                @Override
                public void onFailure(Call<KeywordResponse> call, Throwable t) {

                    //Toast.makeText(menu_keyword.this,"에러 발생",Toast.LENGTH_SHORT).show();
                    t.printStackTrace();
                    //showProgress(false);
                }
            });


        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        tts.stop();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
        mRecognizer.startListening(SttIntent);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;

            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                //Toast.makeText(getApplicationContext(), "Left Swipe", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, menu_basic.class);
                intent.putExtra("uuid",uuid);
                intent.putExtra("userID",userID);
                startActivity(intent);

            }
            // left to right swipe
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                //Toast.makeText(getApplicationContext(), "Right Swipe", Toast.LENGTH_SHORT).show();

            }
            // down to up swipe
            else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                //Toast.makeText(getApplicationContext(), "Swipe up", Toast.LENGTH_SHORT).show();
            }
            // up to down swipe
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                //Toast.makeText(getApplicationContext(), "Swipe down", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {

        }
        return true;
    }

    /*
    private void showProgress(boolean show){
        mProgressView.setVisibility(show? View.VISIBLE:View.GONE);
    }
     */

}
