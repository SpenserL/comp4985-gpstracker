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
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;

    private static final int PORT = 4985;
    private static String deviceId;
    private LocationManager locationManager = null;
    private LocationListener locationListener = null;

    protected ConnectTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        deviceId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
        ((TextView)findViewById(R.id.deviceValue)).setText(deviceId);
        ((TextView)findViewById(R.id.devNameValue)).setText(Build.MODEL);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
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

    private class ConnectTask extends AsyncTask<String, Void, String> {

        private Socket client;

        protected String doInBackground(String... args) {

            try {
                String serverIp = args[0];

                client = new Socket(serverIp, PORT);
                OutputStream os = client.getOutputStream();
                DataOutputStream out = new DataOutputStream(os);

                String jsonMsg = new JSONObject()
                        .put("ip", client.getRemoteSocketAddress().toString())
                        .put("name", args[1])
                        .put("deviceId", args[2])
                        .put("deviceName", args[3])
                        .put("latitude", args[4])
                        .put("longitude", args[5]).toString();

                out.writeUTF(jsonMsg);

                client.close();

                return "Connected";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error";
            }
        }
        protected void onPostExecute(String recmsg) {
//            try {
//                client.close();
//
            ((TextView)findViewById(R.id.statusValue)).setText(recmsg);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    public String getName() {
        String name =((EditText)findViewById(R.id.nameValue)).getText().toString();
        if (name == "") {
            Toast.makeText(getApplicationContext(), "Enter a nickname!", Toast.LENGTH_LONG);
            return null;
        }
        return name;
    }

    public void connectToServer(View view) {
        String ip = ((EditText)findViewById(R.id.ipValue)).getText().toString();
        String name;
        if ((name = getName()) == null) {
            return;
        }

        locationListener = new MyLocationListener();

        // check if permissions are granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // if not, ask for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_ACCESS_COARSE_LOCATION);
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, MY_PERMISSION_ACCESS_FINE_LOCATION);
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 15, locationListener);

        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        String devId = deviceId;
        String devName = Build.MODEL;
        String latitude = String.valueOf(loc.getLatitude());
        String longitude = String.valueOf(loc.getLatitude());

        task = new ConnectTask();
        task.execute(ip, name, devId, devName, latitude, longitude);
    }

    public void disconnectFromServer(View view) {
        task.cancel(true);
        ((TextView)findViewById(R.id.statusValue)).setText("Disconnected");
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            String longitude = "Longitude: " + location.getLongitude();
            Log.v("FUCK", longitude);
            String latitude = "Latitude: " + location.getLatitude();
            Log.v("FUCK", latitude);

            ((TextView)findViewById(R.id.latValue)).setText(latitude);
            ((TextView)findViewById(R.id.longValue)).setText(longitude);
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
