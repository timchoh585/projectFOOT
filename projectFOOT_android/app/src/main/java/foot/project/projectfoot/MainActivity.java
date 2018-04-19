package foot.project.projectfoot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.AnyThread;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import ca.hss.heatmaplib.HeatMap;
import ca.hss.heatmaplib.HeatMapMarkerCallback;
import foot.project.projectfoot.Util.CalculateFoot;

public class MainActivity extends Activity {

    Button btnOn, btnOff;
    TextView txtArduino, txtString, txtStringLength, sensorView0, sensorView1, sensorView2, sensorView3;
    Handler bluetoothIn;
    private HeatMap map;

    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    HashMap< Integer, ArrayList< Integer > > mapNum = new HashMap();
    double count = 0.0;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final float FACTOR = 1.5f;
    private static final float[] X_LOC = { 0.4f, 0.4f, 0.2f, 0.1f, 0.1f, 0.2f, 0.3f };
    private static final float[] Y_LOC = { 0.1f, 0.2f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f };

    private static String address;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

//        btnOn = (Button) findViewById(R.id.buttonOn);
//        btnOff = (Button) findViewById(R.id.buttonOff);
//        txtString = (TextView) findViewById(R.id.txtString);
//        txtStringLength = (TextView) findViewById(R.id.testView1);
//        sensorView0 = (TextView) findViewById(R.id.sensorView0);
//        sensorView1 = (TextView) findViewById(R.id.sensorView1);
//        sensorView2 = (TextView) findViewById(R.id.sensorView2);
//        sensorView3 = (TextView) findViewById(R.id.sensorView3);

        map = findViewById(R.id.example_map);
        map.setMinimum(0.0);
        map.setMaximum(1023.0);
        map.setLeftPadding(100);
        map.setRightPadding(100);
        map.setTopPadding(100);
        map.setBottomPadding(100);
        map.setMarkerCallback(new HeatMapMarkerCallback.CircleHeatMapMarker(0xff9400D3));
        map.setRadius(200.0);
        Map<Float, Integer> colors = new ArrayMap<>();
        //build a color gradient in HSV from red at the center to green at the outside
        for (int i = 0; i < 21; i++) {
            float stop = ((float)i) / 20.0f;
            int color = doGradient(i * 5, 0, 100, 0xff00ff00, 0xffff0000);
            colors.put(stop, color);
        }
        map.setColorStops(colors);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if ( msg.what == handlerState ) {										//if message is what we want
                    String readMessage = (String) msg.obj;                              // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);      								//keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
//                        txtString.setText("Data Received = " + dataInPrint);
                        int dataLength = dataInPrint.length();							//get length of data received
//                        txtStringLength.setText("String Length = " + String.valueOf(dataLength));

                        if ( recDataString.charAt(0) == '#' )								//if it starts with # we know it is what we are looking for
                        {
                            ArrayList< Integer > current = new ArrayList<>();

                            current.add( Integer.parseInt( recDataString.substring( 1,5 ) ) );
                            current.add( Integer.parseInt( recDataString.substring( 5, 9 ) ) );             //get sensor value from string between indices 1-5
                            current.add( Integer.parseInt( recDataString.substring( 9, 13 ) ) );            //same again...
                            current.add( Integer.parseInt( recDataString.substring( 13, 17 ) ) );
                            current.add( Integer.parseInt( recDataString.substring( 17, 21 ) ) );
                            current.add( Integer.parseInt( recDataString.substring( 21, 25 ) ) );
                            current.add( Integer.parseInt( recDataString.substring( 25, 29 ) ) );

                            try{
                                String exit = recDataString.substring( 29, 30 );
                                if( exit.equals( "X" ) ) {
                                    addToMapping( current );
                                    drawHeatMap();
                                    map.forceRefresh();
                                }
                            } catch( Exception e ) {
                                addToMapping( current );
                            }

//                            drawNewMap(
//                                    Integer.parseInt( forceSensor1 ),
//                                    Integer.parseInt( forceSensor2 ),
//                                    Integer.parseInt( forceSensor3 ),
//                                    Integer.parseInt( forceSensor4 ),
//                                    Integer.parseInt( forceSensor5 ),
//                                    Integer.parseInt( forceSensor6 ),
//                                    Integer.parseInt( forceSensor7 )
//                            );

                            map.forceRefresh();
//
//                            sensorView0.setText(" Force Sensor: " + forceSensor);	//update the textviews with sensor values
//                            sensorView1.setText(" Sensor 1 Voltage = " + sensor1 + "V");
//                            sensorView2.setText(" Sensor 2 Voltage = " + sensor2 + "V");
//                            sensorView3.setText(" Sensor 3 Voltage = " + sensor3 + "V");
                        }
                        recDataString.delete(0, recDataString.length()); 					//clear all string data
                        // strIncom =" ";
                        dataInPrint = " ";
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();


        // Set up onClick listeners for buttons to send 1 or 0 to turn on/off LED
//        btnOff.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                mConnectedThread.write("L");    // Send "0" via Bluetooth
//                Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
//            }
//        });

//        btnOn.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                mConnectedThread.write("H");    // Send "1" via Bluetooth
//                Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
//            }
//        });
    }



