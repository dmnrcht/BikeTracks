package ch.mse.biketracks.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import ch.mse.biketracks.models.Contact;
import ch.mse.biketracks.models.Point;
import ch.mse.biketracks.models.Track;

import static ch.mse.biketracks.database.DatabaseContract.ContactEntry.SQL_CREATE_TABLE_CONTACT;
import static ch.mse.biketracks.database.DatabaseContract.ContactEntry.SQL_DROP_TABLE_CONTACT;
import static ch.mse.biketracks.database.DatabaseContract.PointEntry.SQL_CREATE_TABLE_POINT;
import static ch.mse.biketracks.database.DatabaseContract.PointEntry.SQL_DROP_TABLE_POINT;
import static ch.mse.biketracks.database.DatabaseContract.TrackEntry.SQL_CREATE_TABLE_TRACK;
import static ch.mse.biketracks.database.DatabaseContract.TrackEntry.SQL_DROP_TABLE_TRACK;

/**
 * SQLiteOpenHelper handling biketracks database
 * Defined as a singleton to ensure only 1 instance of the db helper
 *
 * NOTE : Support multithreading : if 2 threads ask getInstance at same time, the same instance will
 * be given to each thread, thanks to synchronize mechanism.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "biketracks.db";
    private static final String TAG = DatabaseHelper.class.getSimpleName(); // For log

    private static DatabaseHelper mInstance = null;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_CONTACT);
        db.execSQL(SQL_CREATE_TABLE_TRACK);
        db.execSQL(SQL_CREATE_TABLE_POINT);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DROP_TABLE_POINT);
        db.execSQL(SQL_DROP_TABLE_TRACK);
        db.execSQL(SQL_DROP_TABLE_CONTACT);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    // **********************
    // DATABASE OPERATIONS
    // **********************

    /* TRACK */

    /**
     * Get tracks
     * @param isSortedByDate set to true for most recent date first, false for most older date first
     * @return an array of tracks
     */
    public ArrayList<Track> getTracks(boolean isSortedByDate) {
        ArrayList<Track> tracks = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String[] projection = new String[] { "*" };
        String where = null;
        String whereArgs[] = null;
        String groupBy = null;
        String having = null;
        String order = String.format("%s %s",DatabaseContract.TrackEntry.COLUMN_NAME_DATE,
                isSortedByDate ? "DESC" : "ASC");

        Cursor cursor = db.query(DatabaseContract.TrackEntry.TABLE_NAME, projection,
                where, whereArgs, groupBy, having, order);

        while(cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(
                    DatabaseContract.TrackEntry.COLUMN_NAME_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(
                    DatabaseContract.TrackEntry.COLUMN_NAME_NAME));
            Date date = new Date(1000L * cursor.getInt(cursor.getColumnIndexOrThrow(
                    DatabaseContract.TrackEntry.COLUMN_NAME_DATE)));
            int duration_s = cursor.getInt(cursor.getColumnIndexOrThrow(
                    DatabaseContract.TrackEntry.COLUMN_NAME_DURATION));
            int distance = cursor.getInt(cursor.getColumnIndexOrThrow(
                    DatabaseContract.TrackEntry.COLUMN_NAME_DISTANCE));
            int climb = cursor.getInt(cursor.getColumnIndexOrThrow(
                    DatabaseContract.TrackEntry.COLUMN_NAME_CLIMB));
            int descent = cursor.getInt(cursor.getColumnIndexOrThrow(
                    DatabaseContract.TrackEntry.COLUMN_NAME_DESCENT));
            float speed = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    DatabaseContract.TrackEntry.COLUMN_NAME_SPEED));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(
                    DatabaseContract.TrackEntry.COLUMN_NAME_TYPE));
            byte[] image = cursor.getBlob(cursor.getColumnIndexOrThrow(
                    DatabaseContract.TrackEntry.COLUMN_NAME_IMAGE));

            // Get points
            ArrayList<Point> points = getPoints(id, db);

            tracks.add(new Track(id, name, date, duration_s, distance, climb, descent, speed, type, points, image));
        }
        cursor.close();

        return tracks;
    }

    /**
     * Add a track to database
     * @param track the track with : name, climb, descent, distance, type, date, duration, image, points
     *              Each point of the track must have : lat, lng, elev, time in ms
     * @return the inserted id of the track
     */
    public long insertTrack(Track track) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Insert track
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.TrackEntry.COLUMN_NAME_NAME, track.getName());
        values.put(DatabaseContract.TrackEntry.COLUMN_NAME_CLIMB, track.getClimb());
        values.put(DatabaseContract.TrackEntry.COLUMN_NAME_DESCENT, track.getDescent());
        values.put(DatabaseContract.TrackEntry.COLUMN_NAME_DISTANCE, track.getDistance());
        values.put(DatabaseContract.TrackEntry.COLUMN_NAME_SPEED, track.getSpeed());
        values.put(DatabaseContract.TrackEntry.COLUMN_NAME_TYPE, track.getType());
        values.put(DatabaseContract.TrackEntry.COLUMN_NAME_DATE, track.getDate().getTime() / 1000);
        values.put(DatabaseContract.TrackEntry.COLUMN_NAME_DURATION, track.getDuration());
        values.put(DatabaseContract.TrackEntry.COLUMN_NAME_IMAGE, track.getImage());

        long trackId = db.insert(DatabaseContract.TrackEntry.TABLE_NAME, null, values);

        // Insert points of the track
        for (Point p : track.getPoints()) {
            ContentValues pointValues = new ContentValues();
            pointValues.put(DatabaseContract.PointEntry.COLUMN_NAME_LAT, p.getLat());
            pointValues.put(DatabaseContract.PointEntry.COLUMN_NAME_LNG, p.getLng());
            pointValues.put(DatabaseContract.PointEntry.COLUMN_NAME_ELEV, p.getElev());
            pointValues.put(DatabaseContract.PointEntry.COLUMN_NAME_TIME, p.getTime());
            pointValues.put(DatabaseContract.PointEntry.COLUMN_NAME_DURATION, p.getDuration());
            pointValues.put(DatabaseContract.PointEntry.COLUMN_NAME_TRACK_ID, trackId);

            db.insert(DatabaseContract.PointEntry.TABLE_NAME, null, pointValues);
        }

        return trackId;
    }

    /* POINT */
    private ArrayList<Point> getPoints(int trackId, SQLiteDatabase db) {
        ArrayList<Point> points = new ArrayList<>();

        String[] projection = new String[] { "*" };
        String where = String.format(Locale.getDefault(),"%s = ?",DatabaseContract.PointEntry.COLUMN_NAME_TRACK_ID);
        String whereArgs[] = new String[]{ trackId + "" };
        String groupBy = null;
        String having = null;
        String order = DatabaseContract.PointEntry.COLUMN_NAME_TIME + " ASC";

        Cursor cursor = db.query(DatabaseContract.PointEntry.TABLE_NAME, projection,
                where, whereArgs, groupBy, having, order);
        while(cursor.moveToNext()) {
            double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.PointEntry.COLUMN_NAME_LAT));
            double lng = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.PointEntry.COLUMN_NAME_LNG));
            int elev = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.PointEntry.COLUMN_NAME_ELEV));
            long time = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.PointEntry.COLUMN_NAME_TIME));
            int duration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.PointEntry.COLUMN_NAME_DURATION));

            points.add(new Point(lat, lng, elev, time, duration));
        }
        cursor.close();

        return points;
    }

    /* CONTACT */
    public ArrayList<Contact> getContacts() {
        ArrayList<Contact> contacts = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = new String[] { "*" };
        String where = null;
        String whereArgs[] = null;
        String groupBy = null;
        String having = null;
        String order = DatabaseContract.ContactEntry.COLUMN_NAME_NAME + " ASC";

        Cursor cursor = db.query(DatabaseContract.ContactEntry.TABLE_NAME, projection,
                where, whereArgs, groupBy, having, order);
        while(cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ContactEntry.COLUMN_NAME_NAME));
            String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ContactEntry.COLUMN_NAME_NUMBER));

            contacts.add(new Contact(name, phoneNumber));
        }
        cursor.close();

        return contacts;
    }

    /**
     * Find contact by its phone number
     * @param phoneNumber number of contact to find
     * @param db must be set to getReadableDatabase()
     * @return the contact or null if none found
     */
    private Contact findContactByPhoneNumber(String phoneNumber, SQLiteDatabase db) {
        Contact contact = null;

        String[] projection = new String[] { "*" };
        String where = String.format("%s = ?", DatabaseContract.ContactEntry.COLUMN_NAME_NUMBER);
        String whereArgs[] = {phoneNumber};
        String groupBy = null;
        String having = null;
        String order = null;

        Cursor cursor = db.query(DatabaseContract.ContactEntry.TABLE_NAME, projection,
                where, whereArgs, groupBy, having, order);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ContactEntry.COLUMN_NAME_NAME));
            contact = new Contact(name, phoneNumber);
        }
        cursor.close();

        return contact;
    }

    /**
     * Insert new contact in db
     * @param contact the contact must contain name and phone number
     * @return the id of inserted contact
     */
    public long insertContact(Contact contact) {
        // Check if contact already exists
        if (findContactByPhoneNumber(contact.getPhoneNumber(), this.getReadableDatabase()) != null)
            return 0;

        SQLiteDatabase db = this.getWritableDatabase();

        // Insert contact
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ContactEntry.COLUMN_NAME_NAME, contact.getName());
        values.put(DatabaseContract.ContactEntry.COLUMN_NAME_NUMBER, contact.getPhoneNumber());

        return db.insert(DatabaseContract.ContactEntry.TABLE_NAME, null, values);
    }

    /**
     * Delete contact by its phone number
     * @param phone number of the contact
     * @return the number of rows affected by the suppression
     */
    public int deleteContactByPhoneNumber(String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = DatabaseContract.ContactEntry.COLUMN_NAME_NUMBER + " LIKE ?";
        String[] whereArgs = { phone };
        return db.delete(DatabaseContract.ContactEntry.TABLE_NAME, where, whereArgs);
    }
}