package com.smart.android.smartandroid.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;

import com.smart.android.smartandroid.base.AppEngine;

/**
 * Created by liujia on 2017/10/12.
 */

public class UriHelper {
    @SuppressLint("NewApi")
    public static String getPath(final Uri uri) {

        final boolean isKitKat = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT;
        final Context context = AppEngine.getInstance().getAppContext();

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    //从文件路径获得媒体库的uri
    public static Uri realPathToMediaUri(String path) {
        Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = AppEngine.getInstance().getAppContext().getContentResolver().query(mediaUri,
                null,
                MediaStore.Images.Media.DATA + "='" + path + "'",
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            Uri uri = ContentUris.withAppendedId(mediaUri, id);
            return uri;
        }

        return null;
    }

    public static String getMimeType(Uri uri) {
        if (uri == null) {
            return "";
        }
        ContentResolver resolver = AppEngine.getInstance().getAppContext().getContentResolver();
        if (resolver != null) {
            String type = resolver.getType(uri);
            if (type != null && !type.isEmpty()) {
                return type;
            }
        }

        //ContentResolver不靠谱，哥只能自己手工解析了
        String uriString = uri.toString();
        int dotIndex = uriString.lastIndexOf(".");
        if (dotIndex != -1) {
            String fileExt = uriString.substring(dotIndex + 1);
            fileExt = fileExt.toLowerCase();
            if (fileExt.compareTo("jpg") == 0) {
                return "image/jpeg";
            } else if (fileExt.compareTo("png") == 0) {
                return "image/png";
            } else if (fileExt.compareTo("gif") == 0) {
                return "image/gif";
            } else if (fileExt.compareTo("txt") == 0) {
                return "text/plain";
            } else if (fileExt.compareTo("html") == 0) {
                return "text/html";
            } else if (fileExt.compareTo("rtf") == 0) {
                return "application/rtf";
            } else if (fileExt.compareTo("midi") == 0) {
                return "audio/midi";
            } else if (fileExt.compareTo("mpg") == 0 || fileExt.compareTo("mpeg") == 0) {
                return "video/mpeg";
            } else if (fileExt.compareTo("avi") == 0) {
                return "video/x-msvideo";
            } else if (fileExt.compareTo("gz") == 0) {
                return "application/x-gzip";
            } else if (fileExt.compareTo("tar") == 0) {
                return "application/x-tar";
            }
        }

        Uri mediaStoreUri = getMediaStoreUri(uri);
        if (mediaStoreUri != null) {
            if (resolver != null) {
                String typeMediaStoreUri = resolver.getType(uri);
                if (typeMediaStoreUri != null && !typeMediaStoreUri.isEmpty()) {
                    return typeMediaStoreUri;
                }
            }
        }

        return "image/jpeg";
    }

    //哥忍住不用F开头单词命名这个函数
    public static Uri getMediaStoreUri(Uri sourceUri) {
        String uriPath = getPath(sourceUri);
        if (uriPath != null && !uriPath.isEmpty()) {
            return realPathToMediaUri(uriPath);
        }

        return null;
    }

    public static String queryParamFromUrl(String url, String key) {
        String param = Uri.parse(url).getQueryParameter(key);
        if (param != null) {
            return param;
        }

        return "";
    }

    public static String queryParamFromUrl(Uri uri, String key) {
        if (uri == null) {
            return "";
        }

        String param = uri.getQueryParameter(key);
        if (param != null) {
            return param;
        }

        return "";
    }

    private static File uriToFileInternal(Uri uri) {
        String resourceString = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = AppEngine.getAppContext().getContentResolver().query(uri, proj, null, null, null);
        if (cursor == null) {
            return null;
        }

        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            resourceString = cursor.getString(column_index);
        }
        cursor.close();

        if (TextUtils.isEmpty(resourceString)) {
            return null;
        }

        return new File(resourceString);
    }

    public static File uriToFile(Uri uri) {
        if (uri == null) {
            return null;
        }

        File file = uriToFileInternal(uri);
        if (file != null) {
            return file;
        }

        String path = getPath(uri);
        if (TextUtils.isEmpty(path)) {
            return null;
        }

        return new File(path);
    }
}
