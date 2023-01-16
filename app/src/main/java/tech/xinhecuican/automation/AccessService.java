package tech.xinhecuican.automation;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Binder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import tech.xinhecuican.automation.model.CoordinateDescription;
import tech.xinhecuican.automation.model.Operation;
import tech.xinhecuican.automation.model.Storage;
import tech.xinhecuican.automation.model.WidgetDescription;
import tech.xinhecuican.automation.utils.Debug;
import tech.xinhecuican.automation.utils.Utils;

public class AccessService extends android.accessibilityservice.AccessibilityService {
    private WidgetDescription saveWidgetDescription;
    private CoordinateDescription saveCoordinateDescription;
    private String currentPackageName, currentClassName;
    private ScheduledExecutorService scheduler;
    private Set<String> IMEApp;
    private static AccessService _instance;
    private boolean isShowDialog;
    private Operation currentOperation;
    private PackageManager packageManager;
    private Set<String> packages;
    private String[] totalPackageNames;
    private View suspendView;
    private List<ScheduledFuture> futures;
    private LinkedBlockingDeque<WindowStateChangeListener> windowStateChangeLisnters;

    public static AccessService getInstance(){
        return _instance;
    }

    @Override
    public void onServiceConnected(){
        futures = new CopyOnWriteArrayList<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        IMEApp = new HashSet<>();
        List<InputMethodInfo> inputMethodInfoList = ((InputMethodManager)
                getSystemService(android.accessibilityservice.AccessibilityService.INPUT_METHOD_SERVICE)).getInputMethodList();
        for (InputMethodInfo e : inputMethodInfoList) {
            IMEApp.add(e.getPackageName());
        }
        _instance = this;
        currentPackageName = "";
        currentClassName = "";
        isShowDialog = false;
        windowStateChangeLisnters = new LinkedBlockingDeque<WindowStateChangeListener>();
//        OperationManager.instance().addListener(new OperationListenerAdapter() {
//            @Override
//            public void onPackageChange(String[] packages) {
//                AccessibilityServiceInfo info = getServiceInfo();
//                info.packageNames = packages;
//                setServiceInfo(info);
//                currentClassName = "";
//                currentPackageName = "";
//            }
//        });

        packages = new HashSet<>();
        packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> ResolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        if(ResolveInfoList != null) {
            for (ResolveInfo e : ResolveInfoList) {
                packages.add(e.activityInfo.packageName);
            }
        }
        totalPackageNames = packages.toArray(new String[0]);

        AccessibilityServiceInfo info = getServiceInfo();
        info.packageNames = totalPackageNames;
        setServiceInfo(info);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _instance = this;
        flags = Service.START_FLAG_REDELIVERY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if(!Storage.instance().isOpen())
            return;
        CharSequence packageNameChar = event.getPackageName();
        CharSequence classNameChar = event.getClassName();
        if(packageNameChar == null || classNameChar == null)return;
        String packageName = packageNameChar.toString();
        String className = classNameChar.toString();
        Debug.info(packageName, 0);
        try{
            switch(event.getEventType())
            {
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                    if(windowStateChangeLisnters.size() != 0){
                        WindowStateChangeListener listener = windowStateChangeLisnters.poll();
                        if(listener != null)
                            listener.onWindowStateChange();
                    }
                    boolean isActivity = !className.startsWith("android.") && !className.startsWith("androidx.");
                    if(!currentPackageName.equals(packageName)){
                        if(isActivity) {
                            currentPackageName = packageName;
                            currentClassName = className;
                            currentOperation = Storage.instance().findOperationByActivity(currentClassName);
                            if(currentOperation != null && currentOperation.isAuto())
                                startProcess();
                        }
                    }
                    else{
                        if(isActivity){
                            if(!currentClassName.equals(className)){
                                currentClassName = className;
                                currentOperation = Storage.instance().findOperationByActivity(currentClassName);
                                if(currentOperation != null && currentOperation.isAuto())
                                    startProcess();
                            }
                        }
                    }
                    break;

            }
        }catch(Throwable e){
            Debug.error(e.getMessage(), 0);
            e.printStackTrace();
        }
    }

