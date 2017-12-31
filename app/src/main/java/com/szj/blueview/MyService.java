package com.szj.blueview;

/**
 * 用于管理连接的Service
 * Created by sunzhijun on 2017/12/31.
 */

public class MyService {
    private int state;
    //表示当前连接状态
    public static final int STATE_NONE = 0;//什么也没做
    public static final int STATE_LISTEN = 1;//正在监听连接
    public static final int STATE_CONNECTING = 2;//正在连接
    public static final int STATE_CONNECTED = 3;//已连接到设备
    


    public int getState() {
        return state;
    }

    public void write(byte[] send) {
    }

    public void stop() {
    }

    public void start() {
    }
}
