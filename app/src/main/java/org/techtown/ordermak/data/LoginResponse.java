package org.techtown.ordermak.data;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("userID")
    private int userID;

    public int getCode(){
        return code;
    }

    public String getMessage(){
        return message;
    }

    public int getUserID(){
        return userID;
    }
}
