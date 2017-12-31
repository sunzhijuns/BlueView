package com.szj.blueview;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by sunzhijun on 2018/1/1.
 */

public class ClientActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        Button button = findViewById(R.id.button_client);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    @Override
                    public void run() {
                        connectServer();
                    }
                }.start();
            }
        });
    }

    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    String str = msg.getData().getString("data");
                    TextView tv = findViewById(R.id.tv_client);
                    tv.setText(str);
                    break;
            }
        }
    };
    private void connectServer() {
        String serverIp = "192.168.1.2";
        try {
            Socket socket = new Socket(serverIp,8877);
            DataInputStream din = new DataInputStream(socket.getInputStream());
            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
            EditText et = findViewById(R.id.et_client);
            String tempStr = et.getText().toString();
            dout.writeUTF(tempStr);

            String retStr = din.readUTF();

            Log.i("返回的数据",retStr);
            Message msg = handler.obtainMessage(1);
            Bundle bundle = new Bundle();
            bundle.putString("data",retStr);
            msg.setData(bundle);
            handler.sendMessage(msg);

            din.close();
            dout.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
