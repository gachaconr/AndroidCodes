package com.example.cellphonetest;

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
    private String MobileNumber = "phonenumberhere";
    private String sms;    
	int state;
	String mX,buttontext;
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
            	buttonT.setText(buffer.substring(0,19));
            	//buttonT.setText(buffer.substring(24));
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
    	inprocess = true;
    	state = 0;
    	sms = "0";
    	smgr.sendTextMessage(MobileNumber,null,sms,null,null);
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
	    int totalSMS = 0;
	    if (cursor != null) {
	    	totalSMS = cursor.getCount();
    		if (cursor.moveToFirst()) { // must check the result to prevent exception
                String smsMessage = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.BODY));
                String smsnumber = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.ADDRESS));
                buttontext = formattedDate;
                Message msg = new Message();
            	msg.obj = formattedDate+"::-::"+smsMessage;
            	dataHandler.sendMessage(msg);
    		} else {
    			Log.d("WaterSystemSMS", "everything not ok");
    		}
	    }
    } 
}
