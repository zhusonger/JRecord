package com.jrecord.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.jrecord.common.activity.DefaultActivityLifeCallback;
import com.jrecord.common.base.Preconditions;
import com.jrecord.common.data.BasePreference;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Created by zhusong on 17/5/11.
 */

public class AppUtils {
    private final static String TAG = "AppUtils";

    @SuppressLint("StaticFieldLeak")
    private static Application sApplication;

    public static void init(Application application) {
        Preconditions.checkNotNull(application, "AppUtils.init application is null");
        sApplication = application;
        // share preference
        BasePreference.init(application);
        ILog.e(TAG, "init application");
        application.registerActivityLifecycleCallbacks(new DefaultActivityLifeCallback());
    }

    public static Application getApplication() {
        checkApplicationInit();
        return sApplication;
    }

    private static void checkApplicationInit() {
        Preconditions.checkNotNull(sApplication, "must call init() first");
    }

    // App Debug
    private static boolean sDebug = false;
    public static void setDebug(boolean debug) {
        sDebug = debug;
    }
    public static boolean isDebug() {
        return sDebug;
    }

    public static boolean isWebUrl(String url) {
        if (AppUtils.isEmpty(url)) {
            return false;
        }
        return url.startsWith("http://") || url.startsWith("https://");
    }

    public static boolean isLocalFile(String url) {
        if (AppUtils.isEmpty(url)) {
            return false;
        }
        return url.startsWith("file://") || url.startsWith("/");
    }

    private static String sAppLabel = null;
    public static String getAppLabel() {
        checkApplicationInit();
        if (sAppLabel == null) {
            sAppLabel = getAppLabel(sApplication.getPackageName());
        }

        if (AppUtils.isEmpty(sAppLabel)) {
            sAppLabel = "";
        }
        return sAppLabel;
    }
    public static String getAppLabel(@NonNull String packageName) {
        Preconditions.checkNotNull(packageName);
        checkApplicationInit();
        // ------> AppVersion
        String label = null;
        try {
            PackageManager packageManager = sApplication.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            label = packageManager.getApplicationLabel(applicationInfo).toString();
        } catch (Exception e) {
            ILog.e(TAG, "getAppLabel", e);
        }
        return label;
    }
    // App Version
    private static String sAppVersion = null;

    public static String getVersion() {
        checkApplicationInit();
        if (null == sAppVersion) {
            // ------> AppVersion
            try {
                PackageInfo packageInfo = sApplication.getPackageManager()
                        .getPackageInfo(sApplication.getPackageName(), 0);
                sAppVersion = packageInfo.versionName;
            } catch (Exception e) {
            }
        }

        if (AppUtils.isEmpty(sAppVersion)) {
            sAppVersion = "0.0";
        }
        return sAppVersion;
    }

    private static int sAppVersionCode;

    public static int getVersionCode() {
        checkApplicationInit();
        if (sAppVersionCode <= 0) {
            // ------> AppVersion
            try {
                PackageInfo packageInfo = sApplication.getPackageManager()
                        .getPackageInfo(sApplication.getPackageName(), 0);
                sAppVersionCode = packageInfo.versionCode;
            } catch (Exception e) {
            }
        }
        return sAppVersionCode;
    }

    private static String sApplicationId;

    public static String getApplicationId() {
        checkApplicationInit();
        if (sApplicationId == null) {
            // ------> AppVersion
            try {
                PackageInfo packageInfo = sApplication.getPackageManager()
                        .getPackageInfo(sApplication.getPackageName(), 0);
                sApplicationId = packageInfo.packageName;
            } catch (Exception e) {
            }
        }
        return sApplicationId;
    }


