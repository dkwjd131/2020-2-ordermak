package org.techtown.ordermak.network;

import org.techtown.ordermak.data.JoinData;
import org.techtown.ordermak.data.JoinResponse;
import org.techtown.ordermak.data.KeywordData;
import org.techtown.ordermak.data.KeywordResponse;
import org.techtown.ordermak.data.LoginData;
import org.techtown.ordermak.data.LoginResponse;
import org.techtown.ordermak.data.StoreID;
import org.techtown.ordermak.data.categoryData;
import org.techtown.ordermak.data.orderData;
import org.techtown.ordermak.data.orderResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ServiceApi {

    @POST("/user/login")
    Call<LoginResponse> userLogin(@Body LoginData data);

    @POST("/user/join")
    Call<JoinResponse> userJoin(@Body JoinData data);

    @POST("/menu/search")
    Call<KeywordResponse> keywordSearch(@Body categoryData data);

    @POST("/menu/basicSearch")
    Call<KeywordResponse> basicSearch(@Body StoreID data);

    @POST("/menu/categorySearch")
    Call<KeywordResponse> categorySearch(@Body StoreID data);

    @POST("/menu/categoryResult")
    Call<KeywordResponse> categoryResult(@Body categoryData data);

    @POST("menu/order")
    Call<orderResponse> orderResult(@Body orderData data);

}
