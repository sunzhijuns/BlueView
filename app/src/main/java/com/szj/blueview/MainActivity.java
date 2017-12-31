package com.szj.blueview;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private EditText etOut;
    private Button btnSend;

    private String connectedName = null; //已连接的设备名称
    private StringBuffer sbOut; //发送的字符信息


    private BluetoothAdapter btAdapter = null;//本地蓝牙适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取本地蓝牙适配器
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.i("初始化蓝牙","1");
    }

    @Override
    protected void onStart() {
        super.onStart();
        //如果蓝牙没有开启，提示开启蓝牙，并退出activity
        if (!btAdapter.isEnabled()){
            Toast.makeText(this, "请先开启蓝牙！", Toast.LENGTH_SHORT).show();
            finish();
        }else{//初始化聊天控件
            Log.i("初始化蓝牙","2");
            initChat();
        }
    }

    private void initChat() {
        etOut = findViewById(R.id.edit_text_out);
        btnSend = findViewById(R.id.button_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = findViewById(R.id.edit_text_out);
                String message = textView.getText().toString();
                sendMessage(message);
            }
        });
        sbOut = new StringBuffer("");
    }
    //发送消息
    private void sendMessage(String message) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                //如果设备列表activity成功返回一个连接的设备
                if (resultCode == Activity.RESULT_OK){

                }
                break;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        return super.onPrepareOptionsMenu(menu);
        //启动设备列表activity搜索设备
        Log.i("点击了Menu","----");
        Intent serverIntent = new Intent(this,MyDeviceListActivity.class);
        startActivityForResult(serverIntent,1);
        return true;
    }
}