    // check intent available if start
    public static boolean isIntentAvailable(@NonNull Intent intent) {
        PackageManager packageManager = getApplication().getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    /**
     * 查询的应用包名包含给定的应用包名，俩者不需要完全一致
     * */
    public static boolean isClientAvailable(@NonNull String packageName) {
        Preconditions.checkNotNull(packageName);
        final PackageManager packageManager = getApplication().getPackageManager();
        List<PackageInfo> list = packageManager.getInstalledPackages(0);
        if (isEmpty(list)) {
            return false;
        }
        boolean result = false;
        for (PackageInfo packInfo : list) {
            String pn = packInfo.packageName;
            if (pn.contains(packageName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 查询的应用包名和给定的应用包名完全一致
     * */
    public static boolean isClientAvailableCompleteMatch(@NonNull String packageName) {
        Preconditions.checkNotNull(packageName);
        final PackageManager packageManager = getApplication().getPackageManager();
        List<PackageInfo> list = packageManager.getInstalledPackages(0);
        if (isEmpty(list)) {
            return false;
        }
        boolean result = false;
        for (PackageInfo packInfo : list) {
            String pn = packInfo.packageName;
            if (pn.equals(packageName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static void openApp(@NonNull String packageName) {
        Preconditions.checkNotNull(packageName);
        PackageManager packageManager = AppUtils.getApplication().getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        if (null != intent) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            AppUtils.getApplication().startActivity(intent);
        }
    }

    public static boolean isAppInstalled(@NonNull String packageName) {
        Preconditions.checkNotNull(packageName);
        PackageManager packageManager = AppUtils.getApplication().getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            ILog.d(TAG, "packageName : " + packageName + " not install", e);
        }

        return null != packageInfo;
    }


    // time format as path prefix
    private static SimpleDateFormat sDateFormat = null;

    public static String getCurrentTime() {
        if (null == sDateFormat) {
            sDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.CHINA);
        }

        return sDateFormat.format(new Date());
    }

    /**
     * split the create time of system message
     * when the time is below 10s show just moment
     * below 60s show second
     */
    private static SimpleDateFormat sDelayDateFormat = null;

    public static String getDelayTime(long createdTime) {
        if (null == sDelayDateFormat) {
            sDelayDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        }
        String time = null;
        long delay = (System.currentTimeMillis() - createdTime) / 1000;
        if (delay < 10) { // < 10s
            time = "now";
        } else if (delay < 60) { // < 1min
            time = delay + "m";
        } else if (delay < 3600) { // < 1hour
            time = (delay / 60) + "h";
        } else if (delay < 86400) { // < 1day
            time = (delay / 3600) + "d";
        } else {
            Date date = new Date(createdTime);
            time = sDelayDateFormat.format(date);
        }
        return time;
    }
    /*
        获取时间间隔文本
     */
    public static String getDurationHms(long duration) {
        if (duration <= 0) {
            return "00:00:00";
        }
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration - TimeUnit.HOURS.toMillis(hours));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration - TimeUnit.HOURS.toMillis(hours)
                - TimeUnit.MINUTES.toMillis(minutes));
        return String.format(Locale.CHINA, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String getDurationMs(long duration) {
        if (duration <= 0) {
            return "00:00";
        }
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration - TimeUnit.MINUTES.toMillis(minutes));
        return String.format(Locale.CHINA, "%02d:%02d", minutes, seconds);
    }

    public static String getDuration(/*ms*/long duration) {
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        if (hours > 0) {
            return getDurationHms(duration);
        } else {
            return getDurationMs(duration);
        }
    }

    public static String getFileSize(long size) {
        float size_M = (float) size / 1024 / 1024;
        if (size_M < 1000) {
            return String.format(Locale.CHINA, "%.2fM", size_M);
        } else {
            float size_G = size_M / 1024;
            return String.format(Locale.CHINA, "%.2fG", size_G);
        }
    }

    public static boolean isEmpty(CharSequence string) {
        return TextUtils.isEmpty(string) || string.equals("null") || string.equals("NULL");
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static String getFileSuffix(String filePath) {
        if (AppUtils.isEmpty(filePath)) {
            return "";
        }

        File file = new File(filePath);
        String fileName = file.getName();
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }


    public static final int TYPE_WAN = 1;
    public static final int TYPE_BAI_WAN = 2;
    private static DecimalFormat unitFormat = new DecimalFormat("#.#");

    public static String convertNumber(long number) {
        return convertNumber(number, TYPE_WAN);
    }

    public static String convertNumber(long iNumber, int type) {
        float NUM_WAN = 10000.0f;
        float NUM_BAI_WAN = 1000000.0f;
        float NUM_YI = 100000000.0f;

        switch (type) {

            case TYPE_WAN:
                if (iNumber >= NUM_YI) {
                    return unitFormat.format((iNumber / NUM_YI))+"亿";
                } else if (iNumber >= NUM_WAN) {
                    return unitFormat.format((iNumber / NUM_WAN))+"万";
                } else
                    return String.valueOf(iNumber);

            case TYPE_BAI_WAN:
                if (iNumber >= NUM_YI) {
                    return unitFormat.format((iNumber / NUM_YI))+"亿";
                } else if (iNumber >= NUM_BAI_WAN) {
                    return unitFormat.format((iNumber / NUM_BAI_WAN))+"百万";
                } else
                    return String.valueOf(iNumber);

        }
        return String.valueOf(iNumber);
    }

    /**
     * get orientation for activity
     *
     * @param activity
     * @return
     */
    public static int getActivityOrientation(@NonNull Activity activity) {
        Preconditions.checkNotNull(activity);
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }

    /**
     * hide soft input
     *
     * @param view any view
     */
    public static void hideSoftInput(@NonNull View view) {
        Preconditions.checkNotNull(view);
        InputMethodManager imm = (InputMethodManager) AppUtils.getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    /**
     * show soft input
     *
     * @param view edit text view
     */
    public static void showSoftInput(@NonNull EditText view) {
        Preconditions.checkNotNull(view);
        view.setVisibility(View.VISIBLE);
        view.setFocusable(true);
        InputMethodManager imm = (InputMethodManager) AppUtils.getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            view.requestFocus();
            imm.showSoftInput(view, 0);
        }
    }

    /**
     * index for CharSequence
     *
     * @param source
     * @param target
     * @return
     */
    public static int indexOf(@NonNull CharSequence source,
                              @NonNull CharSequence target) {
        return indexOf(source, target, 0);
    }

    public static int indexOf(@NonNull CharSequence source,
                              @NonNull CharSequence target,
                              int fromIndex) {
        Preconditions.checkNotNull(source);
        Preconditions.checkNotNull(target);
        if (fromIndex >= source.length()) {
            return (target.length() == 0 ? source.length() : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (target.length() == 0) {
            return fromIndex;
        }

        char first = target.charAt(0);
        int max = (source.length() - target.length());

        for (int i = fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (source.charAt(i) != first) {
                while (++i <= max && source.charAt(i) != first) ;
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + target.length() - 1;
                for (int k = 1; j < end && source.charAt(j)
                        == target.charAt(k); j++, k++)
                    ;

                if (j == end) {
                    /* Found whole string. */
                    return i;
                }
            }
        }
        return -1;
    }

    public static void removeForeground(@NonNull Spannable spannable) {
        removeForeground(spannable, false);
    }
    public static void removeForeground(@NonNull Spannable spannable, boolean skipStyle) {
        Preconditions.checkNotNull(spannable);
        String text = spannable.toString();
        ForegroundColorSpan[] spans = spannable.getSpans(0, text.length(), ForegroundColorSpan.class);
        if (null != spans) {
            for (ForegroundColorSpan span : spans) {
                if (skipStyle) {
                    int start = spannable.getSpanStart(span);
                    int end = spannable.getSpanEnd(span);
                    StyleSpan[] styleSpans = spannable.getSpans(start, end, StyleSpan.class);
                    if (null != styleSpans && styleSpans.length > 0) {
                        continue;
                    }
                }

                spannable.removeSpan(span);
            }
        }
    }

    /**
     * 按照时间戳生成文件名
     */
    public static String getTimestampName(@NonNull String path) {
        Preconditions.checkNotNull(path);
        return "ts_" + getCurrentTime() + "." + getFileSuffix(path);
    }

    /**
     * 按照时间戳生成文件名，指定了最终的文件后缀名
     */
    public static String getTimestampName(@NonNull String path, @NonNull String suffix) {
        Preconditions.checkNotNull(path);
        Preconditions.checkNotNull(suffix);
        return "ts_" + getCurrentTime() + "." + suffix;
    }

    // permissions
    // 此方法用来判断当前应用的辅助功能服务是否开启
    public static boolean isAccessibilitySettingsOn() {
        Context context = getApplication();
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            ILog.i(TAG, e.getMessage());
        }

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(context.getPackageName().toLowerCase());
            }
        }
        return false;
    }

    // create simple popup window
    public static PopupWindow createPopupWindow(@LayoutRes int layoutId) {
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        View contentView = inflater.inflate(layoutId, null, false);
        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        contentView.measure(measureSpec, measureSpec);
        PopupWindow popupWindow = new PopupWindow(contentView, contentView.getMeasuredWidth(),
                contentView.getMeasuredHeight());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setFocusable(true);
        return popupWindow;
    }

    private static ColorMatrixColorFilter sGrayColor = null;
    public static void setIconGray(@NonNull ImageView view) {
        Preconditions.checkNotNull(view);
        if (null == sGrayColor) {
            ColorMatrix onlineCm = new ColorMatrix();
            onlineCm.setSaturation(0);
            sGrayColor = new ColorMatrixColorFilter(onlineCm);
        }
        view.setColorFilter(sGrayColor);
    }
    public static void clearGray(@NonNull ImageView view) {
        Preconditions.checkNotNull(view);
        view.clearColorFilter();
    }

    public static void setEnable(@NonNull ViewGroup view, boolean enable) {
        Preconditions.checkNotNull(view);
        int count = view.getChildCount();
        for (int i = 0; i < count; i++) {
            View childView = view.getChildAt(i);
            if (childView instanceof ViewGroup) {
                setEnable((ViewGroup) childView, enable);
            } else {
                childView.setEnabled(enable);
            }
        }
    }

    public static void appendPhoneSplitChar(@NonNull StringBuilder target, CharSequence source, @NonNull char splitChar) {
        if (source == null || source.length() == 0)
            return;
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(splitChar);
        for (int i = 0; i < source.length(); i++) {//添加分割符
            if (i != 3 && i != 8 && source.charAt(i) == splitChar) {
                continue;
            } else {
                target.append(source.charAt(i));
                if ((target.length() == 4 || target.length() == 9)
                        && target.charAt(target.length() - 1) != splitChar) {
                    target.insert(target.length() - 1, splitChar);
                }
            }
        }
    }

    /**
     * 判断Uri对应的图片是否是gif格式：
     * 1.当uri对应的是本地图片文件时，根据图片文件的格式，通过取出文件头的类型信息来做判断；
     * 2.当uri对应的是网络等其它来源图片时，根据后缀名.gif来判断，该情况下判断机制有待进一步优化。
     */
    public static boolean isGifUrl(String uri) {
        if (AppUtils.isEmpty(uri)) {
            return false;
        }
        boolean flag = uri.toLowerCase().endsWith(".gif");
        if (!flag) {
            Uri tempUri = Uri.parse(uri);
            String scheme = tempUri.getScheme();
            if (null != scheme && scheme.equalsIgnoreCase("file")) {
                String value = tempUri.getAuthority();
                if (AppUtils.isEmpty(value)) {
                    value = tempUri.getPath();
                }
                if (!AppUtils.isEmpty(value)) {
                    String type = getImageType(value);
                    flag = null != type && type.equalsIgnoreCase("gif");
                }
            }
        }
        return flag;
    }

    /**
     * 判断图片类型(GIF,PNG,JPG)
     * @return
     */
    public static String getImageType(String path) {
        String type = null;
        if (!isEmpty(path)) {
            FileInputStream imgFile = null;
            byte[] b = new byte[10];
            int l = -1;
            try {
                imgFile = new FileInputStream(path);
                l = imgFile.read(b);
                imgFile.close();
                if (l == 10) {
                    byte b0 = b[0];
                    byte b1 = b[1];
                    byte b2 = b[2];
                    byte b3 = b[3];
                    byte b6 = b[6];
                    byte b7 = b[7];
                    byte b8 = b[8];
                    byte b9 = b[9];
                    if (b0 == (byte) 'G' && b1 == (byte) 'I' && b2 == (byte) 'F') {
                        type = "gif";
                    } else if (b1 == (byte) 'P' && b2 == (byte) 'N' && b3 == (byte) 'G') {
                        type = "png";
                    } else if (b6 == (byte) 'J' && b7 == (byte) 'F' && b8 == (byte) 'I' && b9 == (byte) 'F') {
                        type = "jpeg";
                    }
                }
            } catch(Exception e){
                ILog.e(TAG, "getImageType", e);
            }
        }

        return type;
    }

    public static boolean checkSelfPermission(Context context, String permission){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            return PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED;
        }
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 判断某个Activity 界面是否在前台
     * @param className 某个界面名称
     * @return
     */
    public static boolean  isForeground(String className) {
        Context context = AppUtils.getApplication();
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean compileLetter(String s) {
        Pattern p = Pattern.compile("[a-zA-Z]");
        Matcher m = p.matcher(s);
        return m.matches();
    }
}
