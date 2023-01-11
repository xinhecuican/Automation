package tech.xinhecuican.automation.utils;

import android.util.Log;

public class Debug {
    public static void error(String message)
    {
        Log.d("automationError", message);
    }

    public static void info(String message){
        Log.d("automationInfo", message);
    }
}
