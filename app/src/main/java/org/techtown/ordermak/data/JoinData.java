package org.techtown.ordermak.data;

import com.google.gson.annotations.SerializedName;

public class JoinData {
    @SerializedName("userName")
    private String userName;

    @SerializedName("userPhone")
    private String userPhone;

    @SerializedName("userID")
    private String userID;

    @SerializedName("userPwd")
    private String userPwd;

    public JoinData(String userName, String userPhone, String userID, String userPwd ){
        this.userName=userName;
        this.userPhone=userPhone;
        this.userID=userID;
        this.userPwd=userPwd;
    }
}
