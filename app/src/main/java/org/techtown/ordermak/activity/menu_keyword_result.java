package org.techtown.ordermak.activity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.techtown.ordermak.R;
import org.techtown.ordermak.data.MenuInfo;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import static android.speech.tts.TextToSpeech.ERROR;

public class menu_keyword_result extends AppCompatActivity implements GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener {

    GestureDetector gestureDetector;
    TextView ttsResultView;
    TextView textView15;
    TextToSpeech tts;

    String uuid;
    String userID;

    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_MAX_OFF_PATH = 500;
    private static final int SWIPE_THRESHOLD_VELOCITY = 50;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_keyword_result);

        ttsResultView = findViewById(R.id.ttsResultView);
        ttsResultView.setMovementMethod(new ScrollingMovementMethod());
        textView15 = findViewById(R.id.textView15);

        this.gestureDetector=new GestureDetector(this,this);
        //더블 탭 이벤트를 감지하고자 seOnDoubleTap() 메소드 호출
        gestureDetector.setOnDoubleTapListener(this);

        final Intent intent = getIntent();

        ArrayList<Map<String, String>> arr2 = (ArrayList<Map<String, String>>) intent.getSerializableExtra("info");

        String temp = arr2.size()+"개의 키워드 검색 결과가 있습니다. 이전화면은 두 손가락으로 왼쪽 드래그, 주문하기는 오른쪽 드래그 해주세요.";

        for (int i = 0; i < arr2.size(); i++) {
            temp = temp + (i+1)+ "번. "+arr2.get(i).get("menu_name") +". 가격 "+ arr2.get(i).get("price") +"원.  메뉴설명. "+ arr2.get(i).get("description")+".    ";

        }
        ttsResultView.setText(temp);
        //Toast.makeText(menu_keyword_result.this, temp, Toast.LENGTH_SHORT).show();

        final String finalTemp = textView15.getText().toString() + temp;
        tts=new TextToSpeech(menu_keyword_result.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=ERROR)
                    tts.setLanguage(Locale.KOREAN);
                tts.speak(finalTemp,TextToSpeech.QUEUE_FLUSH,null);
            }
        });

        Intent getIntent=getIntent();
        uuid= getIntent.getStringExtra("uuid");
        userID= getIntent.getStringExtra("userID");

    }

    //액티비티 종료시
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
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
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        tts.stop();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;

            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                //Toast.makeText(getApplicationContext(), "Left Swipe", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, menu_keyword.class);
                intent.putExtra("uuid",uuid);
                intent.putExtra("userID",userID);
                startActivity(intent);
            }
            // left to right swipe
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                //Toast.makeText(getApplicationContext(), "Right Swipe", Toast.LENGTH_SHORT).show();
                Intent intent2=new Intent(this,order.class);
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
}
