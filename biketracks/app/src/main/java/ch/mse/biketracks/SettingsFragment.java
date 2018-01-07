package ch.mse.biketracks;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import ch.mse.biketracks.adapters.MySettingsRecyclerViewAdapter;
import ch.mse.biketracks.database.DatabaseHelper;
import ch.mse.biketracks.models.Contact;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment {

    private static final int RESULT_PICK_CONTACT = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 5;
    private static final String TAG = "SettingsFragment";
    private ArrayList<Contact> contacts = new ArrayList<>();
    private MySettingsRecyclerViewAdapter settingsAdapter;
    private Context mContext;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_list, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getView().getContext();

        // Create the recycler view with the list of contacts
        settingsAdapter = new MySettingsRecyclerViewAdapter(contacts, mContext);
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(settingsAdapter);

        // Get contacts from DB asynchronously
        new FetchContactsTask().execute();

        // Button to add new contacts
        FloatingActionButton addButton = (FloatingActionButton) getView().findViewById(R.id.add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickContactWrapper();
            }
        });
    }

    private void pickContactWrapper() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                showMessageOKCancel(getString(R.string.allow_access_to_contacts),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[] {Manifest.permission.READ_CONTACTS},
                                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                            }
                        });
                return;
            }
            requestPermissions(new String[] {Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            return;
        }
        pickContact();
    }

    private void pickContact() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Log.v(TAG, "************************************************************ --- in onRequestPermissionsResult from SettingsFragment (READ_CONTACT)");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    pickContact();
                } else {
                    // Permission Denied
                    Toast.makeText(mContext, R.string.permission_denied_contacts, Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void contactPicked(Intent data) {
        Uri uri = data.getData();
        Log.i("Contacts", "contactPicked() uri " + uri.toString());

        // Get contact from uri
        Cursor cursor;
        ContentResolver cr = getActivity().getContentResolver();

        try {
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (null != cur && cur.getCount() > 0) {
                cur.moveToFirst();
                for (String column : cur.getColumnNames()) {
                    Log.i("Contacts", "contactPicked() Contacts column " + column + " : " + cur.getString(cur.getColumnIndex(column)));
                }
            }

            if (cur.getCount() > 0) {
                //Query the content uri
                cursor = getActivity().getContentResolver().query(uri, null, null, null, null);

                if (null != cursor && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    for (String column : cursor.getColumnNames()) {
                        Log.i("Contacts", "contactPicked() uri column " + column + " : " + cursor.getString(cursor.getColumnIndex(column)));
                    }
                }

                cursor.moveToFirst();
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNo = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                Contact contact = new Contact(name, phoneNo);

                new AddContactTask().execute(contact);
            }
            cur.close();

        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok
        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be using multiple startActivityForResult
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;
            }
        } else {
            Log.e("ContactFragment", "Failed to pick contact");
        }
    }

    /**
     * Get contacts from DB asynchronously
     */
    private class FetchContactsTask extends AsyncTask<Void, Void, ArrayList<Contact>> {
        @Override
        protected ArrayList<Contact> doInBackground(Void... voids) {
            return DatabaseHelper.getInstance(mContext).getContacts();
        }

        protected void onPostExecute(ArrayList<Contact> result) {
            for (Contact c : result)
                Log.d("FetchContactsTask", c.getName());
            contacts.clear();
            contacts.addAll(result);
            settingsAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Add contact in DB asynchronously
     */
    private class AddContactTask extends AsyncTask<Contact, Void, Contact> {

        @Override
        protected Contact doInBackground(Contact... contacts) {
            Contact contact = null;
            if (DatabaseHelper.getInstance(mContext).insertContact(contacts[0]) > 0) {
                contact = contacts[0];
            }
            return contact;
        }

        protected void onPostExecute(Contact contact) {
            if (contact == null) {
                Toast.makeText(getActivity(), R.string.contact_already_exists, Toast.LENGTH_SHORT).show();
            } else {
                contacts.add(contact);
                settingsAdapter.notifyDataSetChanged();
            }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(mContext)
                .setMessage(message)
                .setPositiveButton(R.string.ok, okListener)
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }
}
