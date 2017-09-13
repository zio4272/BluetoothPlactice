package kr.co.tjeit.bluetoothplactice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import kr.co.tjeit.bluetoothplactice.adapter.BtNewDeviceAdapter;
import kr.co.tjeit.bluetoothplactice.adapter.BtPairedDeviceAdapter;
import kr.co.tjeit.bluetoothplactice.data.BtDevice;

public class DeviceListMainActivity extends BaseActivity {

//    인텐트에서 상대방 장비의 주소를 넘겨줄 떄 쓰는 메모
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter mBtAdapter;
    private android.widget.Button scanBtn;
    private android.widget.ListView newDeviceListView;
    private android.widget.ListView pairedDeviceListView;


    List<BtDevice> newDeviceList = new ArrayList<>();
    BtNewDeviceAdapter mBtListAdapter;

    //    기존에 페어링된 기기들을 보여주기 위한 리스트/어댑터
    List<BtDevice> pairedDeviceList = new ArrayList<>();
    BtPairedDeviceAdapter pairedDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list_main);
        bindViews();
        setupEvents();
        setValues();
    }

    @Override
    public void setupEvents() {

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                탐색을 시작
                doDiscovery();

//                탐색이 진행중일땐 다시 탐색을 시작할 수 없도록.
                scanBtn.setVisibility(View.GONE);
            }
        });

        AdapterView.OnItemClickListener deviceClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                만약 기기를 찾는중이라면 연결을 위해 탐색 중지
                mBtAdapter.cancelDiscovery();

//                장비의 주소를 가져와야함.

                TextView deviceAddressTxt = (TextView) view.findViewById(R.id.deviceAddressTxt);
                String address = deviceAddressTxt.getText().toString();

//               intent에 연결할 장비의 주소를 넣어줌
//                TODO - 다음화면 진행 필요
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
                startActivity(intent);


            }
        };
        newDeviceListView.setOnItemClickListener(deviceClickListener);
        pairedDeviceListView.setOnItemClickListener(deviceClickListener);

    }

    //    화면이 메모리에서 해제될때 (완전히 사라질때) 실행되는 메쏘드
    @Override
    protected void onDestroy() {
        super.onDestroy();

//        만약. 탐색 작업이 진행중이었다면 탐색을 종료.
        if (mBtAdapter != null) {
//            무작정 취소를 날려도 상황에 따라 알아서 정지기능만 실행.
            mBtAdapter.cancelDiscovery();
        }

//        브로드캐스트 리시버의 기능을 해제

        unregisterReceiver(mReceiver);
    }

    //    주변의 블루투스 기기를 탐색.

    void doDiscovery() {

//        1. 새 기기 목록 리스트뷰를 표시. 페어링 된 목록 리스트 숨김

        newDeviceListView.setVisibility(View.VISIBLE);
        pairedDeviceListView.setVisibility(View.GONE);

//        만약, 이미 기기 탐색을 진행중이라면, 진행중이던 요청은 취소.

        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        mBtAdapter.startDiscovery();

    }

    @Override
    public void setValues() {

//        블루투스 어댑터 초기화
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

//        기존에 페어링 된 기기를 보여줄 리스트뷰 세팅.
        pairedDeviceAdapter = new BtPairedDeviceAdapter(mContext, pairedDeviceList);
        pairedDeviceListView.setAdapter(pairedDeviceAdapter);

//        페어링 된 적 있는 기기들의 목록을 가져옴.
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
//            페어링 된 기기를 가지고 있다.

//            페어링 목록 보여주고, 새 기기 목록을 숨겨주자.
            newDeviceListView.setVisibility(View.GONE);
            pairedDeviceListView.setVisibility(View.VISIBLE);

//            페어링된 기기 목록을 리스트에 추가하고 데이터 새로고침
            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceList.add(new BtDevice(device.getName(), device.getAddress()));
            }
            pairedDeviceAdapter.notifyDataSetChanged();
        }


//        탐색된 블루투스 기기를 보여줄 리스트뷰 세팅
        mBtListAdapter = new BtNewDeviceAdapter(mContext, newDeviceList);
        newDeviceListView.setAdapter(mBtListAdapter);

//        브로드 캐스트 리시버를 등록.

//        수신하고 싶은 방송의 종류를 설정
//        1. 블루투스 기기를 찾았다! 라는 방송을 받겠다.
        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        수신하고 싶은 방송을 등록
//        재료 2가지.
//        1) 방송이 수신되었을 때 진행할 행동을 담은 Receiver => 맨 밑에 따로 작성
//        2) 어떤 방송을 수신할지 설정해둔 IntentFilter
        registerReceiver(mReceiver, foundFilter);


//        방송을 수신하고자 한다 => 기기 탐색이 종료되었음을 알리는 방송

        IntentFilter discoveryEndFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, discoveryEndFilter);

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

//            블루투스 기기를 찾았을 때, 수신되는 신호를 가지고
//            이벤트 처리를 해준다.

//            어떤 방송이 수신되었나?
            String actionName = intent.getAction();

//            1. 기기 탐색(Discovering) 결과 어떤 기기를 발견했을 때.
            if (actionName.equals(BluetoothDevice.ACTION_FOUND)) {

//                방송 데이터 안에 들어있는 블루투스기기 클래스를 받아오기.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

//                찾아낸 기기가, 이미 페어링 된 적이 있다면 무시.
//                새 장비가 아니니, 굳이 페어링 가능 목록에 띄워줄 필요가 없다.

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
//                    아직 연결된 적이 없는 기기.

                    newDeviceList.add(new BtDevice(device.getName(), device.getAddress()));
                    mBtListAdapter.notifyDataSetChanged();


                }
            } else if (actionName.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
//                새로 검색된 기기가 없다면
                if (newDeviceList.size() == 0) {
                    Toast.makeText(context, "검색된 기기가 없습니다.", Toast.LENGTH_SHORT).show();

//                    다시 탐색할 수 있도록 버튼 표시
                    scanBtn.setVisibility(View.VISIBLE);

                }
            }

        }
    };

    @Override
    public void bindViews() {
        this.pairedDeviceListView = (ListView) findViewById(R.id.pairedDeviceListView);
        this.newDeviceListView = (ListView) findViewById(R.id.newDeviceListView);
        this.scanBtn = (Button) findViewById(R.id.scanBtn);
    }
}
