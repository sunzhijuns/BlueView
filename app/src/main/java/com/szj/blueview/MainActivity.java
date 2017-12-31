package com.szj.blueview;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
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

    private MyService myService = null;//Service引用

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
            if (myService == null){
                Log.i("初始化蓝牙","3");
                initChat();
            }
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
        myService = new MyService(this,mHandler);//创建Service对象
    }
    //发送消息
    private void sendMessage(String message) {
        //先检查是否已经连接到设备
        if (myService.getState() != MyService.STATE_CONNECTED){
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        if (message.length() > 0){//如果消息不为空，再发送消息
            byte[] send = message.getBytes();
            myService.write(send);
            //消除StringBuffer和编辑文本框的内容
            sbOut.setLength(0);
            etOut.setText(sbOut);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myService != null){//创建并开启service
            //如果Service为空状态
            if (myService.getState() == MyService.STATE_NONE){
                myService.start();//开启service
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myService != null){//停止Service
            myService.stop();
        }
    }

    //处理从Service发来的消息
    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Constant.MSG_READ:
                    byte[] bufRead = (byte[])msg.obj;
                    //创建显示的字符串
                    String readMessage = new String(bufRead,0,msg.arg1);
                    Log.i("输入的信息",readMessage);
                    Toast.makeText(MainActivity.this,
                            connectedName + ":"+readMessage, Toast.LENGTH_LONG).show();
                    break;
                case Constant.MSG_DEVICE_NAME:
                    //获取已连接的设备的名称，并弹出提示信息
                    connectedName = msg.getData().getString(Constant.DEVICE_NAME);
                    Toast.makeText(MainActivity.this,
                            "已连接到 " + connectedName, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                //如果设备列表activity成功返回一个连接的设备
                if (resultCode == Activity.RESULT_OK){
                    //获取设备的MAC地址
                    String address = data.getExtras()
                            .getString(MyDeviceListActivity.EXTRA_DEVICE_ADDR);
                    //获取BluetoothDevice对象
                    BluetoothDevice device = btAdapter.getRemoteDevice(address);
                    myService.connect(device);//连接该设备
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
        return false;
    }
}
