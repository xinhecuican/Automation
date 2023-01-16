package tech.xinhecuican.automation.utils;

import android.util.Log;

public class Debug {

    private static String getCallerInfo(int layer){
        String className = Thread.currentThread().getStackTrace()[4+layer].getClassName();
        String methodName = Thread.currentThread().getStackTrace()[4].getMethodName();
        return className + " " + methodName + " ";
    }

    public static void error(Object message, int layer)
    {

        Log.e("automationError", getCallerInfo(layer) + String.valueOf(message));
    }

    public static void info(Object message, int layer){
        Log.d("automationInfo", getCallerInfo(layer) + String.valueOf(message));
    }
}
