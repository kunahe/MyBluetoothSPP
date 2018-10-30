package com.example.kunah.mybluetoothspp;

public class Message {
    private byte[] mSendBuf;
    private byte[] mReceiveBuf;
    private final int mSendLen = 4;
    private byte mSequence;
    private byte mSegment;
    private byte mReqWin;
    private byte mTotalSeg;

    Message() {
        mSendBuf = new byte[4];
        mReceiveBuf = new byte[1];
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
        mReceiveBuf = new byte[1];
    }
    public void parseMsg(byte[] buf, int length) {
        mSequence = buf[1];
        mSegment = buf[2];
        mTotalSeg = buf[3];
        mReqWin = 0;
        mReceiveBuf = joinBuf(mReceiveBuf, 0, buf, 4, length);
    }

    public byte[] makeAck() {
        //byte[] res = new byte[mSendLen];
        //parseMsg(buf, length);

        mSendBuf[0] = mSendLen;
        mSendBuf[1] = mSequence;
        mSendBuf[2] = mSegment;
        mSendBuf[3] = mReqWin;

        return mSendBuf;
    }

    public int getSendLen() {
        return mSendLen;
    }

    public byte[] getReceiveBuf () {
        return mReceiveBuf;
    }

    public int getReceiveBufLen() {
        return mReceiveBuf.length;
    }

    public boolean isGetAll() {
        return mTotalSeg == mSegment + 1;
    }

    public byte[] joinBuf(byte[] srcBuf, int srcPos,  byte[] destBuf, int destPos, int destLength) {
        byte[] newBuf = new byte[srcBuf.length - srcPos + destLength - destPos];
        //mReceiveBuf = new byte[mReceiveBuf.length + destBuf.length - destPos];

        System.arraycopy(srcBuf, srcPos, newBuf, 0, srcBuf.length - srcPos);
        System.arraycopy(destBuf, destPos, newBuf, srcBuf.length, destLength - destPos);

        return newBuf;
    }
}
