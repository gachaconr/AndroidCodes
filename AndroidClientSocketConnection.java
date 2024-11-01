package com.example.rpisocketconnection;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity{
	
	private Socket socket = null;
	private String serverIpAddress = "173.91.49.243";
  private String serverPort = "55000";
  private Button buttonConn, buttonServer;
  boolean connected = false;
	Thread cThread; 
	int stopserver;

	@SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        buttonConn = (Button) findViewById(R.id.connect);
        buttonServer = (Button) findViewById(R.id.servercmd);
        buttonServer.setEnabled(false);
        stopserver = 0;
        
        buttonConn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (connected == false){
            	        cThread = new Thread(new ClientThread());
            			cThread.start();
    					connected = true;
    					buttonServer.setEnabled(true);
                		buttonConn.setText("Disconnect");
            	} else {
    					connected = false;
    					buttonServer.setEnabled(false);
                		buttonConn.setText("Connect");            	
    			}
            }
        });
        
        buttonServer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (connected && socket.isConnected()){
            		connected = false;
            		stopserver = 1;
            		buttonConn.setEnabled(false);
					      buttonServer.setEnabled(false);
            	}
            }
        });
    }
	
	public class ClientThread implements Runnable {
		public void run() {
				try {
					socket = new Socket(serverIpAddress, Integer.parseInt(serverPort));
					while (connected && socket.isConnected()) { };
					PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
					if (stopserver == 0){
						out.println("close");//("0");
					} else {
						out.println("exit");
					}
					socket.close();
				} catch (Exception e) {
					Log.e("RpiSocketConnection", "couldn't disconnect", e);
				}				
		}
	}

}
