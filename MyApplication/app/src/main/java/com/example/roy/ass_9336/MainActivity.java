package com.example.roy.ass_9336;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity implements SensorEventListener {

    //private Button buttonswitch;
    private Button buttongo;
    private Button buttonrate;
    private Button buttonswitch;
    private Button button2switch;
    private Button button2go;
    private Button button3switch;
    private Button button3go;
    private Button button3loc;
    private TextView text1;
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;
    private SensorManager mSensorManager;
    public static final String sensorStart="sensorStart";
    public static final String sensorStop="sensorStop";
    private GPSReader gpsreader;
    Intent batteryStatus;
    //PowerConnectionReceiver receive;
    private PersistService mySerivece;
    WifiManager mWifiManager;

    String whichSensor;
    String whatRate;
    String whetherRun;
    Boolean task1Run = false;
    Boolean task2Run = false;
    Boolean task2Read = false;

    Thread myThread;
    Float task2Timestamp;
    ArrayList<ArrayList<Object>> task2List;
    DBHelper dbHelper;

    ArrayList<ScanResult> scanResult;
    ArrayList<Integer> levelList;
    ScanResult highResult = null;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.openOptionsMenu();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onDestroy() {
        wakeLock.release();
        task1Run = false;
        task2Run = false;
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //http://www.androiddesignpatterns.com/2014/01/thread-scheduling-in-android.html
        //http://stackoverflow.com/questions/6880919/can-i-stop-android-from-killing-my-app
        //http://m.oschina.net/blog/146709
        //http://developer.android.com/intl/zh-cn/reference/android/app/Service.html#startForeground%28int,%20android.app.Notification%29
        Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"myWake");
        text1 = (TextView) findViewById(R.id.text1);
        buttonswitch = (Button) findViewById(R.id.button1switch);
        buttonswitch.setOnClickListener(click1_switch);
        buttongo = (Button) findViewById(R.id.button1go);
        buttongo.setOnClickListener(click1_go);
        buttonrate = (Button) findViewById(R.id.buttonrate);
        buttonrate.setOnClickListener(click1_rate);
        button2switch = (Button) findViewById(R.id.button2switch);
        button2switch.setOnClickListener(click2_switch);
        button2go = (Button) findViewById(R.id.button2go);
        button2go.setOnClickListener(click2_go);
        button3switch = (Button) findViewById(R.id.button3switch);
        button3switch.setOnClickListener(click3_switch);
        button3loc = (Button) findViewById(R.id.button3loc);
        button3loc.setOnClickListener(click3_loc);
        button3go = (Button) findViewById(R.id.button3go);
        button3go.setOnClickListener(click3_go);

        mySerivece = new PersistService();
        //this.startService();
        startService(new Intent(this, PersistService.class));
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        ifilter.addAction(Intent.ACTION_BATTERY_OKAY);
        ifilter.addAction(Intent.ACTION_BATTERY_LOW);
        ifilter.addAction(sensorStart);
        ifilter.addAction(sensorStop);
        batteryStatus = this.registerReceiver(mySerivece.mReceiver, ifilter);

        SharedPreferences setting = getPreferences(Context.MODE_PRIVATE);
        whatRate = setting.getString("task1Rate", "none");

        whichSensor = setting.getString("task1Sensor", "Other");
        if(whichSensor.equals("Acce")){
            buttonswitch.setText("Acce");
        }
        else if(whichSensor.equals("GPS"))
            buttonswitch.setText("GPS");
        else
            buttonswitch.setText("Other");
        whetherRun = setting.getString("task1Run", "");
        if(whetherRun.equals("true")){
            System.out.println("run true");
            buttongo.setText("Stop");
            long startTime = Long.valueOf(setting.getString("task1Starttime", "-1"));
            long currentTime = System.currentTimeMillis();
            text1.setText("Run for "+(currentTime-startTime)/60000f+" mins using "+whichSensor+" in "+whatRate);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.add(0, 1, 1, "Task 1");
        menu.add(0, 2, 2, "Task 2");
        menu.add(0, 3, 3, "Task 3");
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == 1) {
            buttonrate.setVisibility(View.VISIBLE);
            buttonswitch.setVisibility(View.VISIBLE);
            buttongo.setVisibility(View.VISIBLE);
            button3go.setVisibility(View.INVISIBLE);
            button3switch.setVisibility(View.INVISIBLE);
            button3loc.setVisibility(View.INVISIBLE);
            button2go.setVisibility(View.INVISIBLE);
            button2switch.setVisibility(View.INVISIBLE);
            if(mWifiManager!=null)
                mWifiManager.setWifiEnabled(false);
        }
        else if(id == 2){
            button2go.setVisibility(View.VISIBLE);
            button2switch.setVisibility(View.VISIBLE);
            buttonrate.setVisibility(View.INVISIBLE);
            buttonswitch.setVisibility(View.INVISIBLE);
            buttongo.setVisibility(View.INVISIBLE);
            button3loc.setVisibility(View.INVISIBLE);
            button3switch.setVisibility(View.INVISIBLE);
            button3go.setVisibility(View.INVISIBLE);
        }
        else if(id == 3){
            mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            mWifiManager.setWifiEnabled(true);
            mWifiManager.startScan();
            button3switch.setVisibility(View.VISIBLE);
            button3loc.setVisibility(View.VISIBLE);
            button3go.setVisibility(View.VISIBLE);
            button2go.setVisibility(View.INVISIBLE);
            button2switch.setVisibility(View.INVISIBLE);
            buttongo.setVisibility(View.INVISIBLE);
            buttonswitch.setVisibility(View.INVISIBLE);
            buttonrate.setVisibility(View.INVISIBLE);
        }


        return super.onOptionsItemSelected(item);
    }

    OnClickListener click1_switch = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(buttonswitch.getText().toString().equals("Acce"))
                buttonswitch.setText("GPS");
            else if(buttonswitch.getText().toString().equals("GPS"))
                buttonswitch.setText("Other");
            else
                buttonswitch.setText("Acce");
        }
    };
    OnClickListener click1_go = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(buttongo.getText().toString().equals("Go")) {
                task1Run = true;
                long startTime = System.currentTimeMillis();
                SharedPreferences setting = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = setting.edit();
                //editor.putString("task1Starttime", Objects.toString(startTime));
                editor.putString("task1Starttime", "" + startTime);
                editor.putString("task1Sensor", buttonswitch.getText().toString());
                editor.putString("task1Run", "true");
                editor.putString("task1Rate", buttonrate.getText().toString());
                float startLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)/
                        (float)batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                editor.putFloat("task1Startlevel", startLevel);
                editor.commit();
                System.out.println("in go " + setting.getFloat("task1Startlevel", 0.0f));
                buttongo.setText("Stop");
                wakeLock.acquire(10000000);   //1 hour
                Toast.makeText(getApplicationContext(),"time saved",Toast.LENGTH_SHORT).show();

                if(buttonswitch.getText().toString().equals("GPS")){
                    gpsreader = new GPSReader(getApplicationContext());
                }
                else if(buttonswitch.getText().toString().equals("Acce")){
                    //http://www.cnblogs.com/taoweiji/p/3620329.html
                    mSensorManager.unregisterListener(MainActivity.this);
                    //startActivity(new Intent(sensorStart, mySerivece));
                    sendBroadcast(new Intent(sensorStart));
                    /*
                    Sensor accelerate = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    if(buttonrate.getText().toString().contains("lo"))          //Sampling rate 200ms
                        mSensorManager.registerListener(MainActivity.this, accelerate, SensorManager.SENSOR_DELAY_NORMAL);
                    else if(buttonrate.getText().toString().contains("edi"))    //Sampling rate 60ms
                        mSensorManager.registerListener(MainActivity.this, accelerate, SensorManager.SENSOR_DELAY_UI);
                    else if(buttonrate.getText().toString().contains("as"))     //Sampling rate 0ms(Fastest)
                        mSensorManager.registerListener(MainActivity.this, accelerate, SensorManager.SENSOR_DELAY_FASTEST);
                    else
                        System.out.println(" Sensor delay WRONG!!");*/
                }
                else {
                    Toast.makeText(getApplicationContext(), "Other activity running", Toast.LENGTH_SHORT).show();
                }
            }
            else {  //STOP
                task1Run = false;
                long currentTime = System.currentTimeMillis();
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                    System.out.println("release");
                }
                else
                    System.out.println("lock not held");
                mSensorManager.unregisterListener(MainActivity.this);
                //startActivity(new Intent(sensorStop));
                sendBroadcast(new Intent(sensorStop));
                SharedPreferences setting = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = setting.edit();
                long startTime = Long.valueOf(setting.getString("task1Starttime", "0"));
                float timeGap = (Math.round((currentTime - startTime)/600))/100f;
                float startLevel = setting.getFloat("task1Startlevel", 0.0f);
                float currentLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -100)/
                        (float)batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                System.out.println(currentLevel + " -- " + startLevel);
                String sensorUsed = setting.getString("task1Sensor", "none");
                String rateUsed = setting.getString("task1Rate", "none");
                editor.putString("task1Run", "false");
                editor.commit();
                buttongo.setText("Go");
                text1.setText("Drain " + (startLevel - currentLevel) * 100 + "% in " +
                        timeGap + " mins using " + sensorUsed + " with " + rateUsed + " speed"+currentLevel+" - "+startLevel);
            }
        }
    };
    OnClickListener click1_rate = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(buttonrate.getText().toString().contains("lo")){ //Slow
                buttonrate.setText("Medium");
                //text1.setText("Sampling rate 200ms");
            }
            else if(buttonrate.getText().toString().contains("edi")){ //Medium
                buttonrate.setText("Fast");
                //text1.setText("Sampling rate 60ms");
            }
            else if(buttonrate.getText().toString().contains("as")){ //Fast
                buttonrate.setText("Slow");
                //text1.setText("Sampling rate 0ms(Fastest)");
            }
        }
    };
    OnClickListener click2_switch = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(button2switch.getText().toString().contains("1"))
                button2switch.setText("2");
            else if(button2switch.getText().toString().contains("2"))
                button2switch.setText("3");
            else if(button2switch.getText().toString().contains("3"))
                button2switch.setText("4");
            else
                button2switch.setText("1");
        }
    };
    OnClickListener click2_go = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(button2go.getText().toString().contains("Go")){
                button2go.setText("Stop");
                SharedPreferences setting = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = setting.edit();
                editor.putLong("task2Starttime", System.currentTimeMillis());
                editor.commit();
                Sensor accelerate = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mSensorManager.unregisterListener(MainActivity.this);
                //mSensorManager.registerListener(MainActivity.this, accelerate, SensorManager.SENSOR_DELAY_NORMAL);
                task2Run = true;
                task2List = new ArrayList<ArrayList<Object>>();
                ArrayList<Object> innerList = new ArrayList<Object>();
                innerList.add("time");
                innerList.add("x");
                innerList.add("y");
                innerList.add("z");
                task2List.add(innerList);
                task2Timestamp = 0.00f;
                sendBroadcast(new Intent(sensorStart));
                myThread = new Thread(new myRunnable());
                myThread.start();
            }
            else{   //STOP
                button2go.setText("Go");
                sendBroadcast(new Intent(sensorStop));
                mSensorManager.unregisterListener(MainActivity.this);
                SharedPreferences setting = getPreferences(MODE_PRIVATE);
                Long startTime = setting.getLong("task2Starttime", 0);
                Long endTime = System.currentTimeMillis();
                float timeGap = (Math.round((endTime - startTime)/600))/100f;
                text1.setText(timeGap+" min with "+button2switch.getText()+"th activity");
                task2Run = false;
                //myThread.stop();
                try {
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ass_9336");
                    dir.mkdir();
                    File task2file = new File(dir,"task2_"+button2switch.getText().toString()+".csv");
                    System.out.println(task2file.getAbsolutePath());
                    task2file.delete();
                    task2file.createNewFile();
                    FileWriter fileWriter = new FileWriter(task2file);
                    for(int i = 0; i<task2List.size();i++){
                        ArrayList<Object> itemList = task2List.get(i);
                        fileWriter.append(itemList.get(0).toString()+",");
                        fileWriter.append(itemList.get(1).toString()+",");
                        fileWriter.append(itemList.get(2).toString()+",");
                        fileWriter.append(itemList.get(3).toString()+"\n");
                        fileWriter.flush();
                    }
                    fileWriter.close();
                    task2List.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("file IO ERROR");
                }
            }
        }
    };
    OnClickListener click3_go = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //mWifiManager.startScan();
            scanResult= (ArrayList<ScanResult>) mWifiManager.getScanResults();
            System.out.println("beginning click3 - scanResult size "+scanResult.size());
            if(button3switch.getText().toString().equals("1")){
                int highRSSI = -1000;
                for(int i =0;i<scanResult.size();i++){
                    if(scanResult.get(i).level > highRSSI) {
                        highResult = scanResult.get(i);
                        highRSSI = highResult.level;
                    }
                }
                levelList = new ArrayList<Integer>();
                levelList.add(highRSSI);
                new Thread(new click3Runnable()).start();
                Toast.makeText(getApplicationContext(), "start", Toast.LENGTH_SHORT).show();
            }
            else if(button3switch.getText().toString().equals("2")){
                int highRSSI = -1000;
                for(int i =0;i<scanResult.size();i++){
                    if(scanResult.get(i).SSID.toLowerCase().equals("uniwide") &&
                            scanResult.get(i).level > highRSSI) {
                        highResult = scanResult.get(i);
                        highRSSI = highResult.level;
                    }
                }
                levelList = new ArrayList<Integer>();
                levelList.add(highRSSI);
                new Thread(new click3Runnable()).start();
                Toast.makeText(getApplicationContext(), "start", Toast.LENGTH_SHORT).show();
            }
            else if(button3switch.getText().toString().equals("3")){    //som_control & [runswift]
                for(int i =0;i<scanResult.size();i++){
                    if(scanResult.get(i).SSID.toLowerCase().equals("runswift")) {
                        highResult = scanResult.get(i);
                        break;
                    }
                }
                levelList = new ArrayList<Integer>();
                levelList.add(highResult.level);
                new Thread(new click3Runnable()).start();
                Toast.makeText(getApplicationContext(), "start", Toast.LENGTH_SHORT).show();
            }
            else{   //4
                for(int i =0;i<scanResult.size();i++){
                    if(scanResult.get(i).SSID.toLowerCase().equals("wang9")) {
                        highResult = scanResult.get(i);
                        break;
                    }
                }
                levelList = new ArrayList<Integer>();
                levelList.add(highResult.level);
                new Thread(new click3Runnable()).start();
                Toast.makeText(getApplicationContext(), "start", Toast.LENGTH_SHORT).show();
            }
        }
        class click3Runnable implements Runnable{
            @Override
            public void run() {
                System.out.println("33333runnable");
                int innerHighRSSI = highResult.level;
                try {
                    for(int j = 0;j<10;j++){
                        mWifiManager.startScan();
                        Thread.sleep(5000);
                        scanResult= (ArrayList<ScanResult>) mWifiManager.getScanResults();
                        System.out.println("inside thread - scanresult size "+scanResult.size());
                        for(ScanResult item:scanResult){
                            //if(item.BSSID.equals(highResult.BSSID)){
                            if(item.SSID.equals(highResult.SSID) && item.level > innerHighRSSI){
                                //levelList.add(item.level);
                                //break;
                                innerHighRSSI = item.level;
                            }
                        }
                        levelList.add(innerHighRSSI);
                    }
                //try {
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ass_9336");
                    dir.mkdir();
                    File task3file = new File(dir,"task3_"+button3switch.getText().toString()+
                                    "_"+button3loc.getText().toString()+".txt");
                    /*System.out.println(getApplicationContext().getCacheDir());
                    FileOutputStream fos = openFileOutput("task3_"+button3switch.getText().toString()+
                            "_"+button3loc.getText().toString()+".txt", Context.MODE_ENABLE_WRITE_AHEAD_LOGGING);
                    PrintStream temp = new PrintStream(fos);*/
                    System.out.println(task3file.getAbsolutePath());
                    task3file.delete();
                    task3file.createNewFile();
                    FileWriter fileWriter = new FileWriter(task3file);
                    for(int item:levelList)
                        fileWriter.append(item+" ");
                    fileWriter.append("SSIDName->"+highResult.SSID);
                    fileWriter.flush();
                    fileWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("exception!");
                }
                System.out.println("task3 write file done");
            }
        }
    };
    OnClickListener click3_switch = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(button3switch.getText().toString().equals("1")){
                button3switch.setText("2");
            }
            else if(button3switch.getText().toString().equals("2")){
                button3switch.setText("3");
            }
            else if(button3switch.getText().toString().equals("3")){
                button3switch.setText("4");
            }
            else{
                button3switch.setText("1");
            }
        }
    };
    OnClickListener click3_loc = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(button3loc.getText().toString().equals("1")){
                button3loc.setText("2");
            }
            else if(button3loc.getText().toString().equals("2")){
                button3loc.setText("3");
            }
            else{
                button3loc.setText("1");
            }
        }
    };
    class PersistService extends Service implements SensorEventListener {
        private float[] accValue = new float[3];

        public BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(task1Run){
                    if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED) ||
                            intent.getAction().equals(Intent.ACTION_BATTERY_OKAY) ||
                            intent.getAction().equals(Intent.ACTION_BATTERY_LOW)){
                        batteryStatus = intent;
                    }
                }
                if (mSensorManager != null && intent.getAction().equals(sensorStart)) {
                    mSensorManager.unregisterListener(PersistService.this);
                    System.out.println(sensorStart);
                    Sensor accelerate = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    if(task2Run) {
                        mSensorManager.registerListener(PersistService.this, accelerate, SensorManager.SENSOR_DELAY_FASTEST);
                        return;
                    }
                    else if(buttonrate.getText().toString().contains("lo"))          //Sampling rate 200ms /100ms
                        mSensorManager.registerListener(PersistService.this, accelerate, SensorManager.SENSOR_DELAY_NORMAL);
                    else if(buttonrate.getText().toString().contains("edi"))    //Sampling rate 60ms  /40ms
                        mSensorManager.registerListener(PersistService.this, accelerate, SensorManager.SENSOR_DELAY_UI);
                    else if(buttonrate.getText().toString().contains("as"))     //Sampling rate 0ms(Fastest) 10ms
                        mSensorManager.registerListener(PersistService.this, accelerate, SensorManager.SENSOR_DELAY_FASTEST);
                    else
                        System.out.println(" Sensor delay WRONG!!");
                }
                if (mSensorManager != null && intent.getAction().equals(sensorStop)) {
                    mSensorManager.unregisterListener(PersistService.this);
                    System.out.println(sensorStop);
                }
            }
        };
        @Override
        public void onDestroy() {
            mSensorManager.unregisterListener(PersistService.this);
            super.onDestroy();
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //Log.i(TAG, "PersistService.onAccuracyChanged().");
        }
        public void onSensorChanged(SensorEvent event) {
            if (task2Run && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                if(task2Read){
                    ArrayList<Object> innerList = new ArrayList<Object>();
                    innerList.add(task2Timestamp);
                    innerList.add(event.values[0]);
                    innerList.add(event.values[1]);
                    innerList.add(event.values[2]);
                    task2List.add(innerList);
                    task2Read = false;
                }
                /*
                Log.i(TAG, "PersistService.TYPE_ACCELEROMETER.");
                accValue = event.values;
                for (int i = 0; i < 3; i++) {
                    builder.append((int) accValue[i]);
                    builder.append(",");
                }
                builder.append((event.timestamp - lastTimestamp) / 1000000);// 采样时间差
                builder.append("\n");
                accView.setText(builder.toString());
                lastTimestamp = event.timestamp;*/
            }
            /*
            if(task1Run){
                System.out.println("onsensorchanged");
            }*/
        }
        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
    class myRunnable implements Runnable{
        @Override
        public void run() {
            try {
                task2Read = true;
                Thread.sleep(200);
                task2Timestamp += 0.01f;
                while(task2Run) {
                    task2Read = true;
                    Thread.sleep(10);
                    task2Timestamp += 0.01f;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(task2Run && event.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
            //dbHelper.insert();
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
