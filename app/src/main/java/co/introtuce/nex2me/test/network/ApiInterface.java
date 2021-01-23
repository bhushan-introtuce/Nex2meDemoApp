package co.introtuce.nex2me.test.network;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("/api/user/register/create/rtc")
    Call<SessionResponse> createSession(@Query("id") String id);

    @GET("/api/user/register/join/rtc")
    Call<SessionResponse> joinSession(@Query("id")String id);

    @GET("/api/user/register/start/broadcast")
    Call<SessionResponse> startBroadcast(@Query("id")String id);

    @GET("/api/user/register/stop/broadcast")
    Call<SessionResponse> stopBroadcast(@Query("id")String id);

    @GET("/api/user/register/get/broadcast")
    Call<List<Nex2meBroadcast>> getBroadcasts();



}
