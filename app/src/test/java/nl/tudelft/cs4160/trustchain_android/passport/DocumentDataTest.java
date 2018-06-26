package nl.tudelft.cs4160.trustchain_android.passport;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import static nl.tudelft.cs4160.trustchain_android.passport.DocumentData.CREATOR;
import static org.junit.Assert.*;

public class DocumentDataTest {
    final String documentNumber = "123456789";
    final String expiryDate = "260622";
    final String dateOfBirth = "260690";
    DocumentData data;

    @Before
    public void init() {
        data = new DocumentData(documentNumber, expiryDate, dateOfBirth);
    }

    @Test
    public void testGetExpiryDate() {
        assertEquals(expiryDate, data.getExpiryDate());
    }

    @Test
    public void testSetExpiryDate() {
        String newExpiryDate = "010132";
        data.setExpiryDate(newExpiryDate);
        assertEquals(newExpiryDate,data.getExpiryDate());
    }

    @Test
    public void testGetDocumentNumber() {
        assertEquals(documentNumber, data.getDocumentNumber());
    }

    @Test
    public void testSetDocumentNumber() {
        String newDocumentNumber = "ABCDEFGHI";
        data.setDocumentNumber(newDocumentNumber);
        assertEquals(newDocumentNumber, data.getDocumentNumber());
    }

    @Test
    public void testGetDateOfBirth() {
        assertEquals(dateOfBirth, data.getDateOfBirth());
    }

    @Test
    public void testSetDateOfBirth() {
        String newDateOfBirth = "010180";
        data.setDateOfBirth(newDateOfBirth);
        assertEquals(newDateOfBirth, data.getDateOfBirth());
    }

    @Test
    public void testIsValid() {
        assertTrue(data.isValid());
    }

    @Test
    public void testIsValidNull1() {
        data.setDateOfBirth(null);
        assertFalse(data.isValid());
    }

    @Test
    public void testIsValidNull2() {
        data.setDocumentNumber(null);
        assertFalse(data.isValid());
    }

    @Test
    public void testIsValidNull3() {
        data.setExpiryDate(null);
        assertFalse(data.isValid());
    }

    @Test
    public void testToString() {
        String expected = "Exp date: " + expiryDate + ", Date of birth: " + dateOfBirth + ", Doc num: " + documentNumber + ", Valid: true";
        assertEquals(expected, data.toString());
    }

    @Test
    public void testToStringNull() {
        data.setDocumentNumber(null);
        String expected = "Exp date: " + expiryDate + ", Date of birth: " + dateOfBirth + ", Doc num: null" + ", Valid: false";
        assertEquals(expected, data.toString());
    }

    @Test
    public void testDescribeContents() {
        assertEquals(0, data.describeContents());
    }

    @Test
    public void testEmptyConstructor() {
        DocumentData d = new DocumentData();
        assertNull(d.getDateOfBirth());
        assertNull(d.getDocumentNumber());
        assertNull(d.getExpiryDate());
    }

    @Test
    public void testNewArray() {
        int size = 8;
        DocumentData[] expected = new DocumentData[size];
        assertArrayEquals(expected, CREATOR.newArray(8));
    }
}