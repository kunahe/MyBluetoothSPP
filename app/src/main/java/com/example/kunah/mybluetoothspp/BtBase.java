package com.example.kunah.mybluetoothspp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.UUID;

/**
 * 客户端和服务端的基类，用于管理socket长连接
 */
public class BtBase {
    static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int FLAG_MSG = 0;

    private Listener mListener;
    private BluetoothSocket mSocket;
    private DataOutputStream mOut;
    private boolean isRead;
    private boolean isSending;

    BtBase(Listener listener) {
        mListener = listener;
    }

    /**
     *  循环读取对方数据，若没有数据则阻塞等待
     */
    void loopRead(BluetoothSocket socket) {
        mSocket = socket;
        try {
            if (!mSocket.isConnected())
                mSocket.connect();
            notifyUI(Listener.CONNECTED, mSocket.getRemoteDevice());
            mOut = new DataOutputStream(mSocket.getOutputStream());
            DataInputStream in = new DataInputStream(mSocket.getInputStream());
            isRead = true;

            while (isRead) { // 死循环读取
//                switch (in.readInt()) {
//                    case FLAG_MSG: // 读取短消息
                //int type = in.readInt();
                String msg = in.readUTF();
                String hexMsg = str2Hex(msg);
                        //notifyUI(Listener.MSG, "Received：" + msg);
                notifyUI(Listener.MSG, "[" + App.getTime() + "] Received：" + hexMsg);
                sendMsg("Ack");
//                        break;
//                }
            }
        } catch (Throwable e) {
            close();
        }
    }

    /**
     *  发送短消息
     */
    public void sendMsg(String msg) {
        if (checkSend())
            return;
        isSending = true;
        try {
            mOut.writeInt(FLAG_MSG); // 消息标记
            mOut.writeUTF(msg);
            mOut.flush();
            String cMsg = String.format("<font color='#008577'>[%s] Send: %s</font>", App.getTime(), msg);
            notifyUI(Listener.MSG, cMsg);
        } catch (Throwable e) {
            close();
        }
        isSending = false;
    }

    public void unListener() {
        mListener = null;
    }

    public void close() {
        try {
            isRead = false;
            mSocket.close();
            notifyUI(Listener.DISCONNECTED, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     *  当前设备与指定设备是否连接
     */
    public boolean isConnected (BluetoothDevice dev) {
        boolean connected = (mSocket != null && mSocket.isConnected());
        if (dev == null)
            return connected;
        return connected && mSocket.getRemoteDevice().equals(dev);
    }

    private boolean checkSend() {
        if (isSending) {
            App.toast("正在发送其他数据，请稍后再发...", 0);
            return true;
        }
        return false;
    }

    private void notifyUI(final int state, final Object obj) {
        App.runUI(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mListener != null)
                        mListener.socketNotify(state, obj);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static String str2Hex(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + " " + s4;
        }
        return str;
    }

    public interface Listener {
        int DISCONNECTED = 0;
        int CONNECTED = 1;
        int MSG = 2;

        void socketNotify(int state, Object obj);
    }
}
