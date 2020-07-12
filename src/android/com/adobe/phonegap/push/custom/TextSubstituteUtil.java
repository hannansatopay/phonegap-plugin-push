package com.adobe.phonegap.push;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;

import static com.adobe.phonegap.push.PushConstants.MESSAGE;
import static com.adobe.phonegap.push.PushConstants.TITLE;

public class TextSubstituteUtil {

    private static String TextParserPref = "Push_TextParser";
    private static String Pref_TitleParserProp = "titleParser";
    private static String Pref_MessageParserProp = "messageParser";

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
        // TODO: For backward compatibility, the server will have to send static title
        // Then at a later date, I can force an app update, when most people app have updated
        //
        // Interim, there can be alternative title in the extra payload which can be parsed
        return extras.getString(TITLE);
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
        // TODO: For backward compatibility, the server will have to send static message, with alternative parsable message

        return extras.getString(MESSAGE);
    }

    private void setMessage(Bundle extras, String message) {
        extras.putString(MESSAGE, message);
    }
}