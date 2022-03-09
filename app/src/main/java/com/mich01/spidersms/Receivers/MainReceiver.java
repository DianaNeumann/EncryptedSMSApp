package com.mich01.spidersms.Receivers;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.mich01.spidersms.Crypto.IDManagementProtocol;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.UI.ChatActivity;
import com.mich01.spidersms.UI.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class MainReceiver extends BroadcastReceiver {

    @SuppressLint("NotifyDataSetChanged")
    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String senderNum = null;
        String message =null;
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
        {
            // Retrieves a map of extended data from the intent.
            final Bundle bundle = intent.getExtras();
            try {
                if (bundle != null) {
                    final Object[] pdusObj = (Object[]) bundle.get("pdus");
                    assert pdusObj != null;
                    for (Object aPdusObj : pdusObj)
                    {
                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                        senderNum = currentMessage.getDisplayOriginatingAddress();
                        message = currentMessage.getDisplayMessageBody();
                    } // end for loop
                } // bundle is null
            } catch (Exception ignored) {
            }
            assert message != null;
            if(message.contains("|>"))
            {
                String DecryptedSMS = message;//.replace("|>","");
                DecryptedSMS = IDManagementProtocol.Decode(DecryptedSMS);
                try {
                    JSONObject smsJSON = new JSONObject(DecryptedSMS);
                    senderNum = smsJSON.getString("target");
                    message = smsJSON.getString("Body");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("SpiderMan context", "TRIGGERED here: " );
                new DBManager(context).updateLastMessage(senderNum, message, 0, 0);
                new DBManager(context).AddChatMessage(senderNum,1,message,false);
                ChatActivity.PopulateChatView(context);
                ChatActivity.messageAdapter.notifyDataSetChanged();
                HomeActivity.PopulateChats(context);
                HomeActivity.adapter.notifyDataSetChanged();
                Toast.makeText(context, "Message from: "+senderNum,Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Log.i("Intent Received: ",intent.getAction());
        }
    }
}