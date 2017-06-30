package org.ldscd.callingworkflow.utils;

import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    public static String evalNull(String val) {
        return val.toLowerCase().equals("null") ? null : val;
    }

    public static <T> List<T> toEnumListFromJSONArray(Class<T> clazz, JSONArray jsonArray) throws NoSuchFieldException {
        List<T> list = new ArrayList<T>();
        if (jsonArray != null) {
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                try {
                    list.add(clazz.cast(Enum.valueOf((Class<Enum>)clazz, jsonArray.getString(i))));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }
}