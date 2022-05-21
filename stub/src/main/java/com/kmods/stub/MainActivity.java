package com.kmods.stub;

import static com.kmods.stub.Reflector.getFieldValue;
import static com.kmods.stub.Reflector.invokeMethod;
import static com.kmods.stub.Reflector.setFieldValue;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.ArrayMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import dalvik.system.DexClassLoader;
import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class MainActivity extends Activity {
    private Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = Prefs.with(this);

        if(!prefs.contains(Const.SVERSION)){
            prefs.write(Const.SVERSION, BuildConfig.VERSION_NAME);
        }

        if(prefs.contains(Const.PLUG_DIR) && prefs.contains(Const.PVERSION)){
            launchLoader();
        } else {
            if (isInternetAvailable()) {
                downloadLoader();
            } else {
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle(Const.APP_NAME)
                        .setMessage("Please connect to the Internet! To Download and Run " + Const.PLUGIN_NAME + ".")
                        .setNegativeButton("OK", (d, w) -> finish())
                        .show();
            }
        }
    }

    private void launchLoader() {
        // Fix for Quirky internet problem when launching plugin in Virtual Apps
        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(Const.UPDATE_LINK).openConnection();
                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(true);
                connection.setConnectTimeout(100);
                connection.setReadTimeout(100);
                connection.setRequestProperty("Connection", "close");
                connection.connect();
                connection.disconnect();
            } catch (IOException ignored) {}
        }).start();

        // Loads plugin and launch it
        File loaderPath = new File(prefs.read(Const.PLUG_DIR), Const.PLUG_NAME);
        if(loaderPath.exists()) {
            try {
                changeClassLoader(loaderPath.getAbsolutePath());
                ResourcePatcher.Patch(this, loaderPath.getAbsolutePath());
                Application app = changeTopApplication(a.a.class.getName());
                if (app != null) {
                    app.onCreate();
                }
                startActivity(new Intent(MainActivity.this, a.b.class));
                finish();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    public void downloadLoader() {
        ProgressDialog dialog = ProgressDialog.show(this, Const.APP_NAME, "Fetching Data", true);
        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(Const.UPDATE_LINK).openConnection();
                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("Connection", "close");
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    throw new IOException("Request Code Not 200");
                }

                JSONObject update;

                InputStream inputStream = connection.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] bArr = new byte[8192];
                while (true) {
                    long read = inputStream.read(bArr, 0, 8192);
                    if (read != -1) {
                        byteArrayOutputStream.write(bArr, 0, (int) read);
                    } else {
                        inputStream.close();
                        connection.disconnect();
                        update = new JSONObject(byteArrayOutputStream.toString("UTF-8"));
                        break;
                    }
                }

                final String newSVer = update.getJSONObject(Const.STUB).getString(Const.VERSION);
                final String currSVer = prefs.read(Const.SVERSION, BuildConfig.VERSION_NAME);

                if(!currSVer.equals(newSVer)){
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        new AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setTitle(Const.APP_NAME)
                                .setMessage(String.format("New %s Update v%s Came, Please Download it from %s", Const.APP_NAME, newSVer, Const.SITE_LINK))
                                .setNegativeButton("Download Now", (d, w) -> {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Const.SITE_LINK)));
                                    finish();
                                })
                                .show();
                    });
                } else {
                    runOnUiThread(() -> dialog.setMessage("Downloading Plugin"));

                    String version = update.getJSONObject(Const.PLUGIN).getString(Const.VERSION);
                    String dlLink = update.getJSONObject(Const.PLUGIN).getString(Const.URL);

                    File loaderDir = new File(getFilesDir(), Const.PLUGIN);
                    if(loaderDir.exists()){
                        Utils.deleteRecursive(loaderDir);
                    }
                    loaderDir.mkdir();

                    if(!prefs.contains(Const.PLUG_DIR)){
                        prefs.write(Const.PLUG_DIR, loaderDir.getAbsolutePath());
                    }

                    File loaderPath = new File(loaderDir, Const.PLUG_NAME);

                    connection = (HttpURLConnection) new URL(dlLink).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setInstanceFollowRedirects(true);
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    connection.setRequestProperty("Connection", "close");
                    connection.connect();
                    responseCode = connection.getResponseCode();
                    if (responseCode != 200) {
                        throw new IOException("Request Code Not 200");
                    }

                    InputStream bStream = connection.getInputStream();
                    FileOutputStream fileOut = new FileOutputStream(loaderPath);

                    byte[] data = new byte[1024];
                    long total = 0;
                    int count;
                    while ((count = bStream.read(data)) != -1) {
                        total += count;
                        long finalTotal = total;
                        runOnUiThread(() -> {
                            float curr = (float)(finalTotal / 1024) / 1024;
                            String txt = String.format(Locale.getDefault(),"Downloading Plugin ( %.2fMB )", curr);
                            dialog.setMessage(txt);
                        });
                        fileOut.write(data, 0, count);
                    }

                    bStream.close();
                    fileOut.flush();
                    fileOut.close();
                    connection.disconnect();

                    runOnUiThread(() -> {
                        dialog.dismiss();
                        prefs.write(Const.PVERSION, version);
                        new AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setTitle(Const.APP_NAME)
                                .setMessage("Download Successful, Click OK to Restart App.")
                                .setNegativeButton("OK", (d, w) -> Utils.triggerRebirth(this))
                                .show();
                    });
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    dialog.dismiss();
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle(Const.APP_NAME)
                            .setMessage("Failed To Download " + Const.PLUGIN_NAME + ", Please Try Again!")
                            .setNegativeButton("OK", (d, w) -> finish())
                            .show();
                });
            }
        }).start();
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //ClassLoader Patch
    private void changeClassLoader(String apkPath){
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            Object activityThread = currentActivityThreadMethod.invoke(null);
            Field mPackagesField = activityThreadClass.getDeclaredField("mPackages");
            mPackagesField.setAccessible(true);
            ArrayMap mPackages = (ArrayMap) mPackagesField.get(activityThread);
            WeakReference<?> loadedApkRef = (WeakReference) mPackages.get(getPackageName());
            Class<?> loadedApkClass = Class.forName("android.app.LoadedApk");
            ClassLoader newCL = createDexLoader(apkPath, getClassLoader().getParent());
            setAppClassLoader(loadedApkClass, loadedApkRef, newCL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Application changeTopApplication(String appClassName) {
        Object currentActivityThread = invokeMethod("android.app.ActivityThread", null,
                "currentActivityThread", new Object[]{});
        Object mBoundApplication = getFieldValue("android.app.ActivityThread", currentActivityThread, "mBoundApplication");
        Object loadedApkInfo = getFieldValue("android.app.ActivityThread$AppBindData", mBoundApplication, "info");
        setFieldValue("android.app.LoadedApk", loadedApkInfo, "mApplication", null);
        Object oldApplication = getFieldValue("android.app.ActivityThread", currentActivityThread, "mInitialApplication");
        ArrayList<Application> mAllApplications = (ArrayList<Application>)
                getFieldValue("android.app.ActivityThread", currentActivityThread, "mAllApplications");
        if (mAllApplications != null) {
            mAllApplications.remove(oldApplication);
        }
        ApplicationInfo loadedApk = (ApplicationInfo) getFieldValue("android.app.LoadedApk", loadedApkInfo, "mApplicationInfo");
        ApplicationInfo appBindData = (ApplicationInfo) getFieldValue("android.app.ActivityThread$AppBindData",
                mBoundApplication, "appInfo");
        loadedApk.className = appClassName;
        appBindData.className = appClassName;
        Application app = (Application) invokeMethod(
                "android.app.LoadedApk", loadedApkInfo, "makeApplication",
                new Object[]{false, null},
                boolean.class, Instrumentation.class);
        setFieldValue("android.app.ActivityThread", currentActivityThread, "mInitialApplication", app);
        return app;
    }

    private void setAppClassLoader(Class<?> loadedApkClass, WeakReference<?> loadedApkRef, ClassLoader newClassLoader) {
        try {
            Field mClassLoaderField = loadedApkClass.getDeclaredField("mClassLoader");
            mClassLoaderField.setAccessible(true);
            mClassLoaderField.set(loadedApkRef.get(), newClassLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ClassLoader createDexLoader(String appPath, ClassLoader parent) {
        try {
            File odexDir = getCacheDir();
            String libPath = getApplicationInfo().nativeLibraryDir;
            ClassLoader dexClassLoader;
            if (parent != null) {
                dexClassLoader = new DexClassLoader(appPath, odexDir.getAbsolutePath(), libPath, parent);
            } else {
                dexClassLoader = new DexClassLoader(appPath, odexDir.getAbsolutePath(), libPath, getClassLoader());
            }
            return dexClassLoader;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
