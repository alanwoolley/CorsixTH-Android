package uk.co.armedpineapple.cth;

import android.content.*;
import android.util.*;
import android.widget.*;

public class Reporting {
    private static final String TAG = "CorsixTH";

    public static void reportWithToast(Context ctx, String msg, Exception e) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
        Log.w(TAG, msg, e);
    }

    public static void event(String msg) {
        Log.i(TAG, msg);
    }

    public static void report(Exception e) {
        Log.e(TAG, "Exception", e);

    }

    public static void report(String log, Exception e) {
        Log.e(TAG, log, e);
    }

    public static void setBool(String key, boolean value) {
    }

    public static void setDouble(String key, double value) {
    }

    public static void setFloat(String key, float value) {
    }

    public static void setInt(String key, int value) {
    }

    public static void setLong(String key, long value) {
    }

    public static Logger getLogger(String tag) {
        return new Logger(tag);
    }

    public static class Logger {

        private final String tag;

        private Logger(String tag) {
            this.tag = tag;
        }

        public void d(String msg) {

            Log.d(tag, msg);

        }

        public void e(String msg) {
            Log.e(tag, msg);

        }

        public void i(String msg) {

            Log.i(tag, msg);
        }

        public void w(String msg) {
            Log.w(tag, msg);
        }

        public void v(String msg) {
            Log.v(tag, msg);

        }
    }
}
