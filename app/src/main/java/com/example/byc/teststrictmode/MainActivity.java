package com.example.byc.teststrictmode;

import android.content.pm.ApplicationInfo;
import android.os.Environment;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * 参考：http://blog.csdn.net/mynameishuangshuai/article/details/51742375
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_network;
    private Button btn_diskreadwrite;
    private Button btn_noteslowcall;
    private Button btn_activityleak;
    private Button btn_leakedclosableobject;
    private Button btn_classinstancelimit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (isDebug()) {
            //可以在开发者选项中设置“启用严格模式”
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().penaltyDialog().build());
            // StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectCustomSlowCalls().detectDiskReads().detectDiskWrites().detectNetwork().detectResourceMismatches().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().penaltyDeath().build());
        }
        initViews();
    }

    private boolean isDebug() {
        try {
            ApplicationInfo appInfo = getApplicationInfo();
            return (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
        }
        return false;
    }

    private void initViews() {
        btn_network = (Button) findViewById(R.id.btn_network);
        btn_diskreadwrite = (Button) findViewById(R.id.btn_diskreadwrite);
        btn_noteslowcall = (Button) findViewById(R.id.btn_noteslowcall);

        btn_activityleak = (Button) findViewById(R.id.btn_activityleak);
        btn_leakedclosableobject = (Button) findViewById(R.id.btn_leakedclosableobject);
        btn_classinstancelimit = (Button) findViewById(R.id.btn_classinstancelimit);

        btn_network.setOnClickListener(this);
        btn_diskreadwrite.setOnClickListener(this);
        btn_noteslowcall.setOnClickListener(this);
        btn_activityleak.setOnClickListener(this);
        btn_leakedclosableobject.setOnClickListener(this);
        btn_classinstancelimit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_network:
                postNetwork();
                break;
            case R.id.btn_diskreadwrite:
                writeToExternalStorage();
                break;
            case R.id.btn_noteslowcall:
                runTaskExecutor();
                break;
            case R.id.btn_activityleak:
                activityLeak();
                break;
            case R.id.btn_leakedclosableobject:
                leakedClosableObject();
                break;
            case R.id.btn_classinstancelimit:
                classInstanceLimit();
                break;
            default:
                break;
        }
    }

    private void postNetwork() {
        try {
            URL url = new URL("http://www.iqiyi.com/index.html");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String lines = null;
            StringBuffer sb = new StringBuffer();
            while ((lines = reader.readLine()) != null) {
                sb.append(lines);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeToExternalStorage() {
        File file = Environment.getExternalStorageDirectory();
        File mbFile = new File(file, "castiel.txt");
        try {
            OutputStream output = new FileOutputStream(mbFile, true);
            output.write("测试文件".getBytes());
            output.flush();
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runTaskExecutor() {
        TaskExecutor executor = new TaskExecutor();
        executor.executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //反复旋转屏幕会输出提示信息（重点是instances=2;limit=1这一行）
    // 我们在Activity中创建了一个Thread匿名内部类，而匿名内部类隐式持有外部类的引用。
    // 而每次旋转屏幕是，Android会新创建一个Activity，
    // 而原来的Activity实例又被我们启动的匿名内部类线程持有，所以不会释放，
    // 从日志上看，当先系统中该Activty有4个实例，而限制是只能创建1各实例。
    // 我们不断翻转屏幕，instances 的个数还会持续增加。
    private void activityLeak() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    SystemClock.sleep(1000);
                }
            }
        }.start();
    }

    //用于资源没有正确关闭时提醒
    //类似还有 detectLeakedSqlLiteObjects 用来检查SQLiteCursor或者其他SQLite对象是否被正确关闭
    //detectLeakedRegistrationObjects()用来检查BroadcastReceiver或者ServiceConnection注册类对象是否被正确释放
    private void leakedClosableObject() {
        File newxmlfile = new File(Environment.getExternalStorageDirectory(), "textStrickmode");
        try {
            newxmlfile.createNewFile();
            FileWriter fw = new FileWriter(newxmlfile);
            fw.write("测试，Hello");
            //fw.close();//特意不关闭fw,测试资源未关闭提醒
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class CastielClass{}
    private static List<CastielClass> classList;

    //设置某个类的同时处于内存中的实例上限，可以协助检查内存泄露
    private void classInstanceLimit() {
        classList.add(new CastielClass());
        classList.add(new CastielClass());
        classList.add(new CastielClass());
        classList.add(new CastielClass());
        classList.add(new CastielClass());
        classList.add(new CastielClass());
        classList.add(new CastielClass());
    }


}
