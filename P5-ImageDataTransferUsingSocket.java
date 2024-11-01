package com.example.camerawifiv01;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
public class DisplayNewActivity extends Activity {

    private boolean connected = false;
    private String serverIpAddress = "";
    private String serverPort = "";
    private Button buttonDISCONN, buttonReqIm;
	private TextView commentS;
    private Socket socket = null;
    private Intent intentback;    
    private ImageView imageSock;
	Thread cThread, rThread;
    private Handler mHandler, mHandlerText;
	int txd;
	String txdata;		
	int getImSz, numberBytes, k, ReqIm;
	byte[] imBytes2Send, bufferImg;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mainnewact);
		
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	Bitmap d2 = (Bitmap)msg.obj;
            	Matrix mt = new Matrix();
            	mt.setRectToRect(new RectF(0, 0, d2.getWidth(), d2.getHeight()), new RectF(0, 0, 6*d2.getWidth(), 6*d2.getHeight()), Matrix.ScaleToFit.CENTER); 
            	Bitmap d3 = Bitmap.createBitmap(d2, 0, 0, d2.getWidth(), d2.getHeight(), mt,  true);
            	imageSock.setImageBitmap(d3);
            	commentS.setText("showing image");
            }
        };
        
        mHandlerText = new Handler() {
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
	    imageSock = (ImageView) findViewById(R.id.socketImage);
	    buttonDISCONN = (Button) findViewById(R.id.disconnectNEW);
	    buttonReqIm = (Button) findViewById(R.id.reqim);
	    
        buttonDISCONN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	connected = false;
            }
        });	
        buttonReqIm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (ReqIm == 0){
            		buttonReqIm.setText("Stop");
            		txdata = "1";
            		txd = 1;
            		ReqIm = 1;
            	} else {
            		if (ReqIm == 1){
            			ReqIm = 0;
                		buttonReqIm.setText("Start");
                		txd = 0;            			
            		}
            	}
            }
        });	        
        
        getImSz = 0; ReqIm = 0;
        
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
					mHandlerText.sendMessage(msg);
					rThread.start();
					while (connected && socket.isConnected()) {
						if (txd == 1){
							PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
							out.println(txdata);
							txd = 0;
							getImSz = 1;
							//Thread.sleep(1000);
						}
					}
					PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
					out.println("3");					
					socket.close();
					startActivity(intentback);
				} catch (Exception e) {
					startActivity(intentback);
				}				
		}
	}
	
	@SuppressLint("HandlerLeak")
	public class RcvThread implements Runnable {
		@SuppressWarnings("deprecation")
		public void run() {
				while (connected && socket.isConnected()) {
						try {
								if (getImSz == 1){
									char[] buffer = new char[4];
									BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
									int readX = in.read(buffer, 0, 4);
									if (readX > 0) {
										numberBytes = (1000*(((int)buffer[0])-48)) + (100*(((int)buffer[1])-48)) + (10*(((int)buffer[2])-48)) + (((int)buffer[3])-48);
										PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
										out.println("2");
										bufferImg = new byte[2*numberBytes];																		
										getImSz = 2; k = 0;
									}
								} else {
									if (getImSz == 2){
											DataInputStream dis;
											dis = new DataInputStream(socket.getInputStream());
											int readX = dis.read(bufferImg,0,numberBytes);
											if (readX > 0){
												if (k == 0){
													imBytes2Send = new byte[readX];
													System.arraycopy(bufferImg, 0, imBytes2Send, 0, readX);
												} else {
													byte[] imByte2SendAux = imBytes2Send; 
													imBytes2Send = new byte[imByte2SendAux.length + readX];
													System.arraycopy(imByte2SendAux, 0, imBytes2Send, 0, imByte2SendAux.length);
													System.arraycopy(bufferImg, 0, imBytes2Send, imByte2SendAux.length, readX);
												}
												k = k + readX;
												String numberBytesString = Integer.toString(k);
												Message msg2 = new Message();
												msg2.obj = numberBytesString;
												mHandlerText.sendMessage(msg2);												
											} 
											if (k >= numberBytes){
												k = 0;
												getImSz = 0;
										    	Bitmap img2plotX = BitmapFactory.decodeByteArray(imBytes2Send, 0, imBytes2Send.length);
												//Drawable dd = getResources().getDrawable(R.drawable.yellowdot);
										    	Message msg1 = new Message();
										    	msg1.obj = img2plotX;
										    	mHandler.sendMessage(msg1);
												if (ReqIm == 1){
											    	txd = 1;													
												} else {
													if (ReqIm == 0){
														txd = 0;
													}
												}

											}
									}
								}
						} catch (Exception e) {
							Log.e("SXY", "C: Error", e);
						}
				}
		}
	}
	
}