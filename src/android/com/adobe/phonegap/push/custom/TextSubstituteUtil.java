package com.adobe.phonegap.push;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.eightbhs.core.util.GsonUtil;
import com.eightbhs.personal_safety.BuildConfig;
import com.google.gson.annotations.Expose;

import static com.adobe.phonegap.push.PushConstants.MESSAGE;
import static com.adobe.phonegap.push.PushConstants.TITLE;

public class TextSubstituteUtil {
    private static final String TAG = "TextSubstituteUtil";

    private static final String TextParserPref = "Push_TextParser";
    private static final String Pref_TitleParserProp = "titleParser";
    private static final String Pref_MessageParserProp = "messageParser";

    private static final String Extras_AltTitle = "alt_title";
    private static final String Extras_AltMsg = "alt_message";

    private static TextSubstituteUtil instance = null;

    public static TextSubstituteUtil getInstance(Context appContext) {
        if (instance == null) {
            instance = new TextSubstituteUtil(appContext);
        }

        return instance;
    }

    private Context appContext;

    private TextSubstituteUtil(Context appContext) {
        this.appContext = appContext;
        this.setParsers();
    }

    private void setParsers() {
        SharedPreferences prefs = this.appContext.getSharedPreferences(TextParserPref, Context.MODE_PRIVATE);
        String titleParserClass = prefs.getString(Pref_TitleParserProp, "");
        this.titleParser = this.getParser(titleParserClass);

        String messageParserClass = prefs.getString(Pref_MessageParserProp, "");
        this.messageParser = this.getParser(messageParserClass);
    }

    private ITextSubstitute getParser(String className) {
        try {
            return (ITextSubstitute) Class.forName(className).newInstance();
        }
        catch (Exception ex) {
            Crashlytics.logException(ex);
            return null;
        }
    }

    private ITextSubstitute titleParser = null;
    private ITextSubstitute messageParser = null;

    void updateTitle(Bundle extras) {
        if (titleParser == null) {
            Log.w(TAG, "No title parser");
            return;
        }

        try {
            String title = extras.getString(TITLE);
            if (title == null) {
                title = "";
            }

            String altTitle = this.getAltValue(extras, Extras_AltTitle, title);
            if (title.equals(altTitle)) {
                Log.d(TAG, "No alt title");
                return;
            }

            Log.d(TAG, "altTitle: " + altTitle);
            String updatedTitle = titleParser.parse(altTitle, extras, this.appContext);
            Log.d(TAG, "updatedTitle: " + updatedTitle);

            extras.putString(TITLE, updatedTitle);
            Log.d(TAG, "Title updated");
        }
        catch (Exception e) {
            Crashlytics.logException(e);
            Log.e(TAG, "Unable to update title", e);
        }
    }


    void updateMessage(Bundle extras) {
        if (messageParser == null) {
            Log.w(TAG, "No title parser");
            return;
        }

        try {
            String msg = extras.getString(MESSAGE);
            if (msg == null) {
                msg = "";   // avoid null exception
            }

            String altMsg = this.getAltValue(extras, Extras_AltMsg, msg);
            if (msg.equals(altMsg)) {
                Log.d(TAG, "No alt message");
                return;
            }

            Log.d(TAG, "altMsg: " + altMsg);
            String updatedMsg = messageParser.parse(altMsg, extras, this.appContext);
            Log.d(TAG, "updatedMessage: " + updatedMsg);

            extras.putString(MESSAGE, updatedMsg);
            Log.d(TAG, "Message updated");
        }
        catch (Exception e) {
            Crashlytics.logException(e);
            Log.e(TAG, "Unable to update message", e);
        }
    }

    private String getAltValue(Bundle extras, String altPropName, String defaultValue) {
        String json = extras.getString(altPropName);
        if (json == null) {
            Log.d(TAG, "No alt value for: " + altPropName);
            return defaultValue;
        }

        try {
            String latestValue = defaultValue;

            AltValue[] altValues = GsonUtil.get().fromJson(json, AltValue[].class);
            for (AltValue altTitle : altValues) {
                if (BuildConfig.VERSION_CODE >= altTitle.version) {
                    latestValue = altTitle.value;
                }
            }

            return latestValue;
        }
        catch (Exception e) {
            Crashlytics.logException(e);
            return defaultValue;
        }
    }

    private static class AltValue {
        @Expose
        String client;
        @Expose
        String value;

        private int version = 0;

        public int getVersion() {
            if (version == 0) {
                String[] parts = client.split(".");
                // Expect version to be 3 parts
                if (parts.length == 3) {
                    version = (Integer.parseInt(parts[0]) * 10000) + (Integer.parseInt(parts[1]) * 100) + Integer.parseInt(parts[2]);
                }
            }
            return version;
        }
    }
}