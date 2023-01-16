package tech.xinhecuican.automation.utils;

import static android.content.Context.ACTIVITY_SERVICE;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.RequiresApi;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import tech.xinhecuican.automation.AccessService;
import tech.xinhecuican.automation.model.WidgetDescription;

public class Utils {

    public static String packageName = "tech.xinhecuican.automation";

    public static int calcViewHeight(View view){
        int height = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(0, height);
        return view.getMeasuredHeight();
    }


    /**
     * 将本应用置顶到最前端
     * 当本应用位于后台时，则将它切换到最前端
     *
     * @param context
     */
    public static void setTopApp(Context context) {
        if (!isRunningForeground(context)) {
            /**获取ActivityManager*/
            ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);

            /**获得当前运行的task(任务)*/
            List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(100);
            for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                /**找到本应用的 task，并将它切换到前台*/
                if (taskInfo.topActivity.getPackageName().equals(context.getPackageName())) {
                    activityManager.moveTaskToFront(taskInfo.id, 0);
                    break;
                }
            }
        }
    }

    /**
     * 判断本应用是否已经位于最前端
     *
     * @param context
     * @return 本应用已经位于最前端时，返回 true；否则返回 false
     */
    public static boolean isRunningForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfoList = activityManager.getRunningAppProcesses();
        /**枚举进程*/
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfoList) {
            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (appProcessInfo.processName.equals(context.getApplicationInfo().processName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isAccessibilitySettingsOn(Context mContext, Class<? extends android.accessibilityservice.AccessibilityService> clazz) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + clazz.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isIgnoringBatteryOptimizations(Context context) {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return isIgnoring;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestIgnoreBatteryOptimizations(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static AccessibilityNodeInfo findWidgetByDescription(AccessService service, WidgetDescription description){
        Deque<AccessibilityNodeInfo> nodes = new ArrayDeque<>();
        nodes.add(service.getRootInActiveWindow());
        while(!nodes.isEmpty()){
            AccessibilityNodeInfo node = nodes.poll();
            if(node != null){
                if(description.resourceId != -1){
                    long nodeId = Utils.getResourceId(node, service.reflectSourceNodeId);
                    if(nodeId == description.resourceId) {
                        return node;
                    }
                }
                CharSequence cId = node.getViewIdResourceName();
                CharSequence cClass = node.getClassName();
                CharSequence cText = node.getText();
                boolean considerId = !description.id.equals("");
                boolean considerClass = !description.className.equals("");
                boolean considertext = !description.text.equals("");
                boolean idEqual = !considerId || cId != null && description.id.equals(cId.toString());
                boolean classEqual = !considerClass || cClass != null && description.className.equals(cClass.toString());
                boolean textEqual = !considertext || cText != null && description.text.equals(cText.toString());
                if(idEqual && classEqual && textEqual) {
                    return node;
                }
            }

            for(int i = 0; i< Objects.requireNonNull(node).getChildCount(); i++){
                nodes.add(node.getChild(i));
            }
        }
        Debug.info("can't find widget", 1);
        return null;
    }

    public static int dp2px(float dpValue) {
        return (int) (0.5f + dpValue * Resources.getSystem().getDisplayMetrics().density);
    }

    public static long getResourceId(AccessibilityNodeInfo info, Field reflectId){
        long resouceId = -1;
        if(reflectId != null){
            try {
                resouceId = (long) reflectId.get(info);
            } catch (IllegalAccessException | NullPointerException e) {
                e.printStackTrace();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    AccessibilityNodeInfo node = new AccessibilityNodeInfo(info);
                    Parcel parcel = Parcel.obtain();
                    node.writeToParcel(parcel, 0);
                    parcel.setDataPosition(0);
                    parcel.marshall();
                    long field = parcel.readLong();
                    if((field & 1L) == 1L) parcel.readInt();

                    if((field & 2L) == 2L) resouceId = parcel.readLong();
                }
                return resouceId;
            }
        }
        else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            AccessibilityNodeInfo node = new AccessibilityNodeInfo(info);
            Parcel parcel = Parcel.obtain();
            node.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            parcel.marshall();
            long field = parcel.readLong();
            if((field & 1L) == 1L) parcel.readInt();

            if((field & 2L) == 2L) resouceId = parcel.readLong();
        }
        return resouceId;
    }
}
