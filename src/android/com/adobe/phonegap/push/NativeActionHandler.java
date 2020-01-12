package com.adobe.phonegap.push;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.*;

public class NativeActionHandler 
{
  	private static final String LOG_TAG = "Push_NativeActionHandler";

	public static final String HandlerPreferences = "Push_HandlerRegistry";

	public static final String Prop_Action = "action";
	
	public static void handleNativeAction(Context context, String nativeActionPayload, int notId) {
        Log.d(LOG_TAG, "Handling Native Action");

		try {
            JSONObject json = new JSONObject(nativeActionPayload);
            String action = json.getString(Prop_Action);
            Intent i = getIntent(context, action);
			if( i == null) {
            	Log.w(LOG_TAG, "Action did not have an handler: " + action);
				return;
			}

			i.putExtra("payload", nativeActionPayload);
			i.putExtra("notId", notId);

			context.startService(i);
        }
        catch(JSONException e) {
            Log.e(LOG_TAG, "Unable to handle native action", e);
        }
  	}

	private static Intent getIntent(Context context, String action) {
      	SharedPreferences prefs = context.getSharedPreferences(HandlerPreferences, Context.MODE_PRIVATE);

		String intentName = prefs.getString(action, "");
		if(intentName == "") {
			return null;
		}

		Intent i = new Intent();
		i.setClassName(context, intentName);
        return i;
	}
}