    @Override
    public void onInterrupt() {
        Debug.info("service stop", 0);
        _instance = null;
        stopProcess();
    }

    public void setDescription(CoordinateDescription coordinateDescription, WidgetDescription widgetDescription){
        saveCoordinateDescription = coordinateDescription;
        saveWidgetDescription = widgetDescription;
    }

    public void restartProcess(){
        stopProcess();
        startProcess();
    }

    public void startProcess(){
        if(scheduler.isShutdown())
            scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            futures.removeIf(future -> future.isDone() || future.isCancelled());
            if (currentOperation != null && futures.size() == 0) {
                futures.addAll(currentOperation.startProcess(_instance, scheduler));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void stopProcess(){
        for(ScheduledFuture future : futures){
            future.cancel(true);
        }
        futures.clear();
    }

    private void findAllNode(List<AccessibilityNodeInfo> roots, List<AccessibilityNodeInfo> list, String indent) {
        ArrayList<AccessibilityNodeInfo> childrenList = new ArrayList<>();
        for (AccessibilityNodeInfo e : roots) {
            if (e == null) continue;
            list.add(e);
            for (int n = 0; n < e.getChildCount(); n++) {
                childrenList.add(e.getChild(n));
            }
        }
        if (!childrenList.isEmpty()) {
            findAllNode(childrenList, list, indent + "  ");
        }
    }

    public void stopService(){
        stopSelf();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void showSuspendball(SuspendCloseListener listener){
        final WindowManager windowManager = (WindowManager) getSystemService(android.accessibilityservice.AccessibilityService.WINDOW_SERVICE);
        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);

        boolean b = metrics.heightPixels > metrics.widthPixels;
        final int width = b ? metrics.widthPixels : metrics.heightPixels;
        final int height = b ? metrics.heightPixels : metrics.widthPixels;

        final LayoutInflater inflater = LayoutInflater.from(this);
        suspendView = inflater.inflate(R.layout.floatball, null);
        Button button = suspendView.findViewById(R.id.button);

        final WindowManager.LayoutParams customizationParams;
        customizationParams = new WindowManager.LayoutParams();
        customizationParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        customizationParams.format = PixelFormat.TRANSPARENT;
        customizationParams.gravity = Gravity.START | Gravity.TOP;
        customizationParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        customizationParams.width = Utils.calcViewHeight(button);
        customizationParams.height = customizationParams.width;
        customizationParams.x = 0;
        customizationParams.y = 0;
        customizationParams.alpha = 0.8f;

        button.setOnTouchListener(new View.OnTouchListener() {
            int x = 0, y = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = Math.round(event.getRawX());
                        y = Math.round(event.getRawY());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        customizationParams.x = Math.round(customizationParams.x + (event.getRawX() - x));
                        customizationParams.y = Math.round(customizationParams.y + (event.getRawY() - y));
                        x = Math.round(event.getRawX());
                        y = Math.round(event.getRawY());
                        windowManager.updateViewLayout(suspendView, customizationParams);
                        break;
                }
                return false;
            }
        });

        button.setOnLongClickListener(v -> {
            windowManager.removeViewImmediate(suspendView);
            suspendView = null;
            Storage.instance().setShowBall(false);
            listener.onSuspendClose();
            return true;
        });
        button.setOnClickListener(v -> {
            restartProcess();
        });

        windowManager.addView(suspendView, customizationParams);
    }

