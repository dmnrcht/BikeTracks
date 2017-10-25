package ch.mse.biketracks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import ch.mse.biketracks.adapters.MySettingsRecyclerViewAdapter;
import ch.mse.biketracks.database.ContactContract;
import ch.mse.biketracks.database.ContactDbHelper;
import ch.mse.biketracks.models.Contact;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment {

    private static final int RESULT_PICK_CONTACT = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private ArrayList<Contact> contacts = new ArrayList<>();
    private MySettingsRecyclerViewAdapter settingsAdapter;

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

        //mContext = getActivity();

        Context context = getView().getContext();

        settingsAdapter = new MySettingsRecyclerViewAdapter(contacts, context);

        // TODO : Database operations should be in AsyncTask or Intent
        // Get the database for read
        ContactDbHelper mDbHelper = new ContactDbHelper(getContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                ContactContract.ContactEntry._ID,
                ContactContract.ContactEntry.COLUMN_NAME_NAME,
                ContactContract.ContactEntry.COLUMN_NAME_NUMBER
        };

        String sortOrder =
                ContactContract.ContactEntry.COLUMN_NAME_NAME + " ASC";

        Cursor cursor = db.query(
                ContactContract.ContactEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        // Clear contacts and recreate them
        contacts.clear();
        // Get all the contacts from the database
        while(cursor.moveToNext()) {
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_NAME_NAME));
            String number = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_NAME_NUMBER));
            contacts.add(new Contact(name, number));
        }
        cursor.close();

        // Create the recycler view with the list of contacts
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(settingsAdapter);

        // Button to add new contacts
        FloatingActionButton addButton = (FloatingActionButton) getView().findViewById(R.id.add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if( getApplicationContext().checkSelfPermission( Manifest.permission.READ_CONTACTS ) != PackageManager.PERMISSION_GRANTED )
                //    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS}, resultValue);
                if (ContextCompat.checkSelfPermission(getActivity(),
                        android.Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            android.Manifest.permission.READ_CONTACTS)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {
                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{android.Manifest.permission.READ_CONTACTS},
                                MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }

                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
            }
        });
    }

    private void contactPicked(Intent data) {
        Uri uri = data.getData();
        Log.i("Contacts", "contactPicked() uri " + uri.toString());
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

                // TODO : Database operations should be in AsyncTask or Intent
                // Get writable database
                ContactDbHelper mDbHelper = new ContactDbHelper(getContext());
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                // Set data returned by query
                String[] projection = {
                        ContactContract.ContactEntry._ID,
                        ContactContract.ContactEntry.COLUMN_NAME_NAME
                };

                // Filter results WHERE column name = contact.name
                String selection = ContactContract.ContactEntry.COLUMN_NAME_NAME + " = ?";
                String[] selectionArgs = { contact.getName() };

                Cursor dbCursor = db.query(
                        ContactContract.ContactEntry.TABLE_NAME,  // The table to query
                        projection,                               // The columns to return
                        selection,                                // The columns for the WHERE clause
                        selectionArgs,                            // The values for the WHERE clause
                        null,                                     // don't group the rows
                        null,                                     // don't filter by row groups
                        null                                      // The sort order
                );

                if(dbCursor.getCount() > 0){
                    Toast.makeText(getActivity(), R.string.contact_already_exists, Toast.LENGTH_SHORT).show();
                } else{
                    // Create a new map of values, where column names are the keys
                    ContentValues values = new ContentValues();
                    values.put(ContactContract.ContactEntry.COLUMN_NAME_NAME, contact.getName());
                    values.put(ContactContract.ContactEntry.COLUMN_NAME_NUMBER, contact.getPhoneNumber());

                    // Insert the new row, returning the primary key value of the new row
                    long newRowId = db.insert(ContactContract.ContactEntry.TABLE_NAME, null, values);

                    contacts.add(contact);
                    settingsAdapter.notifyDataSetChanged();
                }
                dbCursor.close();






            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok
        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be usign multiple startActivityForResult
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;
            }
        } else {
            Log.e("ContactFragment", "Failed to pick contact");
        }
    }
}
