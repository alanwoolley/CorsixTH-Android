package uk.co.armedpineapple.cth;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

public class Reporting {

    public static void reportWithToast(Context ctx, String msg,  Exception e) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
        report(e);
    }

    public static void event(String msg) {
        Crashlytics.getInstance().core.log(msg);
    }

    public static void report(Exception e) {
        Crashlytics.getInstance().core.logException(e);
    }

    public static void setBool(String key, boolean value) {
        Crashlytics.getInstance().core.setBool(key, value);
    }
    public static void setDouble(String key, double value) {
        Crashlytics.getInstance().core.setDouble(key, value);
    }
    public static void setFloat(String key, float value) {
        Crashlytics.getInstance().core.setFloat(key, value);
    }
    public static void setInt(String key, int value) {
        Crashlytics.getInstance().core.setInt(key, value);
    }
    public static void setLong(String key, long value) {
        Crashlytics.getInstance().core.setLong(key, value);
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
            Crashlytics.log(Log.DEBUG, tag, msg);
        }
        public void e(String msg) {
            Crashlytics.log(Log.ERROR, tag, msg);
        }
        public void i(String msg) {
            Crashlytics.log(Log.INFO, tag, msg);
        }
        public void w(String msg) { Crashlytics.log(Log.WARN, tag, msg); }
        public void v(String msg) { Crashlytics.log(Log.VERBOSE, tag, msg); }
    }
}
