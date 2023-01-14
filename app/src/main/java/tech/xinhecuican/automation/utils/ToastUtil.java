package tech.xinhecuican.automation.utils;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

public class ToastUtil {
    public static void ToastShort(Context context,  String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void ToastBack(Context context, String text, Toast.Callback callback){
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            toast.addCallback(callback);
        }
        toast.show();
    }
}
