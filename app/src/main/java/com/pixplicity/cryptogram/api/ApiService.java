package com.pixplicity.cryptogram.api;

import com.pixplicity.cryptogram.models.Topic;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @GET("free")
    Call<Map<String, Topic>> getFree();

}
