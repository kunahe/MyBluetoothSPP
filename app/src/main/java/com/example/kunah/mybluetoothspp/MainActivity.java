package com.example.kunah.mybluetoothspp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.bean.TieBean;
import com.dou361.dialogui.listener.DialogUIItemListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BtBase.Listener,
        BtReceiver.Listener { //BtDevAdapter.Listener{

    private Activity mActivity;
    //private Context mContext;
    private BtDevWidget mbdw = new BtDevWidget(new DialogUIItemListener(){
        @Override
        public void onItemClick(CharSequence text, int position) {
            String[] str = text.toString().split("\n");
            //Log.i("JAVA_SPLIT", str[0] + " " + str[1]);
            BluetoothDevice dev = mbdw.findDevByAddress(str[1]);
            if (mClient.isConnected(dev)) {
                //App.toast(text.toString() + " " + position, 0);
                App.toast("already connected " + dev.getAddress(), 0);
                return;
            }
            mClient.connect(dev);
            App.toast("Connecting " + dev.getAddress(), 0);
//            mTips.setText("正在连接...");
        }
    });
    private BtReceiver mBtReceiver;
    private final BtClient mClient = new BtClient(this);
    private TextView mLogs;
    private EditText mInputMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mLogs = findViewById(R.id.tv_log);
        mInputMsg = findViewById(R.id.editText);

        mActivity = this;                                      // vital !!
        //mContext = getApplication();                           // vital !!
        //DialogUIUtils.init(mContext);                          // vital !!
        mBtReceiver = new BtReceiver(this, this);//注册蓝牙广播

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

//                List<TieBean> strings = new ArrayList<TieBean>();
//                strings.add(new TieBean("1"));
//                strings.add(new TieBean("2"));
//                strings.add(new TieBean("3"));
//
//                DialogUIUtils.showSheet(mActivity, strings, "", Gravity.CENTER, true, true, new DialogUIItemListener() {
//                    @Override
//                    public void onItemClick(CharSequence text, int position) {
//                        //showToast(text);
//                    }
//                }).show();
                BluetoothAdapter.getDefaultAdapter().startDiscovery();
                mbdw.show(mActivity);
            }
        });

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            //Toast.makeText(this, "Cannot find Bluetooth hardware.", Toast.LENGTH_LONG ).show();
            App.toast("Cannot find Bluetooth hardware.", 0);
            Log.e("MainActivity", "Cannot find Bluetooth hardware.");
            finish();
        } else {
            //Toast.makeText(this, "Got default bluetooth adapter.", Toast.LENGTH_LONG).show();
            if (!btAdapter.isEnabled()) {
                //Toast.makeText(this, "Please turn on Bluetooth switch.", Toast.LENGTH_LONG).show();
                App.toast("Please turn on Bluetooth switch.", 0);
                Log.e("MainActivity", "Please turn on Bluetooth switch.");
                // 跳转开启蓝牙界面
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 112);
            }
        }

        // android 6.0 动态请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            for (String str : permissions) {
                if (checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, 111);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBtReceiver);
        mClient.unListener();
        mClient.close();
    }

    public void onClearLog(View view) {
        mLogs.setText("LOG:");
    }

    public void onDisconnect(View view){
        if (mClient.isConnected(null)) {
            mClient.close();
            App.toast("Disconnecting...", 0);
        } else {
            App.toast("Not connected.", 0);
        }
    }

    public void onSendMsg(View view) {
        if (mClient.isConnected(null)) {
            String msg = mInputMsg.getText().toString();
            if (TextUtils.isEmpty(msg))
                App.toast("Input cannot be empty.", 0);
            else
                mClient.sendMsg(msg);
        } else {
            App.toast("Not connected.", 0);
        }
    }

    @Override
    public void foundDev(BluetoothDevice dev) {
        //Log.i("addItems", "####foundDev");
        mbdw.addItem(dev);
    }

    @Override
    public void socketNotify(int state, final Object obj) {
        if (isDestroyed())
            return;
        String msg = null;
        switch (state) {
            case BtBase.Listener.CONNECTED:
                BluetoothDevice dev = (BluetoothDevice) obj;
                msg = String.format("[%s] Connected with %s(%s)", App.getTime(), dev.getName(), dev.getAddress());
                App.toast(msg, 0);
                mLogs.append("\n" + msg);
                break;
            case BtBase.Listener.DISCONNECTED:
                msg = "[" + App.getTime() + "] Disconnected";
                App.toast(msg, 0);
                mLogs.append("\n" + msg);
                break;
            case BtBase.Listener.MSG:
                msg = String.format("%s", obj);
                mLogs.append("\n");
                mLogs.append(Html.fromHtml(msg));

                break;
        }

    }


}
