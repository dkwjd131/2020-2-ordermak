package org.techtown.ordermak.activity;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.techtown.ordermak.activity.LoginActivity;

import org.techtown.ordermak.data.PreferencManager;

public class FirstAuth{

    private Intent intent;
    static final String PREF_USER_ID = "userID";
    static final String PREF_USER_PWD = "userPWD";


    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    // 계정 정보 저장
    public static void setUserName(Context ctx, String userID,String userPWD) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_ID, userID);
        editor.putString(PREF_USER_PWD, userPWD);
        editor.commit();
    }


    // 저장된 정보 가져오기
    public static String getUserID(Context ctx) {
        return getSharedPreferences(ctx).getString("userID", "");
    }

    // 저장된 정보 가져오기
    public static String getUserPWD(Context ctx) {
        return getSharedPreferences(ctx).getString("userPWD", "");
    }


    // 로그아웃
    public static void clearAuto(Context ctx) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.clear();
        editor.commit();
    }

}