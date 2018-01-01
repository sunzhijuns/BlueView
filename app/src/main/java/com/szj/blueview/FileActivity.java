package com.szj.blueview;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by sunzhijun on 2018/1/1.
 */

public class FileActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {        //重写onCreate方法
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);                        //设置layout
        Button ok = this.findViewById(R.id.bt01_file);    //获取打开按钮引用
        ok.setOnClickListener                                //为打开按钮添加监听器
                (new View.OnClickListener() {
                    public void onClick(View v) {
                        EditText et1 = findViewById(R.id.et_01_file);
                        //调用loadText方法获取对应文件名的文件
                        String nr = loadText(et1.getText().toString().trim());
                        EditText et2 = findViewById(R.id.et_02_file);
                        //设置显示框内容
                        et2.setText(nr);
                    }
                });
    }

    public String loadText(String name)                        //加载SD卡文件方法
    {
        String nr = null;                                        //内容字符串
        try {




            File root = Environment.getExternalStorageDirectory().getAbsoluteFile();
            for (File file :
                    root.listFiles()) {
                Log.i("root文件",file.getAbsolutePath());
            }

            File f = new File(root ,name);                //创建对应文件
            Log.i("file文件",f.getAbsolutePath());

            byte[] buff = new byte[(int) f.length()];            //创建响应大小的byte数组
            FileInputStream fis = new FileInputStream(f);
            fis.read(buff);                                    //读入文件
            fis.close();                                    //关闭输入流
            nr = new String(buff, "utf-8");                    //转码生成字符串
            nr = nr.replaceAll("\\r\\n", "\n");
        }                //替换换行符

        catch (Exception e) {    //没有找到文件提示
            Toast.makeText(getBaseContext(), "对不起，没有找到指定文件。", Toast.LENGTH_LONG).show();
        }
        return nr;                                            //返回内容字符串
    }
}
