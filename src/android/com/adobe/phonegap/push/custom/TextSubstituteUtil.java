package com.adobe.phonegap.push;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.eightbhs.core.util.GsonUtil;
import com.eightbhs.personal_safety.BuildConfig;
import com.google.gson.annotations.Expose;

import static com.adobe.phonegap.push.PushConstants.MESSAGE;
import static com.adobe.phonegap.push.PushConstants.TITLE;

public class TextSubstituteUtil {

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
            return;
        }

        String title = getTitle(extras);
        setTitle(extras, titleParser.parse(title, extras, this.appContext));
    }

    private String getTitle(Bundle extras) {
        String title = extras.getString(TITLE);
        return this.getAltValue(extras, Extras_AltTitle, title);
    }

    private void setTitle(Bundle extras, String title) {
        extras.putString(TITLE, title);
    }

    void updateMessage(Bundle extras) {
        if (messageParser == null) {
            return;
        }

        String message = getMessage(extras);
        setMessage(extras, messageParser.parse(message, extras, this.appContext));
    }

    private String getMessage(Bundle extras) {
        String msg = extras.getString(MESSAGE);
        return this.getAltValue(extras, Extras_AltMsg, msg);
    }

    private void setMessage(Bundle extras, String message) {
        extras.putString(MESSAGE, message);
    }

    private String getAltValue(Bundle extras, String altPropName, String defaultValue) {
        String latestValue = defaultValue;

        String json = extras.getString(altPropName);
        if (json == null) {
            return latestValue;
        }

        AltValue[] altValues = GsonUtil.get().fromJson(json, AltValue[].class);
        for (AltValue altTitle : altValues) {
            if (BuildConfig.VERSION_CODE >= altTitle.version) {
                latestValue = altTitle.value;
            }
        }

        return latestValue;
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