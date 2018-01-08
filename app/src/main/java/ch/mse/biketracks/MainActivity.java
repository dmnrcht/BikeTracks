package ch.mse.biketracks;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ch.mse.biketracks.database.DatabaseHelper;
import ch.mse.biketracks.models.Contact;
import ch.mse.biketracks.utils.MyTools;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 1; // Request SEND_SMS + ACCESS_FINE_LOCATION
    private FragmentManager fragmentManager;
    private MapFragment mapFragment;
    private TracksFragment tracksFragment;
    private SettingsFragment settingsFragment;
    private Context mContext;
    private FusedLocationProviderClient mFusedLocationClient;

    private static final String BACK_STACK_MAP = "map_fragment";
    private static final String BACK_STACK_TRACKS = "tracks_fragment";
    private static final String BACK_STACK_SETTINGS = "settings_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContext = getApplicationContext();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_map);

        // Use dynamic fragments
        mapFragment = new MapFragment();
        tracksFragment = new TracksFragment();
        settingsFragment = new SettingsFragment();


        if (mapFragment != null) {
            fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.content_main, mapFragment).commit();
        }

        // Setup urgency button
        Button urgencyButton = (Button) findViewById(R.id.urgency);
        // urgencyButton.setVisibility(View.INVISIBLE); // TODO comment/uncomment to pass/fail MainActivityInstrumentedTest
        urgencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.confirm_urgency)
                        .setMessage(R.string.confirm_urgency_description)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                sendSMSWrapper();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
    }

    /**
     * Send SMS but check first for permissions SMS + GPS
     */
    protected void sendSMSWrapper() {
        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("GPS");
        if (!addPermission(permissionsList, Manifest.permission.SEND_SMS))
            permissionsNeeded.add("SMS");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                StringBuilder message = new StringBuilder(getString(R.string.grant_access) + permissionsNeeded.get(0));
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message.append(", ").append(permissionsNeeded.get(i));
                message.append(getString(R.string.secure_contacts));
                showMessageOKCancel(message.toString(),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(this, permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }

        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        if (MyTools.isLocationEnabled(this.mContext))
            sendSMS();
        else {
            Toast.makeText(getApplicationContext(), R.string.enable_location_first,
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                return false;
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton(R.string.ok, okListener)
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    private void sendSMS() {
        // Fetch contact. The sending is made in the inner class
        new SMSContactsTask().execute();
    }

    private class SMSContactsTask extends AsyncTask<Void, Void, ArrayList<Contact>> {
        @Override
        protected ArrayList<Contact> doInBackground(Void... voids) {
            return DatabaseHelper.getInstance(mContext).getContacts();
        }

        protected void onPostExecute(ArrayList<Contact> result) {
            sendSMSMessageWrapper(result);
        }
    }

    public void sendSMSMessageWrapper(List<Contact> contacts) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            mFusedLocationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        Location currentLocation = task.getResult();
                        sendSMSMessage(contacts, currentLocation);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.error_fetching_position,
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (SecurityException e) {
            Log.d(TAG, e.getMessage());
            Toast.makeText(getApplicationContext(), R.string.position_permission_not_granted,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void sendSMSMessage(List<Contact> contacts, Location currentLocation) {
        // Prepare SMS
        SmsManager smsManager = SmsManager.getDefault();

        // IMPORTANT : If message is too long, SMS won't be sent.
        String message = //"This is a test for the mobile application BikeTracks. " +
                getString(R.string.bike_accident_sms_1) +
                        getString(R.string.bike_accident_sms_2);
        String location = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=%f,%f", currentLocation.getLatitude(), currentLocation.getLongitude());
        String signature = ""; // "Message delivered by BikeTracks.";
        String body = String.format(Locale.ENGLISH, "%s %s %s", message, location, signature);

        Log.d(TAG, body);

        // Send SMS to all emergency contacts
        for (Contact contact : contacts) {
            Log.d(TAG, contact.getPhoneNumber());
            smsManager.sendTextMessage(contact.getPhoneNumber(), null, body, null, null);
        }

        // Feedback to user
        Toast.makeText(getApplicationContext(), R.string.urgency_message_sent,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.v(TAG, "************************************************************ --- in onRequestPermissionsResult from MainActivity (access SMS)");

        // Permissions requested from fragments are handled here in MainActivity.
        // This trick is used to handle permissions from inside the fragment
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }

        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_CONTACTS, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    sendSMS();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT)
                            .show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_main, mapFragment).commit();
        } else if (id == R.id.nav_parcours) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_main, tracksFragment).commit();
        } else if (id == R.id.nav_settings) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_main, settingsFragment).commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
