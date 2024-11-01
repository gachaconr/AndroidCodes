package com.example.alarmsystemv01;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
public class DisplayNewActivity extends Activity {

    private boolean connected = false;
    private String serverIpAddress = "";
    private String serverPort = "";
    private Button buttonDISCONN;
	private TextView commentS;
    private Socket socket = null;
    private Intent intentback;    
	Thread cThread, rThread;
    private Handler mHandler;
	String nString; 
	private MediaPlayer mp1, mp2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mainnewact);
		
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	String buffer = (String)msg.obj;
            	commentS.setText(buffer);
            }
        };
        
        
	    Intent intent = getIntent();
	    serverIpAddress = intent.getStringExtra(MainActivity.EXTRA_IP);
	    serverPort = intent.getStringExtra(MainActivity.EXTRA_PORT);
	    
	    intentback = new Intent(this, MainActivity.class);   
	    
	    commentS = (TextView) findViewById(R.id.commentStatus);
	    commentS.setTextColor(Color.BLACK);
	    buttonDISCONN = (Button) findViewById(R.id.disconnectNEW);
	    
	    mp1 = MediaPlayer.create(this, R.raw.kalimba);
	    mp2 = MediaPlayer.create(this, R.raw.sleepaway);
	    
        buttonDISCONN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	connected = false;
            }
        });	
        
        cThread = new Thread(new ClientThread());
        rThread = new Thread(new RcvThread());
		cThread.start();
	}
	
	public class ClientThread implements Runnable {
		public void run() {
				try {
					socket = new Socket(serverIpAddress, Integer.parseInt(serverPort));
					connected = true;
					Message msg = new Message();
					msg.obj = "Connected!!";
					mHandler.sendMessage(msg);
					rThread.start();
					while (connected) {  }
					socket.close();
					startActivity(intentback);
				} catch (Exception e) {
					startActivity(intentback);
				}				
		}
	}
	
	@SuppressLint("HandlerLeak")
	public class RcvThread implements Runnable {
		public void run() {
				while (connected) {
						try {
								char[] buffer = new char[4];
								BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
								int readX = in.read(buffer, 0, 1);
								if (readX > 0) {
									if (buffer[0] == 49){
										nString = "Camera Activated!";
										mp1.start();
										Thread.sleep(2000);
										mp1.stop();
										mp1.prepare();
									} else {
										if (buffer[0] == 50) {
											nString = "Sonar Activated!";
											mp2.start();
											Thread.sleep(2000);
											mp2.stop();
											mp2.prepare();
										}
									}
									Message msg2 = new Message();
									msg2.obj = nString;
									mHandler.sendMessage(msg2);										
								}
						} catch (Exception e) {
							Log.e("SocketConnectionv02Activity", "C: ErrorRCVD", e);
						}
				}
		}
	}
	
}
