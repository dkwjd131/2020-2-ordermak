package org.techtown.ordermak;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.techtown.ordermak.activity.FirstAuth;
import org.techtown.ordermak.activity.LoginActivity;
import org.techtown.ordermak.activity.manual;
import org.techtown.ordermak.activity.menu_basic;
import org.techtown.ordermak.data.KeywordResponse;
import org.techtown.ordermak.data.StoreID;
import org.techtown.ordermak.network.RetrofitClient;
import org.techtown.ordermak.network.ServiceApi;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.speech.tts.TextToSpeech.ERROR;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityManager;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener {

    //홈화면 제스처
    private GestureDetector gDetector;
    private TextToSpeech tts;

    private TextView textView;

    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_MAX_OFF_PATH = 500;
    private static final int SWIPE_THRESHOLD_VELOCITY = 50;

    private static final String notice="비콘알람을 선택하면 메뉴를 들을 수 있습니다. 로그아웃은 왼쪽 드래그, 앱 사용 방법 안내는 아래로 드래그 하세요.";



    //비콘쪽
    protected static final String TAG1 = "::MonitoringActivity::";
    protected static final String TAG2 = "::RangingActivity::";
    private static final String TAG = "BeaconTest";
    private BeaconManager beaconManager;
    //private TextView textView;

    private List<Beacon> beaconList = new ArrayList<>();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 1000;

    NotificationManager manager;
    NotificationCompat.Builder builder;

    private static String CHANNEL_ID = "channel1";
    private static String CHANEL_NAME = "Channel1";

    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    //현재 비콘 id
    String currentBC = "firstBC";
    String currentID = "init";
    private ServiceApi service;
    public static String storename="등록된 비콘이 아님";
    static StoreID storeID;



    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //checkLocationPermition();

        service = RetrofitClient.getClient().create(ServiceApi.class);

        // 접근성 권한이 없으면 접근성 권한 설정하는 다이얼로그 띄워주는 부분
        if(!checkAccessibilityPermissions()) {
            setAccessibilityPermissions();
        }

        //로그인 액티비티에서 넘겨주는 ID 데이터
        final Intent idIntent = getIntent();
        //final String id=idIntent.getStringExtra("ID");
        currentID =idIntent.getStringExtra("ID");


        beaconManager = BeaconManager.getInstanceForApplication(this);
        //textView = (TextView) findViewById(R.id.Textview);//비콘검색후 검색내용 뿌려주기위한 textview

        //getBeaconParsers() = 활성 비콘 파서 목록을 가져옵니다.
        //거기에 새로운 비콘파서 형식을 만들어서 .add() 합니다
        //setBeaconLayout(String) = BLE 알림 내에서 0으로 색인화 된 오프셋을 바이트로 지정하는 문자열을 기반으로 비콘 필드 구문 분석 알고리즘을 정의합니다.

        //비콘 매니저에서 layout 설정 'm:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25'
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        //beaconManager 설정 bind
        //Android Activity또는 Service에 바인딩 합니다 BeaconService.
        beaconManager.bind(this);


        //beacon 을 활용하려면 블루투스 권한획득(Andoird M버전 이상)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access" );
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok,null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "WAKELOCK");

        //기존 home화면 코드 옮겨온 것
        this.gDetector=new GestureDetector(this,this);
        //더블 탭 이벤트를 감지하고자 seOnDoubleTap() 메소드 호출
        gDetector.setOnDoubleTapListener(this);

        textView=findViewById(R.id.textView);

        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!=ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
                //홈화면 말하기
                tts.speak(textView.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);

                try{
                    TimeUnit.SECONDS.sleep(3);
                }catch (InterruptedException e){
                    Toast.makeText(getApplicationContext(),"여기서 에러!!",Toast.LENGTH_SHORT).show();
                }

                tts.speak(notice,TextToSpeech.QUEUE_FLUSH,null);
            }
        });

    }

    // 접근성 권한이 있는지 없는지 확인하는 부분
    // 있으면 true, 없으면 false
    public boolean checkAccessibilityPermissions() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);

        // getEnabledAccessibilityServiceList는 현재 접근성 권한을 가진 리스트를 가져오게 된다
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.DEFAULT);

        for (int i = 0; i < list.size(); i++) {
            AccessibilityServiceInfo info = list.get(i);

            // 접근성 권한을 가진 앱의 패키지 네임과 패키지 네임이 같으면 현재앱이 접근성 권한을 가지고 있다고 판단함
            if (info.getResolveInfo().serviceInfo.packageName.equals(getApplication().getPackageName())) {
                return true;
            }
        }
        return false;
    }

    // 접근성 설정화면으로 넘겨주는 부분
    public void setAccessibilityPermissions() {
        AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
        gsDialog.setTitle("접근성 권한 설정");
        gsDialog.setMessage("접근성 권한을 필요로 합니다");
        gsDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // 설정화면으로 보내는 부분
                Intent i = new Intent();
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                return;
            }
        }).create().show();
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
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=ERROR)
                    tts.setLanguage(Locale.KOREAN);
                tts.speak(textView.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);

                try{
                    TimeUnit.SECONDS.sleep(3);
                }catch (InterruptedException e){
                    Toast.makeText(getApplicationContext(),"여기서 에러!!",Toast.LENGTH_SHORT).show();
                }

                tts.speak(notice,TextToSpeech.QUEUE_FLUSH,null);

        }
        });
    }

    /**위치허용 함수**/
    private void checkLocationPermition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

            if(permissionCheck == PackageManager.PERMISSION_DENIED){

                // 권한 없음
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_ACCESS_FINE_LOCATION);

            } else{
                // ACCESS_FINE_LOCATION 에 대한 권한이 이미 있음.
            }
        }
