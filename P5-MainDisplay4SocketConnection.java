package com.example.camerawifiv01;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    private EditText edittextIP, edittextPORT;
    private TextView ipT, portT;
    
	public final static String EXTRA_IP = "com.example.connectionv01.IP";
	public final static String EXTRA_PORT = "com.example.connectionv01.PORT";
    private String ip;
    private String port;    
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        edittextIP = (EditText) findViewById(R.id.ipserverx2);
        edittextPORT = (EditText) findViewById(R.id.portserverx2);
        ipT = (TextView) findViewById(R.id.ipx2);
        portT = (TextView) findViewById(R.id.portx2);
        
        edittextIP.setTextColor(Color.BLACK);
        edittextPORT.setTextColor(Color.BLACK);
        ipT.setTextColor(Color.BLACK);
        portT.setTextColor(Color.BLACK);        
    }

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
    }
    
    public void sendMessage(View view) {
        // Do something in response to button
        ip = edittextIP.getText().toString();
        port = edittextPORT.getText().toString();
        Intent intent = new Intent(this, DisplayNewActivity.class);
        intent.putExtra(EXTRA_IP, ip);
        intent.putExtra(EXTRA_PORT, port);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);        
        startActivity(intent);
    }    
    
}
