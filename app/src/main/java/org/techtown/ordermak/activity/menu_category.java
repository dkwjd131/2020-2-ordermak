package org.techtown.ordermak.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import static android.speech.tts.TextToSpeech.ERROR;
import org.techtown.ordermak.R;
import org.techtown.ordermak.data.KeywordData;
import org.techtown.ordermak.data.KeywordResponse;
import org.techtown.ordermak.data.StoreID;
import org.techtown.ordermak.data.categoryData;
import org.techtown.ordermak.network.RetrofitClient;
import org.techtown.ordermak.network.ServiceApi;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class menu_category extends AppCompatActivity implements GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener {


    final int PERMISSION = 1;

    GestureDetector gDetector;
    TextToSpeech tts;
    TextView textView3;
    TextView textView7;

    private ServiceApi service;

    //음성인식 객체
    Intent SttIntent;
    SpeechRecognizer mRecognizer;

    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_MAX_OFF_PATH = 500;
    private static final int SWIPE_THRESHOLD_VELOCITY = 50;

    private static final String notice="카테고리를 듣고 원하는 카테고리를 말해주세요. 두 손가락으로 화면을 한번 터치하면 음성인식이 시작됩니다. 이전화면 돌아가기는 왼쪽 드래그, 주문하기는 오른쪽 드래그를 해주세요.";

    //비콘 id 받아오는 코드
    StoreID st = menu_basic.storeID;
    String uuid;
    String userID;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_category);

        //퍼미션 체크
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO}, PERMISSION);
        }

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

        this.gDetector = new GestureDetector(this, this);
        //더블 탭 이벤트를 감지하고자 seOnDoubleTap() 메소드 호출
        gDetector.setOnDoubleTapListener(this);

        textView3 = findViewById(R.id.textView3);

        textView7 = findViewById(R.id.textView7);
        textView7.setMovementMethod(new ScrollingMovementMethod());

        //통신 위한 객체
        service = RetrofitClient.getClient().create(ServiceApi.class);


        //화면 글자 읽어주는 코드
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
                tts.speak(textView3.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                try{
                    TimeUnit.SECONDS.sleep(3);
                }catch (InterruptedException e){
                    //Toast.makeText(getApplicationContext(),"여기서 에러!!",Toast.LENGTH_SHORT).show();
                }

                tts.speak(notice,TextToSpeech.QUEUE_FLUSH,null);
            }
        });

        try{
            TimeUnit.SECONDS.sleep(10);
        }catch (InterruptedException e){
            //Toast.makeText(getApplicationContext(),"여기서 에러!!",Toast.LENGTH_SHORT).show();
        }

        Intent getIntent=getIntent();
        uuid= getIntent.getStringExtra("uuid");
        userID= getIntent.getStringExtra("userID");

        //받아온 id로 카테고리 읽어주는 함수 실행
        categorySearch(st);

    }



    //화면 바뀌었을때 음성 다시 재생하는 코드
    @Override
    protected void onRestart() {
        super.onRestart();
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
                tts.speak(textView3.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                try{
                    TimeUnit.SECONDS.sleep(3);
                }catch (InterruptedException e){
                    //Toast.makeText(getApplicationContext(),"여기서 에러!!",Toast.LENGTH_SHORT).show();
                }

                tts.speak(notice,TextToSpeech.QUEUE_FLUSH,null);

            }
        });

        categorySearch(st);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.stop();
        tts.shutdown();
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
        //tts.shutdown();
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
               // Toast.makeText(getApplicationContext(), "Left Swipe", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, menu_basic.class);
                intent.putExtra("uuid",uuid);
                intent.putExtra("userID",userID);
                startActivity(intent);
            }
            // left to right swipe
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                //Toast.makeText(getApplicationContext(), "Right Swipe", Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(this, order.class);
                intent2.putExtra("uuid",uuid);
                intent2.putExtra("userID",userID);
                startActivity(intent2);

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

    //카테고리 search 함수
    private void categorySearch(StoreID data) {
        service.categorySearch(data).enqueue(new Callback<KeywordResponse>() {
            @Override
            public void onResponse(Call<KeywordResponse> call, Response<KeywordResponse> response) {
                final KeywordResponse result = response.body();

                ArrayList<Map<String, String>> arr = (ArrayList) result.getMessage();
                String temp = arr.size() + "개의 카테고리가 있습니다.  ";
                for (int i = 0; i < arr.size(); i++) {
                    temp = temp + (i+1) + "번. " + arr.get(i).get("category")+".  ";
                }
                textView7.setText(temp);
                //Toast.makeText(menu_category.this,temp,Toast.LENGTH_SHORT).show();
                //tts.speak(temp, TextToSpeech.QUEUE_FLUSH, null);

                try {
                    TimeUnit.SECONDS.sleep(25);
                    tts.speak(temp, TextToSpeech.QUEUE_ADD, null);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<KeywordResponse> call, Throwable t) {
                Log.e("에러","에에에에에");
            }
        });
    }

    private RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            //Toast.makeText(menu_category.this, "음성인식을 시작합니다", Toast.LENGTH_SHORT).show();
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

            //sttTextView.setText(rs[0]);


            categoryData cd=new categoryData(st.getStoreID(),rs[0]);

            categoryResult(cd);

        }

        private void categoryResult(categoryData data){

            service.categoryResult(data).enqueue(new Callback<KeywordResponse>() {
                @Override
                public void onResponse(Call<KeywordResponse> call, Response<KeywordResponse> response) {
                    final KeywordResponse result = response.body();

                    ArrayList<Map<String, String>> arr = (ArrayList) result.getMessage();
                    String temp = arr.size()+"개의 카테고리 검색 결과가 있습니다.";
                    for (int i = 0; i < arr.size(); i++) {
                        temp = temp + (i+1)+ "번.  "+arr.get(i).get("menu_name") +".  가격 "+ arr.get(i).get("price") +"원.  메뉴 설명. "+ arr.get(i).get("description")+".  ";
                    }
                    textView7.setText(temp);
                    //Toast.makeText(menu_category.this, temp, Toast.LENGTH_SHORT).show();

                    try {
                        TimeUnit.SECONDS.sleep(3);
                        tts.speak(temp, TextToSpeech.QUEUE_FLUSH, null);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<KeywordResponse> call, Throwable t) {

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
}
