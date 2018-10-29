package com.example.kunah.mybluetoothspp;

public class Message {
    private byte[] mSendBuf;
    private byte[] mReceiveBuf;
    private final byte mSendLen = 4;
    private byte mSequence;
    private byte mSegment;
    private byte mReqWin;

    Message() {
        mSendBuf = new byte[4];
        clearSendBuf();
        //clearReceiveBuf();
    }

    public void clearSendBuf() {
        for (byte b: mSendBuf) {
            b = 0;
        }
    }

    public void clearReceiveBuf() {
        for (byte b: mReceiveBuf) {
            b = 0;
        }
    }
    public void parseMsg(byte[] buf) {
        mSequence = buf[1];
        mSegment = buf[2];
        mReqWin = 0;
    }

    public byte[] makeAck(byte[] buf) {
        //byte[] res = new byte[mSendLen];
        parseMsg(buf);

        mSendBuf[0] = mSendLen;
        mSendBuf[1] = mSequence;
        mSendBuf[2] = mSegment;
        mSendBuf[3] = mReqWin;

        return mSendBuf;
    }

    public int getSendLen() {
        return mSendLen;
    }
}
