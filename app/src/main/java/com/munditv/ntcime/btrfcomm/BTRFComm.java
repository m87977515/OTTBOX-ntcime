package com.munditv.ntcime.btrfcomm;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import com.munditv.ntcime.R;

public final class BTRFComm {

    private static final int REQUEST_ENABLE_BT = 1;
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter = null;
    private String mConnectedDeviceName = null;
    private BluetoothChatService mChatService = null;
    private boolean isConnected = false;
    private String readMessage;
    private String mStatusString;
    private Handler mIMEHandler;

    public boolean initialize(Context context, Handler handler) {
        mContext = context;
        mIMEHandler = handler;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }

        return true;
    }

    public boolean Destroy() {
        if (mChatService != null) {
            mChatService.stop();
        }
        return true;
    }

    private void setupChat() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(mContext, mHandler);
    }

    private void setStatus(CharSequence subTitle) {
        mStatusString = subTitle.toString();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(mContext.getString(R.string.title_connected_to) + mConnectedDeviceName);
                            //mConversationArrayAdapter.clear();
                            isConnected = true;
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(mContext.getString(R.string.title_connecting));
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(mContext.getString(R.string.title_not_connected));
                            isConnected = false;
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    readMessage = new String(readBuf, 0, msg.arg1);
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    break;
                case Constants.MESSAGE_TOAST:
                    break;
            }
        }

    };

    public String getMessage() {
        return readMessage;
    }

    public String getStatusString() {
        return mStatusString;
    }

    public boolean getConnectStatus() {
        return isConnected;
    }
}