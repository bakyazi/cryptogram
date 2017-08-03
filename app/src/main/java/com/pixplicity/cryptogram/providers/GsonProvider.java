package com.pixplicity.cryptogram.providers;

import com.google.gson.Gson;

public class GsonProvider {

    private static Gson sGson;

    public static Gson getGson() {
        if (sGson == null) {
            sGson = new Gson();
        }
        return sGson;
    }

}
