package ch.mse.biketracks;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;

import ch.mse.biketracks.database.DatabaseHelper;
import ch.mse.biketracks.models.Contact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test database operations
 */
@RunWith(RobolectricTestRunner.class)
public class SQLiteTest {
    private DatabaseHelper dbHelper;

    private
    Context context;

    @Before
    public void init() {
        context = RuntimeEnvironment.application;
        dbHelper = DatabaseHelper.getInstance(context);
    }

    @After
    public void close() {
        dbHelper.clearDB();
        dbHelper.close();
    }

    @Test
    public void saveContact_isCorrect() throws Exception {
        Contact contact = new Contact("Paul", "+41791234567");
        long id = dbHelper.insertContact(contact);
        assertEquals(1, id);
    }

    @Test
    public void getContacts_isCorrect() throws  Exception {
        String name1 = "Paul", name2 = "John";
        String phone1 = "+41791234567", phone2 = "+41791234568";
        Contact contact1 = new Contact(name1, phone1);
        Contact contact2 = new Contact(name2, phone2);
        long id1 = dbHelper.insertContact(contact1);
        long id2 = dbHelper.insertContact(contact2);

        ArrayList<Contact> contacts = dbHelper.getContacts();

        assertTrue(contacts.size() == 2);
        assertEquals(name1, contacts.get(1).getName());
        assertEquals(phone1, contacts.get(1).getPhoneNumber());
        assertEquals(name2, contacts.get(0).getName());
        assertEquals(phone2, contacts.get(0).getPhoneNumber());
    }
}
