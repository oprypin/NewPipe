/*
 * Created by k3b on 07.01.2016.
 *
 * Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
 * NewPipeSettings.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.schabi.newpipe.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import org.schabi.newpipe.R;

import java.io.File;
import java.util.List;

/**
 * Helper for global settings
 */

/*
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * NewPipeSettings.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

public class NewPipeSettings {

    private NewPipeSettings() {
    }

    /**
     * Indicates if is possible pick a directory though the Storage Access Framework.
     * {@code true} if at least one provider can handle {@link Intent#ACTION_OPEN_DOCUMENT_TREE}
     * otherwise {@code false}
     */
    public static boolean hasOpenDocumentTreeSupport = false;

    /**
     * Indicates if is possible create a file though the Storage Access Framework.
     * {@code true} if at least one provider can handle {@link Intent#ACTION_CREATE_DOCUMENT}
     * otherwise {@code false}
     */
    public static boolean hasCreateDocumentSupport = false;

    public static void initSettings(Context context) {
        PreferenceManager.setDefaultValues(context, R.xml.appearance_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.content_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.download_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.history_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.main_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.video_audio_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.debug_settings, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            hasOpenDocumentTreeSupport = testFor(context, Intent.ACTION_OPEN_DOCUMENT_TREE, false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            hasCreateDocumentSupport = testFor(context, Intent.ACTION_CREATE_DOCUMENT, true);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || !hasOpenDocumentTreeSupport) {
            getVideoDownloadFolder(context);
            getAudioDownloadFolder(context);
        }
    }

    private static void getVideoDownloadFolder(Context context) {
        getDir(context, R.string.download_path_video_key, Environment.DIRECTORY_MOVIES);
    }

    private static void getAudioDownloadFolder(Context context) {
        getDir(context, R.string.download_path_audio_key, Environment.DIRECTORY_MUSIC);
    }

    private static void getDir(Context context, int keyID, String defaultDirectoryName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String key = context.getString(keyID);
        String downloadPath = prefs.getString(key, null);
        if ((downloadPath != null) && (!downloadPath.isEmpty())) return;

        SharedPreferences.Editor spEditor = prefs.edit();
        spEditor.putString(key, getNewPipeChildFolderPathForDir(getDir(defaultDirectoryName)));
        spEditor.apply();
    }

    @NonNull
    public static File getDir(String defaultDirectoryName) {
        return new File(Environment.getExternalStorageDirectory(), defaultDirectoryName);
    }

    private static String getNewPipeChildFolderPathForDir(File dir) {
        return new File(dir, "NewPipe").toURI().toString();
    }

    private static boolean testFor(@NonNull Context ctx, @NonNull String intentAction, boolean isFile) {
        Intent queryIntent = new Intent(intentAction)
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        if (isFile) {
            queryIntent.setType("*/*");
            queryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        List<ResolveInfo> infoList = ctx.getPackageManager()
                .queryIntentActivities(queryIntent, PackageManager.MATCH_DEFAULT_ONLY);

        int availableProviders = 0;
        for (ResolveInfo info : infoList) {
            if (info.activityInfo.exported) availableProviders++;
        }

        return availableProviders > 0;
    }
}
