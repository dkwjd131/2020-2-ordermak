package org.techtown.ordermak.activity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static android.speech.tts.TextToSpeech.ERROR;

import org.techtown.ordermak.R;
import org.techtown.ordermak.data.KeywordResponse;
import org.techtown.ordermak.data.LoginData;
import org.techtown.ordermak.data.StoreID;
import org.techtown.ordermak.data.categoryData;
import org.techtown.ordermak.data.orderData;
import org.techtown.ordermak.data.orderResponse;
import org.techtown.ordermak.network.RetrofitClient;
import org.techtown.ordermak.network.ServiceApi;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class order extends AppCompatActivity implements GestureDetector.OnGestureListener,
                                                        GestureDetector.OnDoubleTapListener
{
    GestureDetector gDetector;
    TextToSpeech tts;
    TextView textView5;

    //음성인식 객체
    Intent SttIntent;
    SpeechRecognizer mRecognizer;

    private ServiceApi service;

    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_MAX_OFF_PATH = 500;
    private static final int SWIPE_THRESHOLD_VELOCITY = 50;


    private static final String notice="두 손가락으로 화면을 한번 터치하면 음성인식이 시작됩니다. 주문내용을 말해주세요. 이전화면 돌아가기는 왼쪽 드래그 해주세요.";
    //비콘 id 받아오기
    StoreID st = menu_basic.storeID;

    private LoginData loginData;
    String userID;

    private Date currentTime;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.order);

        this.gDetector=new GestureDetector(this,this);
        //더블 탭 이벤트를 감지하고자 setOnDoubleTap() 메소드 호출
        gDetector.setOnDoubleTapListener(this);

        textView5=findViewById(R.id.textView5);

        //음성으로 주문하기 위해 객체
        //음성인식 객체
        //사용자에게 음성을 요구하고 음성 인식기를 통해 전송하는 활동을 시작
        SttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //음성인식을 위한 음성 인식기의 의도에 사용되는 여분의 키
        SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        //음성을 번역할 언어를 설정
        SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        //SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US");

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(recognitionListener);

        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
                tts.speak(textView5.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
                try{
                    TimeUnit.SECONDS.sleep(3);
                }catch (InterruptedException e){
                    //Toast.makeText(getApplicationContext(),"여기서 에러!!",Toast.LENGTH_SHORT).show();
                }

                tts.speak(notice,TextToSpeech.QUEUE_FLUSH,null);
            }
        });


        //서비스 객체
        service = RetrofitClient.getClient().create(ServiceApi.class);

        //로그인 액티비티에서 넘겨주는 아이디 데이터 받기
        Intent idIntent = getIntent();

        userID=idIntent.getStringExtra("userID");

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

    private RecognitionListener recognitionListener=new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            //Toast.makeText(order.this, "음성인식을 시작합니다", Toast.LENGTH_SHORT).show();
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
            String key = " ";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);

            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);


            orderData od=new orderData(st.getStoreID(),userID,rs[0],currentTime=Calendar.getInstance().getTime());

            orderResult(od);
        }

        private void orderResult(orderData data){

            service.orderResult(data).enqueue(new Callback<orderResponse>() {
                @Override
                public void onResponse(Call<orderResponse> call, Response<orderResponse> response) {
                   final orderResponse result = response.body();

                    tts=new TextToSpeech(order.this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if(status!=ERROR)
                                tts.setLanguage(Locale.KOREAN);
                            tts.speak(result.getMessage(), TextToSpeech.QUEUE_FLUSH,null);
                        }
                    });
                }

                @Override
                public void onFailure(Call<orderResponse> call, Throwable t) {

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
        tts.shutdown();
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
        tts.stop();
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
                Toast.makeText(getApplicationContext(), "Left Swipe", Toast.LENGTH_SHORT).show();
                onBackPressed();

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
}
