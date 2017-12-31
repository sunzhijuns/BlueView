package com.szj.blueview;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * 用于管理连接的Service
 * Created by sunzhijun on 2017/12/31.
 */

public class MyService {
    //本应用唯一的UUID
    private static final UUID  MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private BluetoothAdapter btAdapter;
    private Handler myHandler;
    private AcceptThread myAcceptThread;
    private ConnectThread myConnectThread;
    private ConnectedThread myConnectedThread;
    private int myState;
    //表示当前连接状态
    public static final int STATE_NONE = 0;//什么也没做
    public static final int STATE_LISTEN = 1;//正在监听连接
    public static final int STATE_CONNECTING = 2;//正在连接
    public static final int STATE_CONNECTED = 3;//已连接到设备

    public MyService(MainActivity mainActivity, Handler mHandler) {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        myState = STATE_NONE;
        myHandler = mHandler;

    }


    public synchronized int getState() {
        return myState;
    }

    private synchronized void setState(int state) {
        this.myState = state;
    }

    public void write(byte[] out) {
        ConnectedThread tmpCt;
        synchronized (this){
            if (myState!= STATE_CONNECTED) return;
            tmpCt = myConnectedThread;
        }
        tmpCt.write(out);//写入数据
    }

    public void stop() {//停止所有线程
        if (myConnectThread != null){
            myConnectThread.cancel();
            myConnectThread = null;
        }
        if (myConnectedThread != null){
            myConnectedThread.cancel();
            myConnectedThread = null;
        }
        if (myAcceptThread != null){
            myAcceptThread.cancel();
            myAcceptThread = null;
        }
        setState(STATE_NONE);
    }
    //开启service
    public synchronized void start() {
        //关闭不必要的线程
        if (myConnectThread != null){
            myConnectThread.cancel();
            myConnectThread = null;
        }
        if (myConnectedThread != null){
            myConnectedThread.cancel();
            myConnectedThread = null;
        }
        if (myAcceptThread == null){
            myAcceptThread = new AcceptThread();
            myAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }
    //连接设备
    public synchronized void connect(BluetoothDevice device) {
        //关闭不必要的线程
        if (myState == STATE_CONNECTING){
            if (myConnectThread != null){
                myConnectThread.cancel();
                myConnectThread = null;
            }
        }
        if (myConnectedThread != null){
            myConnectedThread.cancel();
            myConnectedThread = null;
        }
        //开启线程，连接设备
        myConnectThread = new ConnectThread(device);
        myConnectThread.start();
        setState(STATE_CONNECTING);
    }

    //设备通话的线程
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (myConnectThread != null){
            myConnectThread.cancel();
            myConnectThread = null;
        }
        if (myConnectedThread!=null){
            myConnectedThread.cancel();
            myConnectedThread = null;
        }
        if (myAcceptThread !=null){
            myAcceptThread.cancel();
            myAcceptThread = null;
        }
        //创建并开启ConnectedThread
        myConnectedThread = new ConnectedThread(socket);
        myConnectedThread.start();
        //发送已连接的设备名称到主界面
        Message msg = myHandler.obtainMessage(Constant.MSG_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constant.DEVICE_NAME,device.getName());
        msg.setData(bundle);
        myHandler.sendMessage(msg);
        setState(STATE_CONNECTED);

    }

    private class AcceptThread extends Thread{//监听连接的线程
        //本地服务器端ServerSocket
        private final BluetoothServerSocket mmServerSocket;
        private AcceptThread() {
            BluetoothServerSocket tempSS = null;
            try {//创建用于监听的服务器端ServerSocket
                tempSS = btAdapter.listenUsingRfcommWithServiceRecord("BluetoothChat",MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmServerSocket = tempSS;
        }

        @Override
        public void run() {
            setName("AcceptThread");//设置线程名称
            BluetoothSocket socket = null;
            while(myState != STATE_CONNECTED){//如果没有连接到设备
                try {
                    socket = mmServerSocket.accept();//获取连接的Socket
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                if (socket != null){
                    synchronized (MyService.this){
                        switch (myState){
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                //连接后管理数据交流的线程
                                connected(socket,socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                try {
                                    socket.close();//关闭新Socket
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }
            }
        }
        public void cancel(){//关闭本地服务器端ServerSocket
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private class ConnectThread extends Thread{//尝试连接其他设备
        private final BluetoothSocket myBtSocket;
        private final BluetoothDevice mmDevice;

        private ConnectThread(BluetoothDevice device) {
            mmDevice = device;

            BluetoothSocket tmp = null;
            //通过正在连接的设备获取BluetoothSocket
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            myBtSocket = tmp;
        }

        @Override
        public void run() {
            setName("ConnectThread");
            btAdapter.cancelDiscovery();//取消搜索设备
            try {//尝试连接到BluetoothSocket
                myBtSocket.connect();
            } catch (IOException e) {
                setState(STATE_LISTEN);//连接断开后，设置状态为正在监听
                try {
                    myBtSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                MyService.this.start();//如果连接不成功，重新开启service
                return;
            }
            synchronized (MyService.this){
                // 将connectthread线程置为空
                myConnectThread = null;

            }
            connected(myBtSocket,mmDevice);//数据交流的线程
        }
        public void cancel(){
            try {
                myBtSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectedThread extends Thread{
        private final BluetoothSocket myBtSocket;
        private final InputStream mmInputStream;
        private final OutputStream myOs;

        private ConnectedThread(BluetoothSocket socket) {
            myBtSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            //获取BluetoothSocket的输入输出流
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInputStream = tmpIn;
            myOs = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while(true){//一直监听输入流
                try {
                    bytes = mmInputStream.read(buffer);//从输入流中读入数据
                    Log.i("bytes",bytes + "--------------------");
                    //将读入的数据发送到主Activity
                    myHandler.obtainMessage(Constant.MSG_READ,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    setState(STATE_LISTEN);
                    break;
                }

            }
        }
        //向输入流中写入数据
        public void write(byte[] buffer){
            try {
                myOs.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void cancel(){
            try {
                myBtSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
