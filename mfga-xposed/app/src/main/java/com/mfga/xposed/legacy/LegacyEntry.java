package com.mfga.xposed.legacy;

import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;

import com.mfga.xposed.FontForceCore;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 旧版 Xposed API 入口
 * 通过 assets/xposed_init 声明。
 */
public class LegacyEntry implements IXposedHookLoadPackage {

    private static final String TAG = "MFGA";
    private static final java.util.Set<String> TARGET_PACKAGES = new java.util.HashSet<>(
            java.util.Arrays.asList("com.github.android", "com.twitter.android", "org.telegram.messenger", "xyz.nextalone.nagram", "com.zhiliaoapp.musically", "com.google.android.youtube", "com.reddit.frontpage"));

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!TARGET_PACKAGES.contains(lpparam.packageName)) {
            return;
        }
        Log.i(TAG, "MFGA v1.5 (legacy) attach: " + lpparam.packageName);

        XC_MethodHook replaceWithSystemFont = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (FontForceCore.isReplacing()) {
                    // 防止我们自己生成替换字体时又被同一个 hook 拦截，造成死循环
                    return;
                }
                Object result = param.getResult();
                Typeface original = (result instanceof Typeface) ? (Typeface) result : null;
                param.setResult(FontForceCore.systemReplacementFor(original));
            }
        };

        ClassLoader cl = lpparam.classLoader;

        // 单字体文件路径：Android 26+ 上 createFromAsset / createFromFile 内部
        // 最终都会走到 Typeface.Builder#build()
        try {
            XposedHelpers.findAndHookMethod(
                    "android.graphics.Typeface$Builder", cl,
                    "build", replaceWithSystemFont);
        } catch (Throwable t) {
            Log.w(TAG, "hook Typeface.Builder#build failed", t);
        }

        // 多字重 font-family 路径（比如 res/font/inter.xml 这种声明了多个字重/斜体
        // 变体的 family）：Android 29+ 上系统实际走的是
        // Typeface.CustomFallbackBuilder#build()，不经过上面的 Typeface.Builder。
        try {
            XposedHelpers.findAndHookMethod(
                    "android.graphics.Typeface$CustomFallbackBuilder", cl,
                    "build", replaceWithSystemFont);
        } catch (Throwable t) {
            Log.w(TAG, "hook Typeface.CustomFallbackBuilder#build failed", t);
        }

        // 兜底：部分老代码路径可能不经过 Builder，直接补几个静态工厂方法
        try {
            XposedHelpers.findAndHookMethod(
                    Typeface.class, "createFromAsset",
                    android.content.res.AssetManager.class, String.class,
                    replaceWithSystemFont);
        } catch (Throwable ignored) {
        }
        try {
            XposedHelpers.findAndHookMethod(
                    Typeface.class, "createFromFile",
                    java.io.File.class, replaceWithSystemFont);
        } catch (Throwable ignored) {
        }
        try {
            XposedHelpers.findAndHookMethod(
                    Typeface.class, "createFromFile",
                    String.class, replaceWithSystemFont);
        } catch (Throwable ignored) {
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                XposedHelpers.findAndHookMethod(
                        Typeface.class, "create",
                        Typeface.class, int.class, boolean.class,
                        replaceWithSystemFont);
            } catch (Throwable t) {
                Log.w(TAG, "hook Typeface.create(Typeface,int,boolean) failed", t);
            }
        }
        try {
            XposedHelpers.findAndHookMethod(
                    Typeface.class, "create",
                    Typeface.class, int.class,
                    replaceWithSystemFont);
        } catch (Throwable t) {
            Log.w(TAG, "hook Typeface.create(Typeface,int) failed", t);
        }
    }
}
