package com.example.kunah.mybluetoothspp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
    private DataOutputStream mOutString;
    private OutputStream mOut;
    private InputStream mIn;
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
        Message msg = new Message();

        try {
            if (!mSocket.isConnected())
                mSocket.connect();
            notifyUI(Listener.CONNECTED, mSocket.getRemoteDevice());
            mOutString = new DataOutputStream(mSocket.getOutputStream());
            mOut = mSocket.getOutputStream();
            mIn = mSocket.getInputStream();
            byte[] buffer = new byte[1024];
            int length;
            isRead = true;
            byte[] sendBuf;

            while (isRead) { // 死循环读取
                //length = mIn.read(buffer);
                length = getMsgLength();

                readByLength(buffer, length);
                //Log.i("LENGTH", ""+ byte2String(buffer, length));
//                String hexMsg = byte2String(buffer, length);
                msg.parseMsg(buffer, length);
                sendBuf = msg.makeAck();
//                String ack = byte2String(sendBuf, msg.getSendLen());
//                sendMsg(ack);
                sendAck(sendBuf);

                if (msg.isGetAll()) {
                    //notifyUI(Listener.MSG, "[" + App.getTime() + "] Received：" + hexMsg);
                    //notifyUI(Listener.MSG, "[" + App.getTime() + "] Received：" + byte2String(msg.getReceiveBuf(), msg.getReceiveBufLen()));
                    notifyUI(Listener.MSG, "[" + App.getTime() + "] Received：" + msg.getReceiveBuf());
                    msg.clearReceiveBuf();
                }
                //sendMsg("#########");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            close();
        }
    }

    private int getMsgLength() {
        int byte1 = 0, byte2 = 0;

        try {
            byte1 = mIn.read();
            byte2 = mIn.read();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (byte1 != -1 && byte2 != -1) {
            return byte1 * 256 + byte2;
        } else {
            return -1;
        }
    }

    private void readByLength(byte[] buf, int length){
        for (int i = 0; i < length; i++) {
            try {
                buf[i] = (byte) (mIn.read() & 0xFF);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private String byte2String(byte[] buf, int length) {
        String str = "";
        int i;

        for (i = 0; i < length; i++) {
            str = str + " " + String.format("%02x", (buf[i] & 0xFF));
        }

        return str;
    }


    /**
     *  发送短消息
     */
    public void sendAck(String msg) {
        if (checkSend())
            return;
        isSending = true;
        try {
            //mOut.writeInt(FLAG_MSG); // 消息标记
            mOutString.writeUTF(msg);
            //mOut.write(msg.getBytes());
            mOutString.flush();
            String cMsg = String.format("<font color='#008577'>[%s] Send: %s</font>", App.getTime(), msg);
            notifyUI(Listener.MSG, cMsg);
        } catch (Throwable e) {
            close();
        }
        isSending = false;
    }

    public void sendAck(byte[] buf) {
        if (checkSend())
            return;
        isSending = true;
        try {
            mOut.write(buf);
            mOut.flush();
            String cMsg = String.format("<font color='#008577'>[%s] Send: %s</font>", App.getTime(), byte2String(buf, 4));
            notifyUI(Listener.MSG, cMsg);
        } catch (Throwable e) {
            e.printStackTrace();
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
            int ch = (int) s.charAt(i) & 0xFF;
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
