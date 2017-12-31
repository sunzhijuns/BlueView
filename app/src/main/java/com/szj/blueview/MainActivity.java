package com.szj.blueview;

import android.bluetooth.BluetoothAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

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
        }else{
            Log.i("初始化蓝牙","2");

        }
    }
}
