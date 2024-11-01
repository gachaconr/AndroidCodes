package com.example.connectionv01;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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
    private Button buttonDISCONN, bsendP, bsendM;
	private TextView commentS, valCC3200;
    private Socket socket = null;
    private Intent intentback;
	

	Thread cThread, rThread;
	private Handler statusHandler, dataHandler;
	int txd;
	String txdata;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    setContentView(R.layout.mainnewact);
		
        statusHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	String buffer = (String)msg.obj;
            	commentS.setText(buffer);
            }
        };

        dataHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	//byte[] buffer = (byte[])msg.obj;
            	//byte aux1 = (byte)(buffer[0]);
            	String buffer = (String)msg.obj;
            	valCC3200.setText(buffer);
            }
        };        
	    
	    Intent intent = getIntent();
	    serverIpAddress = intent.getStringExtra(MainActivity.EXTRA_IP);
	    serverPort = intent.getStringExtra(MainActivity.EXTRA_PORT);
	    
	    intentback = new Intent(this, MainActivity.class);
	    intentback.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    intentback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    
	    buttonDISCONN = (Button) findViewById(R.id.disconnectNEW);
	    commentS = (TextView) findViewById(R.id.commentStatus);
	    bsendP = (Button) findViewById(R.id.sendP);
	    bsendM = (Button) findViewById(R.id.sendM);
	    valCC3200 = (TextView) findViewById(R.id.valCC3200);
	    
        buttonDISCONN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	connected = false;
            	commentS.setText("disconnected");
            }
        });	
        bsendP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	txdata = "2";
            	txd = 1;
            }
        });	
        bsendM.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	txdata = "1";
            	txd = 1;
            }
        });	
        
        txd = 0;
               
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
					statusHandler.sendMessage(msg);
					rThread.start();
					//buttonDISCONN.setEnabled(true);
					//bsendP.setEnabled(true);
					//bsendM.setEnabled(true);
					while (connected) {
						if (txd == 1){
							PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
							out.println(txdata);
							txd = 0;
						}
					}
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
				try {
					while (connected) {
							//char[] buffer = new char[256];
							//BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
							byte[] buffer = new byte[256];
							InputStream in = socket.getInputStream();
						    DataInputStream dis = new DataInputStream(in);
						    int readX = dis.read(buffer, 0, 1);
							if (readX > 0) {
								String fmsg = Byte.toString(buffer[0]);
								Message msg = new Message();
								msg.obj = fmsg;//buffer[0];
								dataHandler.sendMessage(msg);
							}
					}
				} catch (Exception e) {
					Log.e("SocketConnectionv02Activity", "C: ErrorRCVD", e);
				}
				
		}
	}

}