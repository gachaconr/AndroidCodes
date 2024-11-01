/**
 *  @version 1.1 (28.01.2013)
 *  http://english.cxem.net/arduino/arduino5.php
 *  @author Koltykov A.V. (�������� �.�.)
 * 
 */

package com.example.bluetooth1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
 
@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {
  private static final String TAG = "bluetooth1";
   
  Button btnConn, btnAlarm, btntxThreshold;
  TextView txtArduino, thresholdSet;
  Handler h;
  private Spinner spinner1; 
  
  final int RECIEVE_MESSAGE = 1;	
  private StringBuilder sb = new StringBuilder();
  
  private BluetoothAdapter btAdapter = null;
  private BluetoothSocket btSocket = null;
  private DataOutputStream outStream = null;
  BluetoothDevice device; 
  private DataInputStream inStream;
  MediaPlayer mediaPlayer;
  
  Thread rThread;
  
  int connected;
  int threset;
  int thresetnew;
  String auxtextF = "Threshold set equal to ";
  String thresText;

  private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
 
  private static String address = "00:06:66:66:34:0F"; //00:15:FF:F2:19:5F";

  @SuppressLint("NewApi") @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
     
    setContentView(R.layout.activity_main);
    
    h = new Handler() {
    	public void handleMessage(android.os.Message msg) {
    		switch (msg.what) {
            case RECIEVE_MESSAGE:													// if receive massage
            	byte[] readBuf = (byte[]) msg.obj;
            	String strIncom = new String(readBuf, 0, msg.arg1);					// create string from bytes array
            	sb.append(strIncom);												// append string
            	int endOfLineIndex = sb.indexOf("\n");							// determine the end-of-line
            	if (endOfLineIndex > 0) { 											// if end-of-line,
            		String sbprint = sb.substring(0, endOfLineIndex);				// extract string
                    sb.delete(0, sb.length());										// and clear
                	txtArduino.setText(sbprint); 	        // update TextView
                	if (sbprint.equals("1")){
                		if (!mediaPlayer.isPlaying()){
                			mediaPlayer.start();
                		}
                	}
                }
            	try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            	break;
    		}
        };
	};

    connected = 0;
    
    btnConn = (Button) findViewById(R.id.connect);
    btnAlarm = (Button) findViewById(R.id.alarm);
    btntxThreshold = (Button) findViewById(R.id.txdthreshold);
    
    txtArduino = (TextView) findViewById(R.id.txtArduino);
    thresholdSet = (TextView) findViewById(R.id.txtThreshold);
    
    spinner1 = (Spinner) findViewById(R.id.spinner1);
    
    thresholdSet.setText("Distance not set");
    
    btnAlarm.setEnabled(false);
    btntxThreshold.setEnabled(false); // button
    thresholdSet.setEnabled(false);   // textview
    spinner1.setEnabled(false);
    
    mediaPlayer = MediaPlayer.create(this, R.raw.girl);
    mediaPlayer.setLooping(true);
    
    btAdapter = BluetoothAdapter.getDefaultAdapter();
    checkBTState();
    
    String comp = "RNBT-340F";
    Set<BluetoothDevice> btbonded = btAdapter.getBondedDevices();
    for (BluetoothDevice bt : btbonded){
    	if (comp.equals(bt.getName())){
    		device = bt;
    		break;
    	}
    }
   
    btnConn.setOnClickListener(new OnClickListener() {
    	public void onClick(View v) {
    		if(connected == 0){ 
	    		try {
	    		      btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
	    		} catch (IOException e) {
	    			  Log.d(TAG, "...Socket not created...");
	    		}       
	    	
	    		btAdapter.cancelDiscovery();
	       
	    		Log.d(TAG, "...Connecting...");
	    		try {
	    			btSocket.connect();
	    			Log.d(TAG, "...Connection ok...");
	    			connected = 1;
	    		} catch (IOException e) {
	    			try {
	    				btSocket.close();
	    			} catch (IOException e2) {
	    				Log.d(TAG, "...Connection nok...");
	    			}
	    		}
	    	    
	    		try {
	    	      outStream = new DataOutputStream(btSocket.getOutputStream());
	    	    } catch (IOException e) {
	    	    	Log.d(TAG, "outStream not created");
	    	    }
	    		
		        try {
		          inStream = new DataInputStream(btSocket.getInputStream());
		        } catch (IOException e) { 
		        	Log.d(TAG, "inStream not created");
		        }
		        
	    		//if (btSocket.isConnected()){
	    		if (connected == 1){ 
	    			//connected = 1;
	    			btnAlarm.setEnabled(true);
	    			btntxThreshold.setEnabled(true);
	    		    thresholdSet.setEnabled(true);
	    		    spinner1.setEnabled(true);
	    		    txtArduino.setText("Connected");
	    		    thresholdSet.setText("Distance not set");
	    			btnConn.setText("Disconnect");
		            rThread = new Thread(new RcvThread());
		    		rThread.start();
	    		}
	    		
    		} else {
	    		Log.d(TAG, "...Disconnecting...");
	    		try {
        			try {
          			  outStream.write(0x58);
          			  String aux3 = auxtextF + Integer.toString(thresetnew);
          			  thresholdSet.setText(aux3);            			  
          			  threset = thresetnew;
            		} catch (IOException e) {
            		      String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            		      if (address.equals("00:00:00:00:00:00")) 
            		        msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            		      	msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";
            		      	Log.d(TAG, "Fatal error");
            		}
	    			mediaPlayer.pause();
	    			thresholdSet.setText("Distance not set");
	    			btnAlarm.setEnabled(false);
	    			btntxThreshold.setEnabled(false);
	    		    thresholdSet.setEnabled(false);
	    		    spinner1.setEnabled(false);
	    			btSocket.close();
	    			Log.d(TAG, "...Disconnection ok...");
	    		} catch (IOException e) {
	    			try {
		    			mediaPlayer.pause();
		    			btnAlarm.setEnabled(false);
		    			btntxThreshold.setEnabled(false);
		    		    thresholdSet.setEnabled(false);
	    				btSocket.close();
	    			} catch (IOException e2) {
	    				Log.d(TAG, "Fatal error: In onResume() and unable to close socket during connection failure");
	    			}
	    		}    			
	    		connected = 0;
	    		btnConn.setText("Connect");
	    		txtArduino.setText("Disconnected");
    		}
    	}
    });
    
    btnAlarm.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
      	if (mediaPlayer.isPlaying()) { 
      		mediaPlayer.pause();
      	}
      }
    });
    
    btntxThreshold.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
        	if (btSocket.isConnected()){
		        	String auxtext = "AG600";
		        	String x = String.valueOf(spinner1.getSelectedItem());
		        	String x2 = x.substring(0,2);
		        	String aux3 = auxtextF + x;
		        	thresholdSet.setText(aux3);
		        	int x2s = Integer.parseInt(x2);
		        	switch(x2s){
		        		case 16: auxtext = "AG300";
		        			     break;
		        		case 32: auxtext = "AG150";
		        				 break;
		        		case 48: auxtext = "AG100";
						  	     break;
		        	}
		        	
	    			try {
	      			  outStream.writeBytes(auxtext); 
	      			  threset = thresetnew;
	        		} catch (IOException e) {
	        		      String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
	        		      if (address.equals("00:00:00:00:00:00")) 
	        		        msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
	        		      	msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";
	        		      	Log.d(TAG, "Fatal error XYZ");
	        		}
        	}
        }
    });

  }
   
  private void checkBTState() {
    if(btAdapter==null) { 
      //errorExit("Fatal Error", "Bluetooth not support");
      Log.d(TAG, "Bluetooth not support");
    } else {
      if (btAdapter.isEnabled()) {
        Log.d(TAG, "...Bluetooth ON...");
      } else {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 1);
      }
    }
  }
 
      @SuppressLint({ "HandlerLeak", "NewApi" })
	  private class RcvThread implements Runnable { //extends Thread {
		    public void run() {
		        	while (btSocket.isConnected()) {
		        		try {
		        			byte[] buffer = new byte[256];  // buffer store for the stream
		        			int bytes; // bytes returned from read()
			                bytes = inStream.read(buffer);		// Get number of bytes and message in "buffer"
		                    if (bytes > 0) {
		                    	h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();		// Send to message queue Handler
		                    }
				        } catch (IOException e) { 
				        	break;
				        }
		        	}
		    }
		}	

}