    private void addToMapping( ArrayList< Integer > current ) {
        for( int i = 0; i < 8; i++ ) {
            try  {
                ArrayList< Integer > pre = mapNum.get( i );
                pre.add( current.get( i ) );
                mapNum.put( i, pre );
            } catch ( Exception e ) {
                ArrayList< Integer > cur = new ArrayList<>();
                cur.add( current.get( i ) );
                mapNum.put( i, cur );
            }
        }
    }



    @AnyThread
    private void drawHeatMap() {
        CalculateFoot calc = new CalculateFoot( mapNum );
        double[] map = calc.getHeatMap();
        drawNewMap( map );
    }



    @AnyThread
    private void drawNewMap( double[] heatMap ) {
        map.clearData();
        count++;

        for( int i = 0; i < 8; i++ ) {
            map.addData( new HeatMap.DataPoint( X_LOC[i] * FACTOR, Y_LOC[i] * FACTOR, heatMap[i] ) );
        }

//        HeatMap.DataPoint point1 = new HeatMap.DataPoint( 0.400f*factor, 0.100f*factor, s1 );
//        HeatMap.DataPoint point2 = new HeatMap.DataPoint( 0.400f*factor, 0.200f*factor, s2 );
//        HeatMap.DataPoint point3 = new HeatMap.DataPoint( 0.200f*factor, 0.200f*factor, s3 );
//        HeatMap.DataPoint point4 = new HeatMap.DataPoint( 0.100f*factor, 0.300f*factor, s4 );
//        HeatMap.DataPoint point5 = new HeatMap.DataPoint( 0.100f*factor, 0.400f*factor, s5 );
//        HeatMap.DataPoint point6 = new HeatMap.DataPoint( 0.200f*factor, 0.500f*factor, s6 );
//        HeatMap.DataPoint point7 = new HeatMap.DataPoint( 0.300f*factor, 0.600f*factor, s7 );
//        map.addData(point1);
//        map.addData(point2);
//        map.addData(point3);
//        map.addData(point4);
//        map.addData(point5);
//        map.addData(point6);
//        map.addData(point7);

        Log.d( "Data Sending: ", "Updating" );
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();

        try {
            address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        } catch (Exception e) {
            address = "00:21:13:02:5C:98";
        }

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }

        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //TODO: Establish the Bluetooth socket connection.
            }
        }

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            btSocket.close();
        } catch (IOException e2) {
            //TODO: Don't leave Bluetooth sockets open when leaving activity
        }
    }

    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }


    private static int doGradient(double value, double min, double max, int min_color, int max_color) {
        if (value >= max) {
            return max_color;
        }
        if (value <= min) {
            return min_color;
        }
        float[] hsvmin = new float[3];
        float[] hsvmax = new float[3];
        float frac = (float)((value - min) / (max - min));
        Color.RGBToHSV(Color.red(min_color), Color.green(min_color), Color.blue(min_color), hsvmin);
        Color.RGBToHSV(Color.red(max_color), Color.green(max_color), Color.blue(max_color), hsvmax);
        float[] retval = new float[3];
        for (int i = 0; i < 3; i++) {
            retval[i] = interpolate(hsvmin[i], hsvmax[i], frac);
        }
        return Color.HSVToColor(retval);
    }

    private static float interpolate(float a, float b, float proportion) {
        return (a + ((b - a) * proportion));
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String input) {
            byte[] msgBuffer = input.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }
}

