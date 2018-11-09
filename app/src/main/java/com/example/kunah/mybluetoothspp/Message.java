package com.example.kunah.mybluetoothspp;

public class Message {
    private byte[] mSendBuf;
    private byte[] mReceiveBuf;
    private final int SENDLEN = 4;
    private byte mSequence;
    private byte mSegment;
    private byte mReqWin;
    private byte mTotalSeg;

    Message() {
        mSendBuf = new byte[SENDLEN];
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
        // if mSequence, mSegment is required
        // else send the same ACK
        mReceiveBuf = joinBuf(mReceiveBuf, 0, buf, SENDLEN, length);
    }

    public byte[] makeAck() {
        //byte[] res = new byte[mSendLen];
        //parseMsg(buf, length);

        mSendBuf[0] = SENDLEN;
        mSendBuf[1] = mSequence;
        mSendBuf[2] = mSegment;
        mSendBuf[3] = mReqWin;

        return mSendBuf;
    }

    public int getSendLen() {
        return SENDLEN;
    }

    private String convert2String(byte[] buf, int startPos, int length) {
        String str = "";
        int i;

        for (i = startPos; i < length; i++) {
            str = str + " " + String.format("%02x", (buf[i] & 0xFF));
        }

        return str;
    }

    //public byte[] getReceiveBuf () {
    public String getReceiveBuf () {
        //byte[] retBuf = new byte[mReceiveBuf.length - 1];
        //System.arraycopy(mReceiveBuf, 1, mReceiveBuf, 0, mReceiveBuf.length - 1);
        return convert2String(mReceiveBuf, 1, mReceiveBuf.length);
        //return mReceiveBuf;
    }

    public int getReceiveBufLen() {
        return mReceiveBuf.length;
    }

    public boolean isGetAll() {
        return mTotalSeg == mSegment + 1;
    }

    public byte[] joinBuf(byte[] oldBuf, int oldBufPos,  byte[] newBuf, int newBufPos, int newBufLen) {
        byte[] retBuf = new byte[oldBuf.length - oldBufPos + newBufLen - newBufPos];
        //mReceiveBuf = new byte[mReceiveBuf.length + newBuf.length - newBufPos];

        System.arraycopy(oldBuf, oldBufPos, retBuf, 0, oldBuf.length - oldBufPos);
        System.arraycopy(newBuf, newBufPos, retBuf, oldBuf.length - oldBufPos, newBufLen - newBufPos);

        return retBuf;
    }
}
