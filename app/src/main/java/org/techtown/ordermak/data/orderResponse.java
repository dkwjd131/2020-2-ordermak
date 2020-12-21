package org.techtown.ordermak.data;
import com.google.gson.annotations.SerializedName;

public class orderResponse {

    @SerializedName("array")
    private String message;

    public String getMessage() {
        return message;
    }
}
