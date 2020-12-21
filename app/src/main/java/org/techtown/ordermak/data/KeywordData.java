package org.techtown.ordermak.data;

import com.google.gson.annotations.SerializedName;

public class KeywordData {

    @SerializedName("keyword")
    String keyword;

    public KeywordData(String keyword){
        this.keyword=keyword;
    }

}