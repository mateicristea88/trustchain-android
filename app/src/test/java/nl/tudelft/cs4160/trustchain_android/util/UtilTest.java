package nl.tudelft.cs4160.trustchain_android.util;

import android.content.Context;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static nl.tudelft.cs4160.trustchain_android.util.Util.copyFile;
import static nl.tudelft.cs4160.trustchain_android.util.Util.ellipsize;
import static nl.tudelft.cs4160.trustchain_android.util.Util.readFile;
import static nl.tudelft.cs4160.trustchain_android.util.Util.writeToFile;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by meijer on 10-11-17.
 */
@RunWith(JUnit4.class)
public class UtilTest {
    String longString;
    String longStringFirst6;
    String shortString;
    String shortStringFirst6;
    final String testAssets = "src/test/test_assets/";
    final File testInput = new File(testAssets + "testInput.txt");

    @Mock
    Context contextMock;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void initialization() throws FileNotFoundException {
        longString = "0002SDVnnuisdvbhhduifvbsdiuvndskjvcxnvjkdsvusidvbsdjovcnvjkcxv0002SDVnnuisdvbhhduifvbsdiuvndskjvcxnvjkdsvusidvbsdjovcnvjkcxv0002SDVnnuisdvbhhduifvbsdiuvndskjvcxnvjkdsvusidvbsdjovcnvjkcxv";
        longStringFirst6 = "0(..)v";
        shortString = "viudnfvidufdvn84938enfivdnidvn";
        shortStringFirst6 = "v(..)n";
        when(contextMock.getFileStreamPath(anyString())).thenAnswer(i -> new File(testAssets + i.getArgument(0)));
        when(contextMock.openFileInput(anyString())).thenAnswer(i -> new FileInputStream(testAssets + i.getArgument(0)));
        when(contextMock.openFileOutput(anyString(), anyInt())).thenAnswer(i -> new FileOutputStream(testAssets + i.getArgument(0)));
    }

    @Test
    public void ellipsizeTest() throws Exception {
        String input = "12345678910";
        String expected = "1(..)0";
        Assert.assertEquals(expected,ellipsize(input,5));
    }

    @Test
    public void ellipsizeTest2() throws Exception {
        String input = "12345678910";
        String expected = "12(..)10";
        Assert.assertEquals(expected,ellipsize(input,8));
    }

    @Test
    public void ellipsizeTest3() throws Exception {
        String input = "12345678910";
        Assert.assertEquals(input,ellipsize(input,11));
    }

    @Test
    public void ellipsizeTest4() throws Exception {
        String input = "12345678910";
        String expected = "1(..)0";
        Assert.assertEquals(expected,ellipsize(input,6));
    }

    @Test
    public void ellipsizeTest5() throws Exception {
        String input = "12";
        String expected = "12";
        Assert.assertEquals(expected,ellipsize(input,5));
    }

    @Test
    public void ellipsizeTestZero() throws Exception {
        String input = "12";
        String expected = "12";
        Assert.assertEquals(expected, ellipsize(input, 0));
    }

    @Test
    public void testEllipsizeShort() {
        String a = Util.ellipsize(shortString, 6);
        assertEquals(a, shortStringFirst6);
    }

    @Test
    public void testEllipsizeLong() {
        assertEquals(Util.ellipsize(longString, 6), longStringFirst6);
    }

    @Test
    public void testEllipsizeFull() {
        assertEquals(Util.ellipsize(shortString, 1000), shortString);
    }


    @Test
    public void testReadableSize() {
        assertEquals("1 KB", Util.readableSize(1024));
        assertEquals("100 B", Util.readableSize(100));
        assertEquals("1 MB", Util.readableSize(1024*1024));
    }


    @Test
    public void testTimeToString() {
        assertEquals("0s", Util.timeToString(100));
        assertEquals("0s", Util.timeToString(999));
        assertEquals("1s", Util.timeToString(1000));
        assertEquals("2s", Util.timeToString(2000));
        assertEquals("59s", Util.timeToString(1000*60-1));
        assertEquals("1m0s", Util.timeToString(1000*60));
        assertEquals("1m1s", Util.timeToString(1000*60+1000));
        assertEquals("1h0m", Util.timeToString(1000*60*60));
    }

