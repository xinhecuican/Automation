package tech.xinhecuican.automation.utils;

import static android.content.Context.ACTIVITY_SERVICE;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.FileUtils;
import android.os.Parcel;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
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

    public static String showActivityName(String packageName, String activityName){
        if(activityName.startsWith("android")){
            int index = -1;
            if(!packageName.equals("") && (index = packageName.lastIndexOf('.')) != -1){
                return packageName.substring(index+1).concat(activityName.substring(activityName.indexOf('.')));
            }
        }
        return activityName;
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
        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        nodes.add(service.getRootInActiveWindow());
        int index = 0;
        while(!nodes.isEmpty()){
            AccessibilityNodeInfo node = nodes.get(index++);
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
                AccessibilityNodeInfo childNode = node.getChild(i);
                if(childNode != null)
                    nodes.add(childNode);
            }
        }
        Debug.info("can't find widget", 1);
        return null;
    }

    public static AccessibilityNodeInfo findWidgetByText(AccessService service, String text){
        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        nodes.add(service.getRootInActiveWindow());
        int index = 0;
        while(!nodes.isEmpty()){
            AccessibilityNodeInfo node = nodes.get(index++);
            if(node != null){
                CharSequence cText = node.getText();
                if(cText != null){
                    String nodeText = cText.toString();
                    if(nodeText.length() > 10)
                        nodeText = nodeText.substring(0, 9);
                    if(nodeText.contains(text))
                        return node;
                }
            }
            for(int i = 0; i< Objects.requireNonNull(node).getChildCount(); i++){
                AccessibilityNodeInfo childNode = node.getChild(i);
                if(childNode != null)
                    nodes.add(childNode);
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

    public static String getFilePathFromContentUri(Uri contentUri, ContentResolver contentResolver) {
        String filePath;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};

        Cursor cursor = contentResolver.query(contentUri, filePathColumn, null, null, null);

        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }

    public static String getFileAbsolutePath(Context context, Uri imageUri) {
        if (context == null || imageUri == null) {
            return null;
        }

        return uriToFileApiQ(context,imageUri);
    }

    private static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            String[] projection = {MediaStore.Images.ImageColumns.DATA};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

//            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    /**
     * Android 10 以上适配 另一种写法
     */
    private static String getFileFromContentUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        String filePath;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, filePathColumn, null,
                null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            try {
                int index = cursor.getColumnIndex(filePathColumn[0]);
                if(index == -1)
                    return "";
                filePath = cursor.getString(index);
                return filePath;
            } catch (Exception e) {
            } finally {
                cursor.close();
            }
        }
        return "";
    }

    private static String uriToFileApiQ(Context context, Uri uri) {
        File file = null;
        //android10以上转换
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            file = new File(uri.getPath());
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //把文件复制到沙盒目录
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if(index == -1)
                    return "";
                String displayName = cursor.getString(index);
                try {
                    InputStream is = contentResolver.openInputStream(uri);
                    File cache = new File(context.getExternalCacheDir().getAbsolutePath(), Math.round((Math.random() + 1) * 1000) + displayName);
                    FileOutputStream fos = new FileOutputStream(cache);
                    FileUtils.copy(is, fos);
                    file = cache;
                    fos.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        assert file != null;
        return file.getAbsolutePath();
    }
}
