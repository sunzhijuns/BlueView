package com.szj.blueview;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.Set;

/**
 * 设备列表Activity
 * Created by sunzhijun on 2017/12/31.
 */

public class MyDeviceListActivity extends AppCompatActivity {
    //extra信息名称
    public static String EXTRA_DEVICE_ADDR = "device_address";
    private BluetoothAdapter myBtAdapter;
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

        //注册发现设备时的广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver,filter);

        //注册搜索完成时的广播
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver,filter);

        //获取本地蓝牙适配器
        myBtAdapter = BluetoothAdapter.getDefaultAdapter();
        //获取已配对的设备
        Set<BluetoothDevice> pairedDevices = myBtAdapter.getBondedDevices();
        //将所有已配对设备信息放入列表中
        if (pairedDevices.size()>0){
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device: pairedDevices) {
                myAdapterPaired.add(device.getName() + "\n"+device.getAddress());
            }
        }else{
            String noDevices = getResources().getText(R.string.none_paired).toString();
            myAdapterPaired.add(noDevices);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myBtAdapter != null){//确保不在搜索设备
            myBtAdapter.cancelDiscovery();
        }
        //取消广播监听器
        this.unregisterReceiver(mReceiver);
    }

    //使用蓝牙适配器搜索设备
    private void doDiscovery() {
        //在标题上显示正在搜索的标志
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);
        //显示搜索新设备的副标题
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        if (myBtAdapter.isDiscovering()){//如果正在搜索，取消本次搜索
            myBtAdapter.cancelDiscovery();
        }
        myBtAdapter.startDiscovery();//开始搜索
    }

    //监听搜索到设备的broadcastreceiver
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //如果找到设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                //从Intent中获取BluetoothDevice对象
                BluetoothDevice device =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //如果没有配对，将设备就加入新设备列表
                if (device.getBondState() != BluetoothDevice.BOND_BONDED){
                    myAdapterNew.add(device.getName() + "\n" + device.getAddress());
                }
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                //当搜索完成后，改变activity的标题
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (myAdapterNew.getCount() == 0){
                    String noDevices = getResources().getText(R.string.none_found)
                            .toString();
                    myAdapterNew.add(noDevices);
                }
            }
        }
    };
}
