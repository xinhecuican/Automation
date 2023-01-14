package tech.xinhecuican.automation.utils;

import android.util.Log;

public class Debug {

    private static String getCallerInfo(){
        String className = Thread.currentThread().getStackTrace()[4].getClassName();
        String methodName = Thread.currentThread().getStackTrace()[4].getMethodName();
        return className + " " + methodName + " ";
    }

    public static void error(Object message)
    {

        Log.d("automationError", getCallerInfo() + String.valueOf(message));
    }

    public static void info(Object message){
        Log.d("automationInfo", getCallerInfo() + String.valueOf(message));
    }
}
