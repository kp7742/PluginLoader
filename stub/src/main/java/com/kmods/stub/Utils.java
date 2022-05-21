package com.kmods.stub;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.io.File;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
class Utils {
    static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory == null)
            return;

        if (fileOrDirectory.isDirectory()){
            if(fileOrDirectory.listFiles() == null)
                return;

            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        }

        fileOrDirectory.delete();
    }

    static void triggerRebirth(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }
}
