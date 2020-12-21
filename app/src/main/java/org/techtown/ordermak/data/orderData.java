package org.techtown.ordermak.data;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class orderData {

    @SerializedName("storeID")
    public String storeID;

    @SerializedName("userID")
    public String userID;

    @SerializedName("orderContent")
    public String orderContent;

    @SerializedName("currentTime")
    public Date currentTime;


    public orderData(String storeID, String userID, String orderContent, Date currentTime){
        this.storeID=storeID;
        this.orderContent=orderContent;
        this.userID=userID;
        this.currentTime=currentTime;
    }






}
