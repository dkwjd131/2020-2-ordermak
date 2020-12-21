package org.techtown.ordermak.data;

import com.google.gson.annotations.SerializedName;

public class LoginData {
    @SerializedName("userID")
    String userID;

    @SerializedName("userPwd")
    String userPwd;

    public LoginData(String userID, String userPwd){
        this.userID=userID;
        this.userPwd=userPwd;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserPwd() {
        return userPwd;
    }
}
