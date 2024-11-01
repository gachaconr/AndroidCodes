package com.example.environmentsensing;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

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
import android.media.MediaPlayer;

@SuppressLint("HandlerLeak")
public class DisplayNewActivity extends Activity {

    private boolean connected = false;
    private String serverIpAddress = "";
    private String serverPort = "";
    private Button buttonDISCONN;
	private TextView eCO2, TVOC, Temp, Press, Hum;
    private Socket socket = null;
    private Intent intentback;
    MediaPlayer mediaPlayer;

	Thread cThread, rThread;
	private Handler dataHandler, paramsHandler;
	int firstbatch;
	String txdata;
	byte[] paramServer = new byte[50];
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    setContentView(R.layout.mainnewact);

        dataHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	byte[] buffer = (byte[])msg.obj;
            	//byte aux1 = (byte)(buffer[0]);
            	//String bufferX = (String)msg.obj;;
            	int co2 = ((buffer[0] << 8) & 0xFF00) | (buffer[1] & 0x00FF);
            	//
            	int tvoc = ((buffer[2] << 8) & 0xFF00) | (buffer[3] & 0x00FF); 
            	//
            	int rawtemp = ((((buffer[8] << 16) & 0xFF0000) | ((buffer[9] << 8) & 0x00FF00) | (buffer[10] & 0x0000FF)) >> 4) & 0x0FFFFF;
            	float ut = (float)rawtemp;
            	int T1f = ((paramServer[1] << 8) & 0xFF00) | (paramServer[0] & 0x00FF);
            	int T2f = ((paramServer[3] << 8) & 0xFF00) | (paramServer[2] & 0x00FF);
            	int T3f = ((paramServer[5] << 8) & 0xFF00) | (paramServer[4] & 0x00FF);
            	float var1 = ((ut/((float)16384)) - ((float)T1f)/(((float)1024)))*((float)T2f);
            	float var2 = ( ( (ut/((float)131072)) - (((float)T1f)/((float)8192)) )*((ut/((float)131072)) - (((float)T1f)/((float)8192))))*((float)T3f);
            	float tempf = (var1 + var2)/((float)5120);
            	//
            	int P1f = ((paramServer[7] << 8) & 0xFF00) | (paramServer[6] & 0x00FF);
            	int P2f = ((paramServer[9] << 8) & 0xFF00) | (paramServer[8] & 0x00FF);
            	int P3f = ((paramServer[11] << 8) & 0xFF00) | (paramServer[10] & 0x00FF);
            	int P4f = ((paramServer[13] << 8) & 0xFF00) | (paramServer[12] & 0x00FF);
            	int P5f = ((paramServer[15] << 8) & 0xFF00) | (paramServer[14] & 0x00FF);
            	int P6f = ((paramServer[17] << 8) & 0xFF00) | (paramServer[16] & 0x00FF);
            	int P7f = ((paramServer[19] << 8) & 0xFF00) | (paramServer[18] & 0x00FF);
            	int P8f = ((paramServer[21] << 8) & 0xFF00) | (paramServer[20] & 0x00FF);
            	int P9f = ((paramServer[23] << 8) & 0xFF00) | (paramServer[22] & 0x00FF);
            	int rawpress = ((((buffer[5] << 16) & 0xFF0000) | ((buffer[6] << 8) & 0x00FF00) | (buffer[7] & 0x0000FF) ) >> 4) & 0x0FFFFF;
            	float utp = (float)rawpress;
            	float var1p = ((float)-64000.0);
            	float var2p = var1p*var1p*((float)P6f)/((float)32768.0);
            	var2p = var2p + var1p*((float)P5f)*((float)2.0);
            	var2p = (var2p/((float)4.0)) + ((float)P4f)*((float)65536.0);
            	var1p = ( ((float)P3f)*var1p*var1p/((float)524288.0) + ((float)P2f)*var1p)/((float)524288.0);
            	var1p = (((float)1.0) + var1p/((float)32768.0))*((float)P1f);
            	float pressf;
            	if (var1p == ((float)0)){
            		pressf = ((float)0.0);
            	} else {
            		utp = ((float)1048576.0) - utp;
            		utp = ((utp - var2p/((float)4096.0))*((float)6250.0))/var1p;
            		var1p = ((float)P9f)*utp*utp/((float)2147483648.0);
            		var2p = utp*((float)P8f)/((float)32768.0);
            		pressf = utp + (var1p + var2p + ((float)P7f))/((float)16.0);
            	}
            	//
            	int H1f = paramServer[24] & 0x0000FF;
            	int H2f = (((paramServer[26] << 8) & 0xFF00) | (paramServer[25] & 0x00FF) & 0x00FFFF);
            	int H3f = paramServer[27] & 0x0000FF;
            	int H4f = (((paramServer[28] << 4) & 0x0FF0) | (paramServer[29] & 0x000F) & 0x0FFF);
            	int H5f = (((paramServer[31] << 4) & 0x0FF0) | (paramServer[30] & 0x000F) & 0x0FFF);
            	int H6f = paramServer[32] & 0x0000FF;
            	float rawhum = (((buffer[11] << 8) & 0xFF00) | (buffer[12] & 0x00FF) & 0x00FFFF);
            	float uth = (float)(-76800.0);
            	uth = (rawhum-(((float)H4f)*((float)64.0)+(((float)H5f)/((float)16384.0))*uth))*((((float)H2f)/((float)65536.0))*(((float)1.0)+(((float)H6f)/((float)67108864.0))*uth*(((float)1.0)+(((float)H3f)/((float)67108864.0))*uth)));
            	uth = uth*(((float)1.0) - ((float)H1f)*uth/((float)524288.0));
            	float humf;
            	if (uth > 100){
            		humf = ((float)100.0);
            	} else {
            		if (uth < 0){ 
            			humf = ((float)0.0); 
            		} else {
            			humf = uth;
            		}
            	}
            	//
            	String buffer1 = Integer.toString(co2);
            	String buffer2 = Integer.toString(tvoc);
            	String temps = Float.toString(tempf);
            	String press = Float.toString(pressf);
            	String hum = Float.toString(humf);
            	eCO2.setText(buffer1);
            	TVOC.setText(buffer2);
            	Temp.setText(temps);
            	Press.setText(press);
            	Hum.setText(hum);
            }
        };        

        paramsHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	byte[] buffer = (byte[])msg.obj;
            	paramServer = Arrays.copyOf(buffer,33);
            }
        };
        
	    Intent intent = getIntent();
	    serverIpAddress = intent.getStringExtra(MainActivity.EXTRA_IP);
	    serverPort = intent.getStringExtra(MainActivity.EXTRA_PORT);
	    
	    intentback = new Intent(this, MainActivity.class);
	    intentback.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    intentback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    
	    eCO2 = (TextView) findViewById(R.id.eco2);
	    TVOC = (TextView) findViewById(R.id.tvoc);
	    Temp = (TextView) findViewById(R.id.temp);
	    Press = (TextView) findViewById(R.id.press);
	    Hum = (TextView) findViewById(R.id.hum);
	   
	    buttonDISCONN = (Button) findViewById(R.id.newact);
        buttonDISCONN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	connected = false;
            }
        });		

        mediaPlayer = MediaPlayer.create(this, R.raw.girl);
        mediaPlayer.setLooping(true);
        
        cThread = new Thread(new ClientThread());
        rThread = new Thread(new RcvThread());
		cThread.start();
	}
	
	public class ClientThread implements Runnable {
		public void run() {
				try {
					socket = new Socket(serverIpAddress, Integer.parseInt(serverPort));
					connected = true;
					firstbatch = 1;
					rThread.start();
					buttonDISCONN.setEnabled(true);
					while (connected);
					txdata = "1";
					PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
					out.println(txdata);
					buttonDISCONN.setEnabled(false);
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
						    if (firstbatch == 1){
							    int readX = dis.read(buffer, 0, 33);
								if (readX > 0) {
									Message msg = new Message();
									msg.obj = buffer;
									paramsHandler.sendMessage(msg);
									firstbatch = 2;
								}
						    } else {
							    int readX = dis.read(buffer, 0, 13);
								if (readX > 0) {
									Message msg = new Message();
									msg.obj = buffer;
									dataHandler.sendMessage(msg);
								}
						    }
					}
				} catch (Exception e) {
					Log.e("SocketConnectionv02Activity", "C: ErrorRCVD", e);
				}
				
		}
	}

}