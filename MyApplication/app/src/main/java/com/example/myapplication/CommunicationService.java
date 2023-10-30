package com.example.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;

public class CommunicationService extends Service {
    private Handler socketHandler;
    public CommunicationService() {
        IntentFilter intentFilter = new IntentFilter("communication_service_filter");

        registerReceiver(broadcastReceiver,intentFilter);
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Message message = Message.obtain();
            message.obj = intent.getByteArrayExtra("msg");
            socketHandler.dispatchMessage(message);
        }
    };
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    class SendThread extends Thread {
        Socket socket = null;
        public SendThread() {

        }
        @Override
        public void run() {
            String ip = Utils.getIPAddress(true);


            final WifiManager manager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            final DhcpInfo dhcp = manager.getDhcpInfo();
            int ipAddress = dhcp.gateway;
            InetAddress myAddr = null;
            ipAddress = (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) ?
                    Integer.reverseBytes(ipAddress) : ipAddress;
            byte[] ipAddressByte = BigInteger.valueOf(ipAddress).toByteArray();
            try {
                myAddr = InetAddress.getByAddress(ipAddressByte);
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                Log.e("Wifi Class", "Error getting Hotspot IP address ", e);
            }

            try {
                socket = new Socket(myAddr,50000);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Looper.prepare();
            socketHandler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    byte[] info = (byte[]) msg.obj;
                    try {
                        socket.getOutputStream().write(info);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            Looper.loop();

        }
    }


}

class Utils {
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }

}