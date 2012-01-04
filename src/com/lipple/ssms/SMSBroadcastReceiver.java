package com.lipple.ssms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.lipple.ssms.SSMSService.ServiceBinder;

public class SMSBroadcastReceiver extends BroadcastReceiver {
	
	
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "SMSBroadcastReceiver";
    
    
	@Override
	public void onReceive(Context context, Intent intent) {
		
        //Log.i(TAG, "Intent recieved: " + intent.getAction());

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[])bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                }
                if (messages.length > -1) {
                    //Log.i(TAG, "Message recieved: " + messages[0].getMessageBody());
                }
                String message = messages[0].getMessageBody();
                
                if(message.startsWith("<SSMS> ")) //this is a message sent by our app
                {
                	abortBroadcast();
                	context.startService(new Intent(context, SSMSService.class));
            		SSMSService service = SSMSService.getInstance();
            		
                	message = message.substring(7);
                	//Log.i(TAG, "Message: " + message);
                	if(message.startsWith("MSG"))
                	{            		
                		message = message.substring(3);
                		//Log.i(TAG, "Message: " + message);
                		service.receiveMessage(context, messages[0].getOriginatingAddress(),message );
                	}else if(message.startsWith("AREQ")) //they sent us their key!
                	{
                		message = message.substring(4);
                		//Log.i(TAG, "Message: " + message);
                		service.processAckReq(context, messages[0].getOriginatingAddress(), message);
                	}else if(message.startsWith("REQ"))//they are requesting our key
                	{
                		message = message.substring(3);
                		//Log.i(TAG, "Message: " + message);
                		service.processRequestMessage(context, messages[0].getOriginatingAddress(), message);
                	}else if(message.startsWith("ACK"))
                	{
                		message = message.substring(3);
                		//Log.i(TAG, "coded message: " + message);
                		service.processAcknowledge(context, messages[0].getOriginatingAddress(), message);
                	}
                		
                }
            }
	}



}
