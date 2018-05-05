package foot.project.projectfoot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.AnyThread;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ca.hss.heatmaplib.HeatMap;
import ca.hss.heatmaplib.HeatMapMarkerCallback;
import foot.project.projectfoot.Util.CalculateFoot;

public class MainActivity extends Activity {

    Handler bluetoothIn;
    private HeatMap map;
    private TextView augment;
    private TextView time;
    private Button clearData;

    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    HashMap< Integer, ArrayList< Integer > > mapNum = new HashMap();
    double count = 0.0;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final float FACTOR = 1.5f;
    private static final float[] X_LOC = { 0.48f, 0.48f, 0.3f, 0.2f, 0.2f, 0.25f, 0.3f };
    private static final float[] Y_LOC = { 0.05f, 0.2f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f };

    private static final int RECTUS = 0;
    private static final int CAVUS = 2;
    private static final int PLANUS = 1;



    private static String address;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView( R.layout.activity_main );

        time = findViewById( R.id.step_time );
        augment = findViewById( R.id.augment_description );
        clearData = findViewById( R.id.clear_data );
        map = findViewById( R.id.left_foot );
        map.setMinimum(0.0);
        map.setMaximum(767.0);
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

        clearData.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearLocalData();
            }
        });

        setupBluetooth();
        loadOldData();

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
    }

    private void loadOldData() {
        Context context = this;
        SharedPreferences sharedPref = context.getSharedPreferences( getString( R.string.preference_file_key ), Context.MODE_PRIVATE );

        if( sharedPref.contains( "points" ) && sharedPref.contains( "time" ) ) {
            float savedTime = sharedPref.getFloat( "time", 0.0f );
            String savedString = sharedPref.getString("points", "");

            String[] history = savedString.split( ",", 7 );
            try {
                for (int i = 0; i < 7; i++) {
                    double overTime = Double.parseDouble( history[i] ) / savedTime;
                    map.addData( new HeatMap.DataPoint( X_LOC[i] * FACTOR, Y_LOC[i] * FACTOR,  overTime ) );
                    setTextOfTime( ( double ) savedTime );
                }
            } catch ( Exception e ) {
            }
        }
    }

    private void clearLocalData() {
        Context context = this;
        SharedPreferences sharedPref = context.getSharedPreferences( getString( R.string.preference_file_key ), Context.MODE_PRIVATE );

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        map.clearData();
        map.forceRefresh();
        time.setText( R.string.empty_data );
        augment.setText( R.string.empty_augment );
    }


    @SuppressLint("HandlerLeak")
    private void setupBluetooth() {
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if ( msg.what == handlerState ) {										//if message is what we want
                    String readMessage = (String) msg.obj;                              // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);      						    //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);// extract string
//                        txtString.setText("Data Received = " + dataInPrint);
                        int dataLength = dataInPrint.length();							//get length of data received
//                        txtStringLength.setText("String Length = " + String.valueOf(dataLength));

                        if ( recDataString.charAt(0) == '#' )								//if it starts with # we know it is what we are looking for
                        {
                            ArrayList< Integer > current = new ArrayList<>();

                            current.add( Integer.parseInt( recDataString.substring( 1,5 ) ) );
                            current.add( Integer.parseInt( recDataString.substring( 5, 9 ) ) );
                            current.add( Integer.parseInt( recDataString.substring( 9, 13 ) ) );
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
                                } else {
                                    addToMapping( current );
                                }
                            } catch( Exception e ) {
                                addToMapping( current );
                            }
                        }
                        recDataString.delete( 0, recDataString.length() ); 					//clear all string data
                    }
                }
            }
        };
    }



    private void addToMapping( ArrayList< Integer > current ) {
        for( int i = 0; i < 7; i++ ) {
            try {
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



    public void updateText( String message ){
        augment.setText( message );
    }



    @AnyThread
    private void drawHeatMap() {
        CalculateFoot calc = new CalculateFoot( mapNum );
        double[] map = calc.getHeatMap();
        double time = calc.getTimeOverMS();

            switch (checkForMAI(map)) {
                case -1:
                    updateText("You need to see a doctor!");
                    break;
                case RECTUS:
                    updateText("Keep up the GOOD WORK");
                    break;
                case PLANUS:
                    updateText("We're noticing a flatfooted walk. Make sure you lift off more as you walk," +
                            " and put more pressure on the ball and heel of your foot!");
                    break;
                case CAVUS:
                    updateText("We're noticing a high-arched walk. You're  putting too much pressure" +
                            "on the ball and heel of your foot. Reduce the pressure there! ");
                    break;
            }


        drawNewMap( map, time, calc );
    }



    @AnyThread
    private void drawNewMap( double[] heatMap, double time, CalculateFoot calc ) {
        map.clearData();
        mapNum.clear();
        double[] newMap = updateHistory( heatMap, time, calc );

        for( int i = 0; i < 7; i++ ) {
            map.addData( new HeatMap.DataPoint( X_LOC[i] * FACTOR, Y_LOC[i] * FACTOR, newMap[i] ) );
        }

        Log.d( "Data Sending: ", "Updating" );
    }


    private void setTextOfTime( double time ) {
        this.time.setText( "Total Time recorded: " + Double.toString( round( time, 2 ) ) + " seconds" );
    }


    private static double round( double value, int places ) {
        if ( places < 0 ) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal( Double.toString( value ) );
        bd = bd.setScale( places, RoundingMode.HALF_UP );
        return bd.doubleValue();
    }



    private int checkForMAI( double[] current ) {
        double sectionA = current[0] + current[1] + current[2];
        double sectionB = current[3] + current[4] + current[5];
        double sectionC = current[6];

        double mai = sectionB / ( sectionA + sectionB + sectionC );

        if( 0 < mai && 0.3 > mai ) {
            return CAVUS;
        }
        else if( 0.31 < mai && 0.463 > mai ) {
            return RECTUS;
        }
        else if( 0.564 < mai && 0.913 > mai ) {
            return PLANUS;
        }
        else { return -1; }
    }



    private double[] updateHistory( double[] current, double time, CalculateFoot calc ) {
        Context context = this;
        SharedPreferences sharedPref = context.getSharedPreferences( getString( R.string.preference_file_key ), Context.MODE_PRIVATE );

        float savedTime = sharedPref.getFloat( "time", 0.0f );
        String savedString = sharedPref.getString("points", "");

        String[] history = savedString.split( ",", 7 );
        double[] savedList = new double[7];
        try {
            for (int i = 0; i < 7; i++) {
                savedList[i] = Double.parseDouble(history[i]) + current[i];
            }
        } catch ( Exception e ) {
            savedList = current;
        }

        SharedPreferences.Editor editor = sharedPref.edit();

        StringBuilder str = new StringBuilder();
        for( int i = 0; i < 7; i++ ) {
            if( 6 == i ) { str.append( savedList[i] ); }
            else { str.append( savedList[i] ).append( "," ); }
        }
        savedTime += time;

        editor.putString( "points", str.toString() );
        editor.putFloat( "time", savedTime );

        editor.apply();
        setTextOfTime( savedTime );


        return calc.calculateHeatMap( savedList, savedTime );
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
        float frac = (float)( ( value - min ) / ( max - min ) );
        Color.RGBToHSV(Color.red( min_color ), Color.green( min_color ), Color.blue( min_color ), hsvmin);
        Color.RGBToHSV(Color.red( max_color ), Color.green( max_color ), Color.blue( max_color ), hsvmax);
        float[] retval = new float[3];
        for (int i = 0; i < 3; i++) {
            retval[i] = interpolate(hsvmin[i], hsvmax[i], frac);
        }
        return Color.HSVToColor(retval);
    }



    private static float interpolate(float a, float b, float proportion) {
        return ( a + ( ( b - a ) * proportion ) );
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
                    bytes = mmInStream.read( buffer );
                    String readMessage = new String( buffer, 0, bytes );
                    bluetoothIn.obtainMessage( handlerState, bytes, -1, readMessage ).sendToTarget();
                } catch ( IOException e ) {
                    break;
                }
            }
        }



        public void write( String input ) {
            byte[] msgBuffer = input.getBytes();
            try {
                mmOutStream.write( msgBuffer );
            } catch ( IOException e ) {
                Toast.makeText( getBaseContext(), "Connection Failure", Toast.LENGTH_LONG ).show();
                finish();

            }
        }
    }
}

