package org.techtown.ordermak.data;

import com.google.gson.annotations.SerializedName;

public class StoreID {

    @SerializedName("storeID")
    private String storeID;

    public StoreID(String storeID){
        this.storeID=storeID;
    }

    public String getStoreID() {
        return storeID;
    }
}
