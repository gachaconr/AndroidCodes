package com.example.watersystemsms;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {

    private boolean inprocess = false;
    private Button buttonT;
    private String MobileNumber = "5045783412"; // MUST CHANGE WITH THE NEW CELL PHONE
    private String sms;    
	int state;
	String mX, mXData,buttontext;
	private Handler dataHandler;
	SmsManager smgr = SmsManager.getDefault();
	Calendar currentTime = Calendar.getInstance();;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        dataHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	String buffer = (String)msg.obj;
            	buttonT.setText(buffer);
            }
        };  
        
        buttonT = (Button) findViewById(R.id.turn);
        buttonT.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	sendMessage();
            }
        });        
    }
    
    public void sendMessage() {
        // Do something in response to button
    	//currentTime = Calendar.getInstance();
        String formattedDate = df.format(currentTime.getTime());
        int lengthD = formattedDate.length();
    	inprocess = true;
    	state = 0;
    	sms = formattedDate + "0";
    	smgr.sendTextMessage(MobileNumber,null,sms,null,null);
    	while(inprocess){
    		Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
    	    int totalSMS = 0;
    	    if (cursor != null) {
    	    	totalSMS = cursor.getCount();
	    		if (cursor.moveToFirst()) { // must check the result to prevent exception
	    			for (int idx = 0; idx < totalSMS; idx++) {
	                    String smsnumber = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.ADDRESS));
	     	            if (smsnumber.equals("NUMBER OF DEVICE")){
	     	            	mX = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.BODY));
	                    	String dataConf = mX.substring(1, lengthD); // CONFIRM THIS PART OF THE SUBSTRING
	     	            	if (dataConf.equals(formattedDate)){
	     	            		mXData = mX.substring(lengthD);
		     	            	break;	
	                    	}
	                    } else {
	                    	cursor.moveToNext(); 
	                    }
	    			}
	    			if (state == 0){ // check the state of the pump
		    			if (mX.equals(formattedDate + "1")){ // it is closed
		    		    	sms = formattedDate + "2"; // open pump
		    		    	smgr.sendTextMessage(MobileNumber,null,sms,null,null);
		    		    	state = 1;
		    			}
		    			if (mX.equals(formattedDate + "2")){ // pump is open
		    			    sms = formattedDate + "1"; // close pump
		    			    smgr.sendTextMessage(MobileNumber,null,sms,null,null);		
			    		    state = 1;
		    			}	    				
	    			} else {
	    				if (state == 1){ // waiting for the echo
	    					if (mX.equals(sms)){
								if (mX.equals(sms)){
									buttontext = "Opened";
								}
								if (mX.equals(sms)){
									buttontext = "Closed";
								}
								Message msg = new Message();
								msg.obj = buttontext;
								dataHandler.sendMessage(msg);
								inprocess = false;
	    					}
	    				}
	    			}
	    		} else {
	    			Log.d("WaterSystemSMS", "everything not ok");
	    		}
    	    }
    	}
    } 
}
