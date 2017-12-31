package com.szj.blueview;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

/**
 * 设备列表Activity
 * Created by sunzhijun on 2017/12/31.
 */

public class MyDeviceListActivity extends AppCompatActivity {
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> myAdapterPaired;
    private ArrayAdapter<String> myAdapterNew;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置窗口
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);
        //设置为RESULT_CANCELED时,返回到该activity的调用者
        setResult(Activity.RESULT_CANCELED);
        //初始化搜索按钮
        Button btnScan = findViewById(R.id.button_scan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);//使按钮不可见
            }
        });

        //初始化适配器
        myAdapterPaired = new ArrayAdapter<String>(this,R.layout.device_name);//已配对
        myAdapterNew = new ArrayAdapter<String>(this,R.layout.device_name);//新发现的
        //将已配对的设备放入列表中
        ListView lvPaired = findViewById(R.id.paired_devices);
        lvPaired.setAdapter(myAdapterPaired);

        //将新发现的设备放入列表中
        ListView lvNewDevices = findViewById(R.id.new_devices);
        lvNewDevices.setAdapter(myAdapterNew);

    }

    private void doDiscovery() {
    }
}
