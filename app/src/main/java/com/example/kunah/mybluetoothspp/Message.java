package com.example.kunah.mybluetoothspp;

public class Message {
    private byte[] mSendBuf;
    private byte[] mReceiveBuf;
    static private final int SENDLEN = 5;
    static private final int MSGHEADERLEN = 4;
    private byte mSequence;
    private byte mSegment;
    private byte mReqSequence;
    private byte mReqSegment;
    private byte mTotalSeg;

    Message() {
        mSendBuf = new byte[SENDLEN];
        mReceiveBuf = new byte[1];
        clearSendBuf();
        mSequence = 0;
        mSegment = 0;
        mReqSequence = 0;
        mReqSegment = 0;
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
        byte lastSequence = mSequence;
        byte lastSegment = mSegment;
        byte lastTotalSeg = mTotalSeg;
        byte lastReqSequence = mReqSequence;
        byte lastReqSegment = mReqSegment;

        mSequence = buf[1];
        mSegment = buf[2];
        mTotalSeg = buf[3];

        if (lastReqSequence == mSequence && lastReqSegment == mSegment) {
            // if mSequence and mSegment is required, send required ACK.
            if (mSegment + 1 != mTotalSeg) {
                // this big packet is not sent completely.
                mReqSegment = (byte) (mSegment + (byte) 1);
                mReqSequence = mSequence;
            } else {
                mReqSegment = 0;
                mReqSequence = (byte) (mSequence + (byte) 1);
            }
            mReceiveBuf = joinBuf(mReceiveBuf, 0, buf, MSGHEADERLEN, length);
        } else {
            // else send the same ACK
//            mSequence = lastSequence;
//            mSegment = lastSegment;
            mReqSequence = lastReqSequence;
            mReqSegment = lastReqSegment;
        }


        //mReceiveBuf = joinBuf(mReceiveBuf, 0, buf, SENDLEN, length);
    }

    public byte[] makeAck() {
        //byte[] res = new byte[mSendLen];
        //parseMsg(buf, length);

        mSendBuf[0] = SENDLEN;
        mSendBuf[1] = mSequence;
        mSendBuf[2] = mSegment;
        mSendBuf[3] = mReqSequence;
        mSendBuf[4] = mReqSegment;

        return mSendBuf;
    }

    static public int getSendLen() {
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

    private byte[] joinBuf(byte[] oldBuf, int oldBufPos,  byte[] newBuf, int newBufPos, int newBufLen) {
        byte[] retBuf = new byte[oldBuf.length - oldBufPos + newBufLen - newBufPos];
        //mReceiveBuf = new byte[mReceiveBuf.length + newBuf.length - newBufPos];

        System.arraycopy(oldBuf, oldBufPos, retBuf, 0, oldBuf.length - oldBufPos);
        System.arraycopy(newBuf, newBufPos, retBuf, oldBuf.length - oldBufPos, newBufLen - newBufPos);

        return retBuf;
    }
}
