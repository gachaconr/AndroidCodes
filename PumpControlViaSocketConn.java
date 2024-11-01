package com.example.watersystem;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {

    private boolean connected = false;
    private Button buttonT;
    private Socket socket = null;
    private String ip;
    private String port;    
	Thread cThread, rThread;
	int txd, state, sendtx;
	String txdata;
	String fmsg, buttontext;
	private Handler dataHandler;
	
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
            	connected = false;
                cThread = new Thread(new ClientThread());
                rThread = new Thread(new RcvThread());
        		cThread.start();
            }
        });        
    }
    
	public class ClientThread implements Runnable {
		public void run() {
				try {
					state = 0;
					while (state < 2){
						socket = new Socket(ip, Integer.parseInt(port));
						connected = true;
						rThread.start();
						sendtx = 0;
						while (connected) {
							if ((state == 0) && (sendtx == 0)){ // ask state of pump
								sendtx = 1;
								txdata = "0";
								PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
								out.println(txdata);
							} 
							if ((state == 1) && (sendtx == 0)){ // turn on/off pump
								sendtx = 1;
								PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
								out.println(txdata);
							}
						}
						socket.close();
					}
				} catch (Exception e) {
					Log.e("WaterSystem", "C: No connected", e);
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
							fmsg = Byte.toString(buffer[0]);
							if (state == 0){
								//value determine the command to send to pump
								if (fmsg.equals("1")){
									buttontext = "Opened";
									txdata = "2";
								} else {
									if (fmsg.equals("2")){
										buttontext = "Closed";
										txdata = "1";
									}
								}
								Message msg = new Message();
								msg.obj = buttontext;
								dataHandler.sendMessage(msg);
								state = 1;
							} else {
								if (state == 1) {
									state = 2;
									if (fmsg.equals(txdata)){
										Log.d("WaterSystem", "everything ok");
									}
								}
							}
							connected = false;
						}

					}
				} catch (Exception e) {
					Log.e("WaterSystem", "C: ErrorRCVD", e);
				}
		}
	}    
    
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/
}
