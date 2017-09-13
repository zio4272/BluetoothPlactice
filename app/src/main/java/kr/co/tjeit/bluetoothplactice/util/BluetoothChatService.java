package kr.co.tjeit.bluetoothpracetice.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by user on 2017-09-13.
 */

public class BluetoothChatService {

    //    블루투스 서버와의 소켓통신을 위한 이름 지정
    private static final String NAME = "BluetoothChat";

    //    통신 규약에 따른 블루투스 UUID
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");


    //    멤버변수들
    private BluetoothAdapter mBtAdapter;
    private Handler mHandler;
    //    접속을 수락하는데 사용되는 쓰레드.
//    접속을 수락 => 누군가 원격에서 나에게 채팅을 요청하면 받아들이는 기능.
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private int mState;

    public static final int STATE_NONE = 0; // 아무것도 하지 않고 있는 상태를 표시.
    public static final int STATE_LISTEN = 1; // 연결이 들어오기를 대기.
    public static final int STATE_CONNECTING = 2; // 외부에 연결을 시도하고 있는 상태.
    public static final int STATE_CONNECTED = 3; // 원격 기기와 연결되어 있는 상태.

    public BluetoothChatService(Context context, Handler handler) {
//        블루투스 채팅이 시작될 때.
//        블루투스 기기 목록을 다루는 어댑터를 받아오기.
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

//        아직 통신을 시작하지 않았으니, 상태값을 아무것도 안하고 있는것으로 기록.

        mState = STATE_NONE;

//        필요한 핸들러를 주입

        mHandler = handler;

    }

    //    현재 연결 상태 값을 세팅.
//    synchronized -> 여러개의 쓰레드가 동시에 접근하는것을 막기 위한 안전장치.
//    여러개의 쓰레드가 동시에 접근해서 값이 꼬이는것을 막는다.
    private synchronized void setState(int state) {
        mState = state;

//        채팅을 하는 화면에 현재 상태를 전달해준다.
//        TODO => 채팅 액티비티가 화면을 수정할 수 있도록 메세지 전달.
//        mHandler.obtainMessage(BluetoothChat);
    }

    //    현재 상태를 확인하기 위한 메쏘드.
    public synchronized int getState() {
        return mState;
    }

//    채팅을 시작하는 메쏘드.
//    블루투스 채팅 액티비티가 onResume될때 실행.

    public synchronized void start() {
//      연결을 시도하는 쓰레드가 있다면 일단 취소.
        if (mConnectThread != null) {
//            mConnectThread.cancel();
            mConnectThread = null;
        }
//        현재 연결된 쓰레드가 있다면 마찬가지로 취소.

        if (mConnectedThread != null) {
//            mConnectedThread.cancel();
            mConnectedThread = null;
        }

//        실제로 쓰레드를 통해 수신을 시도.

        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }

//        현재 상태가 수신 대기중임을 명시.
        setState(STATE_LISTEN);

    }

    //    이 기기에서 원격으로 연결을 시도하는 메쏘드
//    어떤 기기에 연결할건지 자료로 전달.
    public synchronized void connect(BluetoothDevice device) {

//        연결을 시도중이라면 취소

        if (mState == STATE_CONNECTING) {

            if (mConnectThread != null) {
//                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

//        연결이 되어있어도 취소

        if (mConnectedThread != null) {
//            mConnectedThread.cancel();
            mConnectedThread = null;
        }

//        TODO - device를 자료로 전달
//        mConnectThread = new ConnectThread(device);

        mConnectThread.start();

        setState(STATE_CONNECTING);

    }

    //    연결된 상태를 설정 => 실제 통신
//    재료가 2개.
//    1. 블루투스 소켓 : 기기와 연결된 통신 통로.
//    2. 블루투스 기기 : 실제로 연결한 기기
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {

//        연결 시도 중이면 취소
        if (mConnectThread != null) {

//                mConnectThread.cancel();
            mConnectThread = null;
        }
//        연결 되어있던것도 취소

        if (mConnectedThread != null) {
//            mConnectedThread.cancel();
            mConnectedThread = null;
        }

//        수신 대기중인것도 취소

        if (mAcceptThread != null) {
//            mAcceptThread.cancel();
            mAcceptThread = null;
        }

//        TODO - 연결된 상태의 쓰레드 작업 필요
//        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);

    }

//    모든 쓰레드를 정지하고자 할때 실행.

    public synchronized void stop() {

//        연결 시도 중이면 취소
        if (mConnectThread != null) {

//                mConnectThread.cancel();
            mConnectThread = null;
        }
//        연결 되어있던것도 취소

        if (mConnectedThread != null) {
//            mConnectedThread.cancel();
            mConnectedThread = null;
        }

//        수신 대기중인것도 취소

        if (mAcceptThread != null) {
//            mAcceptThread.cancel();
            mAcceptThread = null;
        }

//        현재 아무것도 하지 않고 있다는것을 명시.
        setState(STATE_NONE);
    }

//    메세지를 상대 기기에 전달하는 기능

    public void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) {
//                연결되지 않은상태이므로 종료
                return;
            }
            r = mConnectedThread;
        }
//        연결된 쓰레드에서 상대에게 메세지를 전달 메쏘드.
//        r.write(out);
    }

    //    연결 실패 시 공지
    private void connectionFailed() {
        setState(STATE_NONE);
    }

    //    통신 중간에 연결이 끊겼을때
    private void connetionLost() {
        setState(STATE_NONE);
    }


    //    원격 접속을 수락하는 기능
    private class AcceptThread extends Thread {

        //    소켓? => 연결부. 서버소켓? 클라/서버 서버의 역할을 하는 소켓
        BluetoothServerSocket mServerSocket = null;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
//                미리 지정한 이름과, UUID를 이용해 통신을 수락.
                tmp = mBtAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mServerSocket = tmp;
        }

        public void run() {
            setName("AcceptThread");
            BluetoothSocket socket = null;

//            while ()
        }

    }

    private class ConnectThread extends Thread {

    }

    private class ConnectedThread extends Thread {

    }

}