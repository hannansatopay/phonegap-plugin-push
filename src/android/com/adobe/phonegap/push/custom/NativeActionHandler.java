package com.adobe.phonegap.push;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint("LongLogTag")
public class NativeActionHandler {
    private static final String LOG_TAG = "Push_NativeActionHandler";

    public static final String HandlerPreferences = "Push_HandlerRegistry";

    public static final String Prop_Action = "action";

    public static final String INTENT_PROP_PAYLOAD = "payload";
    public static final String INTENT_PROP_NOT_ID = "notId";
    public static final String INTENT_PROP_RECEIVER_ID = "receiverId";

    private static final String FCM_PROP_ACTION_PAYLOAD = "b_action";
    private static final String FCM_PROP_NATIVE = "native";
    private static final String FCM_PROP_RECEIVER_ID = "receiverId";

    private Context context;

    public NativeActionHandler(Context appContext) {
        this.context = appContext;
    }

    public void dispose() {
        this.context = null;
    }

    public void process(Bundle extras, int notId) {
        String actionPayload = extras.getString(FCM_PROP_ACTION_PAYLOAD, null);
        if (actionPayload == null) {
            Log.d(LOG_TAG, "process(): No action payload");
            return;
        }

        JSONObject json;
        try {
            Log.d(LOG_TAG, "process(): action payload - " + actionPayload);
            json = new JSONObject(actionPayload);
        } catch (JSONException e) {
            // Shouldn't happen
            Log.e(LOG_TAG, "process(): Action payload is not json object", e);
            return;
        }

        try {
            String receiverId = extras.getString(FCM_PROP_RECEIVER_ID);
            String action = json.getString(Prop_Action);
            this.handleNativeAction(action, actionPayload, notId, receiverId);
        }
		catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to handle native action", e);
        }
    }

    private void handleNativeAction(String action, String actionPayload, int notId, String receiverId) {
        Log.d(LOG_TAG, "Handling Native Action");

        Intent i = this.getIntent(action);
        if (i == null) {
            Log.w(LOG_TAG, "No native handler for - " + action);
            return;
        }

        i.putExtra(INTENT_PROP_PAYLOAD, actionPayload);
        i.putExtra(INTENT_PROP_NOT_ID, notId);
        i.putExtra(INTENT_PROP_RECEIVER_ID, receiverId);

        this.context.startService(i);
    }

    private Intent getIntent(String action) {
        SharedPreferences prefs = this.context.getSharedPreferences(HandlerPreferences, Context.MODE_PRIVATE);

        String intentName = prefs.getString(action, "");
        if (intentName.equals("")) {
            return null;
        }

        Intent i = new Intent();
        i.setClassName(this.context, intentName);
        return i;
    }
}