// OS가 Marshmallow 이전일 경우 권한체크를 하지 않는다.
        else{

        }
    }/**위치허용 함수**/

    @Override
    public void onBeaconServiceConnect() {
  /*

        //모든 범위 알리미를 제거한다.
        beaconManager.removeAllRangeNotifiers();

        //BeaconService비콘 영역을 보거나 멈출 때 마다 호출해야하는 클래스를 지정합니다 .
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            //하나 이상의 비콘 Region이 표시 될 때 호출됩니다 .
            public void didEnterRegion(Region region) {
                Log.i(TAG1, ":::::최소하나의 비콘 발견하였음:::::");
            }
            @Override
            //비콘 Region이 보이지 않을 때 호출됩니다 .
            public void didExitRegion(Region region) {
                Log.i(TAG1, ":::::더이상 비콘을 찾을 수 없음:::::");
            }
            @Override
            //하나 이상의 비콘 Region이 표시 될 때 MonitorNotifier.INSIDE 상태 값으로 호출됩니다 .
            public void didDetermineStateForRegion(int state, Region region) {
                if(state==0){
                    Log.i(TAG1, ":::::비콘이 보이는 상태이다. state : "+state + ":::::");
                } else {
                    Log.i(TAG1, ":::::비콘이 보이지 않는 상태이다. state : "+state +":::::");
                }
            }
        });

         */


        //범위한정 알리미를 추가한다
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            //눈에 보이는 비콘에 대한 mDistance(major 또는 minor와의 거리를 뜻하는)의 추정치를 제공하기 위해 초당 한 번 호출
            // 비콘이 감지되면 해당 함수가 호출된다. Collection<Beacon> beacons에는 감지된 비콘의 리스트가,
            // region에는 비콘들에 대응하는 Region 객체가 들어온다.
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {


                //List<Beacon> list = (List<Beacon>)beacons;
                if (beacons.size() > 0) {
                    //Log.i(TAG2, ":::::The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.:::::");
                    //Log.i(TAG2, ":::::This :: U U I D :: of beacon   :  "+ beacons.iterator().next().getId1().toString() + ":::::");
                    //Log.i(TAG2, ":::::This ::M a j o r:: of beacon   :  "+ beacons.iterator().next().getId2().toString() + ":::::");
                    //Log.i(TAG2, ":::::This ::M i n o r:: of beacon   :  "+ beacons.iterator().next().getId3().toString() + ":::::");
                    beaconList.clear();
                    int i = 0;
                    for(Beacon beacon : beacons){
                        beaconList.add(beacon);
                    }

                    //String minor = beacons.getClass(0).getId3().toString();

                    String minor=beaconList.get(0).getId3().toString();
                    
                    if(!currentBC.equals(minor)){
                        currentBC = minor;
                        storeID=new StoreID(currentBC);
                        //storename="등록된 비콘이 아님";
                        basicSearch(storeID);
                    }
                }



                //double distance = beacons.iterator().next().getDistance();

            }
        });


        try {
            //알려주는 BeaconService전달 일치 비콘을 찾고 시작하는 Region개체를 지역에서 비콘을 볼 수있는 동안 추정 mDistance에 있는 모든 초 업데이트를 제공합니다.
            beaconManager.startRangingBeaconsInRegion(new Region("C2:02:DD:00:13:DD", null, null, null));
        } catch (RemoteException e) {    }
    }

    public void showNoti(String name, String minor){
        builder = null;
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);

            //manager.createNotificationChannel(
            //new NotificationChannel(CHANNEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_DEFAULT) );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(this,CHANNEL_ID);
        }else{
            builder = new NotificationCompat.Builder(this,null);
        }

        //여기 이부분 수정 함 ! ! ! !
        Intent intent = new Intent(this, menu_basic.class);
        intent.putExtra("uuid",minor);
        intent.putExtra("userID",currentID);
        //intent.putExtra("name",name);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //startActivity(intent);

        //알림창 제목
        builder.setContentTitle("비콘 확인");

        //알림창 메시지
        builder.setContentText(name);

        //알림창 아이콘
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);

        //알림창 터치시 상단 알림상태창에서 알림이 자동으로 삭제되게 합니다.
        builder.setAutoCancel(true);

        //pendingIntent를 builder에 설정 해줍니다.
        // 알림창 터치시 인텐트가 전달할 수 있도록 해줍니다.
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();

        //알림창 실행
        manager.notify(1,notification);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    //제스처 관련 메소드
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
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
                tts.stop();
                Toast.makeText(getApplicationContext(), "Left Swipe", Toast.LENGTH_SHORT).show();
                tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status!=ERROR)
                            tts.setLanguage(Locale.KOREAN);
                        tts.speak("로그아웃 되었습니다.",TextToSpeech.QUEUE_FLUSH,null);
                    }
                });
                FirstAuth.clearAuto(MainActivity.this);
                Intent intent3 = new Intent(this, LoginActivity.class);
                startActivity(intent3);
                //여기 밑 부분 지우기

            }
            // left to right swipe
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(getApplicationContext(), "Right Swipe", Toast.LENGTH_SHORT).show();

                //원래는 앱 종료 위해 사용한 코드
                /*
                finishAffinity();
                System.runFinalization();
                System.exit(0);
                 */
            }
            // down to up swipe
            else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(getApplicationContext(), "Swipe up", Toast.LENGTH_SHORT).show();

            }
            // up to down swipe
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(getApplicationContext(), "Swipe down", Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(this, manual.class);
                startActivity(intent2);
            }
        }
        catch (Exception e) {

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
                storename = arr.get(0).get("store_name");

                 String dum = "등록된 비콘이 아님";
                 if(!storename.equals(dum)){
                     showNoti(arr.get(0).get("store_name"),arr.get(0).get("beacon_ID"));
                     wakeLock.acquire(); // WakeLock 깨우기
                     wakeLock.release(); // WakeLock 해제
                 }
            }

            @Override
            public void onFailure(Call<KeywordResponse> call, Throwable t) {

            }
        });
    }
}