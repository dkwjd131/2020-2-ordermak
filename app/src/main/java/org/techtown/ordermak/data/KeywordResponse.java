package org.techtown.ordermak.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Map;

public class KeywordResponse {

    @SerializedName("array")
    private ArrayList<Map<String,String>> arr;

    public ArrayList<Map<String,String>> getMessage(){
        return arr;
    }
}

