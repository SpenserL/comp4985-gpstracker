package com.datacom.cst.dcgps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_REFRESH = 5000; // 5 sec
    private static final int LOCATION_RANGE = 15; // 15 meters

    private static final int PORT = 4985;
    private static String deviceId;
    private static String deviceName = Build.MODEL;

    private LocationManager locationManager = null;
    private LocationListener locationListener = null;
    private Location location = null;

    private static String latitude = "Calculating...";
    private static String longitude = "Calculating...";

    protected ConnectTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        deviceId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

        ((TextView)findViewById(R.id.deviceValue)).setText(deviceId);
        ((TextView)findViewById(R.id.devNameValue)).setText(deviceName);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean initLocationServices() {

        // check if permissions are granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // if not, ask for permission
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    0
            );
            return false;
        }

        locationListener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH, LOCATION_RANGE, locationListener);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // for testing
//        location = new Location("dummymanager");
//        location.setLatitude(48.5000);
//        location.setLongitude(-122.5000);
        return  true;
    }

    public String getName() {
        String name =((EditText)findViewById(R.id.nameValue)).getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter a username!", Toast.LENGTH_LONG).show();
            return "";
        }
        return name;
    }

    public void updateLocationUI() {
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
        ((TextView)findViewById(R.id.latValue)).setText(latitude);
        ((TextView)findViewById(R.id.longValue)).setText(longitude);
    }

    public void sendToServer() {
        String ip = ((EditText)findViewById(R.id.ipValue)).getText().toString();
        String name = getName();

        if (name.isEmpty()) {
            return;
        }

        task = new ConnectTask();
        task.execute(ip, name, deviceId, deviceName, latitude, longitude);
    }

    public void connectBtn(View view) {
        if (locationListener == null) {
            if (!initLocationServices()) {
                return;
            }
        }
        updateLocationUI();
        sendToServer();
    }

    public void disconnect() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager != null && locationListener != null) {
                locationManager.removeUpdates(locationListener);
            }
            locationListener = null;
        }
        ((TextView)findViewById(R.id.statusValue)).setText("Disconnected");
        ((TextView)findViewById(R.id.latValue)).setText("---");
        ((TextView)findViewById(R.id.longValue)).setText("---");
    }

    public void disconnectPress(View view) {
        task.cancel(true);
        // check if permissions are granted
        disconnect();
    }

    private class ConnectTask extends AsyncTask<String, Void, String> {

        private Socket client = null;

        protected String doInBackground(String... args) {

            try {
                String serverIp = args[0];

                client = new Socket(serverIp, PORT);
                OutputStream os = client.getOutputStream();
                DataOutputStream out = new DataOutputStream(os);

                String jsonMsg = new JSONObject()
                        .put("name", args[1])
                        .put("deviceId", args[2])
                        .put("deviceName", args[3])
                        .put("latitude", args[4])
                        .put("longitude", args[5]).toString();

                out.writeUTF(jsonMsg);

                if (isCancelled() && client != null) {
                    client.close();
                }

                return "Connected";
            } catch (ConnectException ce) {
                return "Server Unavailable";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error";
            }
        }
        protected void onPostExecute(String recmsg) {

            try {
                ((TextView)findViewById(R.id.statusValue)).setText(recmsg);

                if (client != null) {
                    client.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            location.setLatitude(loc.getLatitude());
            location.setLongitude(loc.getLongitude());
            updateLocationUI();
            sendToServer();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}
