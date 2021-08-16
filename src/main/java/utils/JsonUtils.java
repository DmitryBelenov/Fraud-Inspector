package utils;

import com.google.gson.Gson;

import java.io.Serializable;
import java.lang.reflect.Type;

public class JsonUtils {
    public static String toJson(final Object o) {
        final Gson gson = new Gson();
        return gson.toJson(o);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        final Gson gson =  new Gson();
        return gson.fromJson(json, typeOfT);
    }

    public static <T extends Serializable> T fromJson(final String s, final Class<T> c) {
        final Gson gson = new Gson();
        return gson.fromJson(s, c);
    }
}