    public void removeSuspendView(){
        final WindowManager windowManager = (WindowManager) getSystemService(android.accessibilityservice.AccessibilityService.WINDOW_SERVICE);
        if(suspendView != null)
            windowManager.removeViewImmediate(suspendView);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void showActivityCustomizationDialog(boolean isOnlyScroll, WindowInfoResultListener listener) {
        if(isShowDialog)
            return;

//        // 暂时允许接受所有包
//        AccessibilityServiceInfo info = getServiceInfo();
//        info.packageNames = packages.toArray(new String[0]);
//        setServiceInfo(info);
//        AccessibilityNodeInfo activeNode = getRootInActiveWindow();
//        currentPackageName = activeNode.getPackageName() != null ? activeNode.getPackageName().toString() : "";
//        CharSequence cClass = activeNode.getClassName();
//        if(cClass != null){
//            String rootClassName = cClass.toString();
//            if(!rootClassName.startsWith("android.") && !rootClassName.startsWith("androidx.")){
//                currentClassName = rootClassName;
//            }
//            else{
//                currentClassName = "";
//            }
//        }
//        else{
//            currentClassName = "";
//        }
        // show activity customization window
        final WindowManager windowManager = (WindowManager) getSystemService(android.accessibilityservice.AccessibilityService.WINDOW_SERVICE);
        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);

        boolean b = metrics.heightPixels > metrics.widthPixels;
        final int width = b ? metrics.widthPixels : metrics.heightPixels;
        final int height = b ? metrics.heightPixels : metrics.widthPixels;

        final LayoutInflater inflater = LayoutInflater.from(this);
        // activity customization view
        final View viewCustomization = inflater.inflate(R.layout.get_window_info, null);
        final TextView packageNameView = viewCustomization.findViewById(R.id.package_name);
        final TextView activityNameView = viewCustomization.findViewById(R.id.operation_activity_name);
        final TextView widgetInfoView = viewCustomization.findViewById(R.id.widget_info);
        final TextView coordInfoView = viewCustomization.findViewById(R.id.coordinate_info);
        Button showLayoutButton = viewCustomization.findViewById(R.id.layout);
        final Button addWidgetButton = viewCustomization.findViewById(R.id.add_widget);
        Button focusButton = viewCustomization.findViewById(R.id.focus);
        final Button addCoordButton = viewCustomization.findViewById(R.id.add_coord);
        Button exitButton = viewCustomization.findViewById(R.id.exit);

        final View viewTarget = inflater.inflate(R.layout.window_info_node, null);
        final FrameLayout layoutOverlayOutline = viewTarget.findViewById(R.id.frame);

        final ImageView imageTarget = new ImageView(this);
        imageTarget.setImageResource(R.drawable.ic_target);

        // define view positions
        final WindowManager.LayoutParams customizationParams, outlineParams, targetParams;
        customizationParams = new WindowManager.LayoutParams();
        customizationParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        customizationParams.format = PixelFormat.TRANSPARENT;
        customizationParams.gravity = Gravity.START | Gravity.TOP;
        customizationParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        customizationParams.width = width;
        customizationParams.height = height / 5;
        customizationParams.x = (metrics.widthPixels - customizationParams.width) / 2;
        customizationParams.y = metrics.heightPixels - customizationParams.height;
        customizationParams.alpha = 0.8f;

        outlineParams = new WindowManager.LayoutParams();
        outlineParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        outlineParams.format = PixelFormat.TRANSPARENT;
        outlineParams.gravity = Gravity.START | Gravity.TOP;
        outlineParams.width = metrics.widthPixels;
        outlineParams.height = metrics.heightPixels;
        outlineParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        outlineParams.alpha = 0f;

        targetParams = new WindowManager.LayoutParams();
        targetParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        targetParams.format = PixelFormat.TRANSPARENT;
        targetParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        targetParams.gravity = Gravity.START | Gravity.TOP;
        targetParams.width = targetParams.height = width / 4;
        targetParams.x = (metrics.widthPixels - targetParams.width) / 2;
        targetParams.y = (metrics.heightPixels - targetParams.height) / 2;
        targetParams.alpha = 0f;

        CoordinateDescription coordinateDescription = new CoordinateDescription();
        WidgetDescription widgetDescription = new WidgetDescription();
        if(saveCoordinateDescription != null){

            coordInfoView.setText("X轴：" + saveCoordinateDescription.x + "    " + "Y轴：" + saveCoordinateDescription.y);
        }
        if(saveWidgetDescription != null){
            packageNameView.setText(saveWidgetDescription.packageName);
            activityNameView.setText(saveWidgetDescription.className);
            widgetInfoView.setText("click:".concat((saveWidgetDescription.isClickable ? "true bonus" : "false bonus"))
                    .concat(saveWidgetDescription.rect.toShortString())
                    .concat(" id:").concat(saveWidgetDescription.id).concat(" desc:").concat(saveWidgetDescription.description)
                    .concat(" text:").concat(saveWidgetDescription.text));
        }

        viewCustomization.setOnTouchListener(new View.OnTouchListener() {
            int x = 0, y = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = Math.round(event.getRawX());
                        y = Math.round(event.getRawY());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        customizationParams.x = Math.round(customizationParams.x + (event.getRawX() - x));
                        customizationParams.y = Math.round(customizationParams.y + (event.getRawY() - y));
                        x = Math.round(event.getRawX());
                        y = Math.round(event.getRawY());
                        windowManager.updateViewLayout(viewCustomization, customizationParams);
                        break;
                }
                return true;
            }
        });

        imageTarget.setOnTouchListener(new View.OnTouchListener() {
            int x = 0, y = 0, width = targetParams.width / 2, height = targetParams.height / 2;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        addCoordButton.setEnabled(true);
                        targetParams.alpha = 0.9f;
                        windowManager.updateViewLayout(imageTarget, targetParams);
                        x = Math.round(event.getRawX());
                        y = Math.round(event.getRawY());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        targetParams.x = Math.round(targetParams.x + (event.getRawX() - x));
                        targetParams.y = Math.round(targetParams.y + (event.getRawY() - y));
                        x = Math.round(event.getRawX());
                        y = Math.round(event.getRawY());
                        windowManager.updateViewLayout(imageTarget, targetParams);
                        coordinateDescription.packageName = currentPackageName;
                        coordinateDescription.activityName = currentClassName;
                        coordinateDescription.x = x + width;
                        coordinateDescription.y = y + height;
                        packageNameView.setText(currentPackageName);
                        activityNameView.setText(currentClassName);
                        coordInfoView.setText(
                                "X轴：" + (targetParams.x + width) + "    " + "Y轴：" + (targetParams.y + height));
                        break;
                    case MotionEvent.ACTION_UP:
                        targetParams.alpha = 0.5f;
                        windowManager.updateViewLayout(imageTarget, targetParams);
                        break;
                }
                return true;
            }
        });

        showLayoutButton.setOnClickListener(v -> {
            Button button = (Button) v;
            if (outlineParams.alpha == 0) {
                AccessibilityNodeInfo root = getRootInActiveWindow();
                if (root == null) return;
                widgetDescription.packageName = currentPackageName;
                widgetDescription.className = currentClassName;
                layoutOverlayOutline.removeAllViews();
                ArrayList<AccessibilityNodeInfo> roots = new ArrayList<>();
                roots.add(root);
                ArrayList<AccessibilityNodeInfo> nodeList = new ArrayList<>();
                findAllNode(roots, nodeList, "");
                if(isOnlyScroll) {
                    roots.clear();
                    for (AccessibilityNodeInfo info1 : nodeList) {
                        if (info1.isScrollable())
                            roots.add(info1);
                    }
                    nodeList = roots;
                }
                Collections.sort(nodeList, (a, b1) -> {
                    Rect rectA = new Rect();
                    Rect rectB = new Rect();
                    a.getBoundsInScreen(rectA);
                    b1.getBoundsInScreen(rectB);
                    return rectB.width() * rectB.height() - rectA.width() * rectA.height();
                });
                for (final AccessibilityNodeInfo e : nodeList) {
                    final Rect temRect = new Rect();
                    e.getBoundsInScreen(temRect);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(temRect.width(), temRect.height());
                    params.leftMargin = temRect.left;
                    params.topMargin = temRect.top;
                    final ImageView img = new ImageView(AccessService.this);
                    img.setBackgroundResource(R.drawable.node);
                    img.setFocusableInTouchMode(true);
                    img.setOnClickListener(v1 -> v1.requestFocus());
                    img.setOnFocusChangeListener((v12, hasFocus) -> {
                        if (hasFocus) {
                            CharSequence cId = e.getViewIdResourceName();
                            CharSequence cDesc = e.getContentDescription();
                            CharSequence cText = e.getText();
                            widgetDescription.rect = temRect;
                            widgetDescription.packageName = currentPackageName;
                            widgetDescription.isClickable = e.isClickable();
                            widgetDescription.activityName = currentClassName;
                            widgetDescription.className = e.getClassName().toString();
                            widgetDescription.id = cId == null ? "" : cId.toString();
                            widgetDescription.description = cDesc == null ? "" : cDesc.toString();
                            widgetDescription.text =  cText == null ? "" : cText.toString();
                            widgetDescription.isScrollable = e.isScrollable();
                            addWidgetButton.setEnabled(true);
                            packageNameView.setText(e.getPackageName().toString());
                            activityNameView.setText(e.getClassName().toString());
                            widgetInfoView.setText(widgetDescription.toString());
                            v12.setBackgroundResource(R.drawable.node_focus);
                        } else {
                            v12.setBackgroundResource(R.drawable.node);
                        }
                    });
                    layoutOverlayOutline.addView(img, params);
                }
                outlineParams.alpha = 0.5f;
                outlineParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                windowManager.updateViewLayout(viewTarget, outlineParams);
                packageNameView.setText(currentPackageName);
                activityNameView.setText(currentClassName);
                button.setText(R.string.hide_layout);
            } else {
                outlineParams.alpha = 0f;
                outlineParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                windowManager.updateViewLayout(viewTarget, outlineParams);
                addWidgetButton.setEnabled(false);
                button.setText(R.string.show_layout);
            }
        });
        focusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                if (targetParams.alpha == 0) {
                    coordinateDescription.packageName = currentPackageName;
                    coordinateDescription.activityName = currentClassName;
                    targetParams.alpha = 0.5f;
                    targetParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    windowManager.updateViewLayout(imageTarget, targetParams);
                    packageNameView.setText(currentPackageName);
                    activityNameView.setText(currentClassName);
                    button.setText(R.string.hide_focus);
                } else {
                    targetParams.alpha = 0f;
                    targetParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                    windowManager.updateViewLayout(imageTarget, targetParams);
                    addCoordButton.setEnabled(false);
                    button.setText(R.string.show_focus);
                }
            }
        });
        addWidgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveWidgetDescription = new WidgetDescription(widgetDescription);
                addWidgetButton.setEnabled(false);
                packageNameView.setText(widgetDescription.packageName.concat(" (以下控件数据已保存)"));
            }
        });
        addCoordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCoordinateDescription = new CoordinateDescription(coordinateDescription);
                addCoordButton.setEnabled(false);
                packageNameView.setText(coordinateDescription.packageName.concat(" (以下坐标数据已保存)"));
            }
        });
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCoordResult(saveCoordinateDescription);
                listener.onWidgetResult(saveWidgetDescription);
                windowManager.removeViewImmediate(viewTarget);
                windowManager.removeViewImmediate(viewCustomization);
                windowManager.removeViewImmediate(imageTarget);

//                AccessibilityServiceInfo info1 = getServiceInfo();
//                info1.packageNames = Storage.instance().getOperationPackageNames();
//                setServiceInfo(info1);
                isShowDialog = false;
            }
        });
        windowManager.addView(viewTarget, outlineParams);
        windowManager.addView(viewCustomization, customizationParams);
        windowManager.addView(imageTarget, targetParams);


        isShowDialog = true;
    }

    public void addWindowStateChangeListener(WindowStateChangeListener listener){
        windowStateChangeLisnters.offer(listener);
    }

    public interface WindowInfoResultListener{
        void onWidgetResult(WidgetDescription description);
        void onCoordResult(CoordinateDescription description);
    }

    public interface SuspendCloseListener{
        void onSuspendClose();
    }

    /**
     * 只会触发一次，之后该listener便会被移出队列
     */
    public interface WindowStateChangeListener {
        void onWindowStateChange();
    }

    public class AccessibilityBinder extends Binder {
        public AccessService getService(){
            return AccessService.this;
        }
    }

}