package org.techtown.ordermak.data;

import com.google.gson.annotations.SerializedName;
public class categoryData {

    @SerializedName("storeID")
    public String storeID;

    @SerializedName("category")
    public String category;

    public categoryData(String storeID, String category){
        this.storeID=storeID;
        this.category=category;
    }

}
