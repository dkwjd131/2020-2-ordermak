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

import org.techtown.ordermak.MainActivity;
import org.techtown.ordermak.R;
import org.techtown.ordermak.data.KeywordResponse;
import org.techtown.ordermak.data.LoginResponse;
import org.techtown.ordermak.data.StoreID;
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

import static android.speech.tts.TextToSpeech.ERROR;

public class menu_basic extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private GestureDetector gDetector;
    private TextToSpeech tts;
    private TextView textView2;
    private TextView textView27;

    private ServiceApi service;

    static StoreID storeID;


    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_MAX_OFF_PATH = 500;
    private static final int SWIPE_THRESHOLD_VELOCITY = 50;


    private static final String notice="두 손가락으로 화면을 한번 터치하면 메뉴안내가 시작되고 두번 터치하면 멈춥니다. 카테고리 검색은 위로 드래그, 키워드 검색은 아래로 드래그, 주문하기는 왼쪽 드래그를 해주세요";

    private  String menu="";
    private String tempEvent = "매장의 이벤트 입니다.";

    private String data2;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_basic);

        this.gDetector=new GestureDetector(this,this);
        //더블 탭 이벤트를 감지하고자 seOnDoubleTap() 메소드 호출
        gDetector.setOnDoubleTapListener(this);

        textView2=findViewById(R.id.textView2);
        textView27=findViewById(R.id.textView27);
        textView27.setMovementMethod(new ScrollingMovementMethod());

        //통신위함
        service = RetrofitClient.getClient().create(ServiceApi.class);

        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!=ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
                tts.speak(textView2.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
                try{
                    TimeUnit.SECONDS.sleep(3);
                }catch (InterruptedException e){
                    //Toast.makeText(getApplicationContext(),"여기서 에러!!",Toast.LENGTH_SHORT).show();
                }

                tts.speak(notice,TextToSpeech.QUEUE_FLUSH,null);
            }
        });

        //홈화면에서 받아온 비콘 id

        Intent getIntent=getIntent();
        String data= getIntent.getStringExtra("uuid");
        data2= getIntent.getStringExtra("userID");

        //비콘id 토스트 메시지로 띄우는 코드( 나중에 삭제해야 하는 코드)
        //Toast.makeText(getApplicationContext(),data,Toast.LENGTH_LONG).show();

        storeID=new StoreID(data);

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
                tts.speak(textView2.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
                try{
                    TimeUnit.SECONDS.sleep(3);
                }catch (InterruptedException e){
                    //Toast.makeText(getApplicationContext(),"여기서 에러!!",Toast.LENGTH_SHORT).show();
                }

                tts.speak(notice,TextToSpeech.QUEUE_FLUSH,null);
            }
        });
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

        tts.stop();
        //여기서 시작
        basicSearch(storeID);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY1) {
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
                tts.stop();
                //Toast.makeText(getApplicationContext(), "Left Swipe", Toast.LENGTH_SHORT).show();
                //
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);


            }
            // left to right swipe
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                tts.stop();
                //Toast.makeText(getApplicationContext(), "Right Swipe", Toast.LENGTH_SHORT).show();
                //Intent intent = new Intent(this, MainActivity.class);
                //startActivity(intent);

                Intent intent4=new Intent(this,order.class);
                intent4.putExtra("uuid",storeID.getStoreID());
                intent4.putExtra("userID",data2);
                startActivity(intent4);
            }
            // down to up swipe
            else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                tts.stop();
                //Toast.makeText(getApplicationContext(), "Swipe up", Toast.LENGTH_SHORT).show();
                Intent intent2=new Intent(this,menu_category.class);
                intent2.putExtra("uuid",storeID.getStoreID());
                intent2.putExtra("userID",data2);
                startActivity(intent2);
            }
            // up to down swipe
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                tts.stop();
                //Toast.makeText(getApplicationContext(), "Swipe down", Toast.LENGTH_SHORT).show();
                Intent intent3=new Intent(this,menu_keyword.class);
                intent3.putExtra("uuid",storeID.getStoreID());
                intent3.putExtra("userID",data2);
                startActivity(intent3);
            }
        } catch (Exception e) {
        }
        return true;
    }

    //메뉴 기본 재생 함수 
    private void basicSearch(StoreID data){
        service.basicSearch(data).enqueue(new Callback<KeywordResponse>() {
            @Override
            public void onResponse(Call<KeywordResponse> call, Response<KeywordResponse> response) {
                final KeywordResponse result = response.body();

                ArrayList<Map<String, String>> arr = (ArrayList) result.getMessage();

                String tempMenu = "매장의 메뉴 입니다."+arr.size()+"개의 메뉴가 있습니다.";

                tempEvent = tempEvent + arr.get(0).get("stevt")+".  ";

                for (int i = 0; i < arr.size(); i++) {
                    tempMenu = tempMenu + (i+1) + "번. " + arr.get(i).get("menu_name") + ". 가격 " + arr.get(i).get("price") + "원. 메뉴 설명." + arr.get(i).get("description") + ".  ";
                }
                menu = tempMenu;
                //밑에 코드 나중에 삭제
                //Toast.makeText(menu_basic.this, temp,Toast.LENGTH_SHORT).show();
                final String finalTempMenu = tempMenu;
                textView27.setText(tempEvent+finalTempMenu);
                tts.stop();
                tts = new TextToSpeech(menu_basic.this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != ERROR)
                            tts.setLanguage(Locale.KOREAN);
                        tts.speak(tempEvent, TextToSpeech.QUEUE_FLUSH, null);
                        try{
                            TimeUnit.SECONDS.sleep(6);
                        }catch (InterruptedException e){
                            //Toast.makeText(getApplicationContext(),"여기서 에러!!",Toast.LENGTH_SHORT).show();
                        }
                        tts.speak(finalTempMenu,TextToSpeech.QUEUE_FLUSH,null);

                    }
                });
            }
                        /*
                tts = new TextToSpeech(menu_basic.this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status!=ERROR)
                            tts.setLanguage(Locale.KOREAN);
                        tts.speak(menu, TextToSpeech.QUEUE_FLUSH,null);
                    }
                }) ;
            }


                         */


            @Override
            public void onFailure(Call<KeywordResponse> call, Throwable t) {

            }
        });
    }
}
