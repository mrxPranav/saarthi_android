package com.pranav.saarthi.api;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    private static final String BASE_URL = "https://saarthi-api-20ml.onrender.com/";

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new okhttp3.logging.HttpLoggingInterceptor();
            // Set your desired log level
            logging.setLevel(okhttp3.logging.HttpLoggingInterceptor.Level.BODY);

            okhttp3.OkHttpClient.Builder httpClient = new okhttp3.OkHttpClient.Builder();
            // Add logging as last interceptor
            httpClient.addInterceptor(logging);

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
}
