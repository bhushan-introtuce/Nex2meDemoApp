package co.introtuce.nex2me.test.network;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
/**
 * Created by Abu Osama on 23-06-2020.
 */
public class ApiClient {
    public static final String BASE_URL = "http://104.131.58.199:5000/";
    private static Retrofit retrofit = null;
    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            // todo deal with the issues the way you need to
            if (response.code() == 500) {
//                        startActivity(
//                                new Intent(
//                                        ErrorHandlingActivity.this,
//                                        ServerIsBrokenActivity.class)
//       );
                return response;
            }
            return response;
        }
    })
            .connectTimeout(2, TimeUnit.SECONDS)
            .writeTimeout(2, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.SECONDS)
            .addNetworkInterceptor(new NetworkConnectionInterceptor() {
                @Override
                public boolean isInternetAvailable() {
                    return false;
                }
                @Override
                public void onInternetUnavailable() {

                }
            })
            .build();
    public static Retrofit getClient() {
        Log.d("BASE_URL","base url "+BASE_URL);
        if (retrofit==null) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            retrofit = new Retrofit.Builder()
                    .client(okHttpClient)
                     .baseUrl(BASE_URL)
                     .addConverterFactory(GsonConverterFactory.create(gson))
                    //.addConverterFactory(ScalarsConverterFactory.create())
                    //.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