    @Test
    public void testCopyFile() throws IOException {
        InputStream is = new FileInputStream(testAssets + "testInput.txt");
        File f = new File(testAssets + "testCopyFile");

        assertTrue(copyFile(is,f));
        assertTrue(f.exists());
        assertTrue(IOUtils.contentEquals(new FileInputStream(testAssets + "testInput.txt"),
                new FileInputStream(f.getPath())));
        f.delete();
    }

    @Test
    public void testCopyFileNull1() throws IOException {
        InputStream is = null;
        File f = null;

        assertFalse(copyFile(is,f));
    }

    @Test
    public void testCopyFileNull2() throws IOException {
        InputStream is = null;
        File f = new File(testAssets + "testCopyFile");

        assertFalse(copyFile(is,f));
        assertFalse(f.exists());
        f.delete();
    }

    @Test
    public void testCopyFileNull3() throws IOException {
        InputStream is = new FileInputStream(testAssets + "testInput.txt");
        File f = null;

        assertFalse(copyFile(is,f));
    }


    @Test
    public void testReadFile() {
        String fileName = "testInput.txt";
        String expected = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque sem sem, sollicitudin et magna sed, porta auctor dolor. Nullam aliquet nibh elit, accumsan commodo ligula maximus suscipit. Cras in orci sed lectus laoreet mattis a nec felis. Donec vitae fringilla dolor. Nunc mollis maximus faucibus. Quisque sagittis quis nisi et luctus. Phasellus sagittis, diam quis placerat malesuada, enim lacus sodales risus, nec placerat nunc eros eget metus. Donec interdum elementum ante ac scelerisque. Mauris ut tempus mi. Morbi eget purus dapibus, tempus erat sed, tincidunt lectus. Donec ut nisl ac velit ullamcorper egestas eget eu odio.\n";
        assertEquals(expected, readFile(contextMock,fileName));
    }

    @Test
    public void testReadFileNull() {
        Context context = null;
        String fileName = null;
        String expected = null;
        assertEquals(expected, readFile(context,fileName));
    }

    @Test
    public void testReadFileNull2() {
        String fileName = null;
        String expected = null;
        assertEquals(expected, readFile(contextMock,fileName));
    }

    @Test
    public void testReadFileNull3() {
        Context context = null;
        String fileName = testAssets + "testInput.txt";
        String expected = null;
        assertEquals(expected, readFile(context,fileName));
    }

    @Test
    public void testWriteToFile() throws IOException {
        String fileName = "testWriteFile.txt";
        File f = new File(testAssets + fileName);
        String data = "Lorum ipsum";

        assertTrue(writeToFile(contextMock,fileName,data));
        assertTrue(f.exists());
        assertTrue(IOUtils.contentEquals(new FileInputStream(testAssets + fileName),
                new ByteArrayInputStream(data.getBytes())));
        f.delete();
    }

    @Test
    public void testWriteToFileNull1() throws IOException {
        Context context = null;
        String fileName = null;
        File f = new File(testAssets + fileName);
        String data = null;

        assertFalse(writeToFile(context,fileName,data));
        assertFalse(new File(testAssets + fileName).exists());
        f.delete();
    }

    @Test
    public void testWriteToFileNull2() throws IOException {
        Context context = null;
        String fileName = "testWriteFile.txt";
        File f = new File(testAssets + fileName);
        String data = "Lorum ipsum";

        assertFalse(writeToFile(context,fileName,data));
        assertFalse(new File(testAssets + fileName).exists());
        f.delete();
    }

    @Test
    public void testWriteToFileNull3() throws IOException {
        String fileName = null;
        File f = new File(testAssets + fileName);
        String data = "Lorum ipsum";

        assertFalse(writeToFile(contextMock,fileName,data));
        assertFalse(new File(testAssets + fileName).exists());
        f.delete();
    }

    @Test
    public void testWriteToFileNull4() throws IOException {
        String fileName = "testWriteFile.txt";
        File f = new File(testAssets + fileName);
        String data = null;

        assertFalse(writeToFile(contextMock,fileName,data));
        assertFalse(new File(testAssets + fileName).exists());
        f.delete();
    }
}