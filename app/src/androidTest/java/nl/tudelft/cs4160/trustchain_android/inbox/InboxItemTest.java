package nl.tudelft.cs4160.trustchain_android.inbox;


import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.libsodium.jni.NaCl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.ui.userconfiguration.UserConfigurationActivity;
import nl.tudelft.cs4160.trustchain_android.peer.Peer;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class InboxItemTest {

    private String userName;
    private ArrayList<Integer> halfBlockSequenceNumbers = new ArrayList<>();
    private String address;
    private PublicKeyPair publicKey;
    private int port;
    private InboxItem ii;

    @Rule
    public ActivityTestRule<UserConfigurationActivity> mActivityRule = new ActivityTestRule<>(
            UserConfigurationActivity.class,
            true,
            false);

    @Before
    public void setUp() {
        NaCl.sodium();
        userName = "userName";
        halfBlockSequenceNumbers.add(1);
        halfBlockSequenceNumbers.add(2);
        halfBlockSequenceNumbers.add(3);
        address = "127.0.0.1";
        publicKey = new DualSecret().getPublicKeyPair();
        port = 123;
        Peer peer = new Peer(new InetSocketAddress(address, port),publicKey,userName);
        ii = new InboxItem(peer, halfBlockSequenceNumbers);
    }

    @Test
    public void testConstructorPublicKey() {
        assertEquals(ii.getPeer().getPublicKeyPair(), publicKey);
    }

    @Test
    public void testEquals() {
        Peer peer = new Peer(new InetSocketAddress(address,port),publicKey,userName);
        InboxItem ii2 = new InboxItem(peer, halfBlockSequenceNumbers);
        assertEquals(ii, ii2);
    }

    @Test
    public void testEqualsFalseUserName() {
        Peer peer = new Peer(new InetSocketAddress(address,port),publicKey,userName + "r");
        InboxItem ii2 = new InboxItem(peer, halfBlockSequenceNumbers);
        assertTrue(ii.equals(ii2));
    }

    @Test
    public void testEqualsFalsePublicKey() {
        Peer peer = new Peer(new InetSocketAddress(address,port), Key.createNewKeyPair().getPublicKeyPair(),userName);
        InboxItem ii2 = new InboxItem(peer, halfBlockSequenceNumbers);
        assertFalse(ii.equals(ii2));
    }

    @Test
    public void testSetPublicKey() {
        PublicKeyPair pubKeyPair = new DualSecret().getPublicKeyPair();
        ii.getPeer().setPublicKeyPair(pubKeyPair);
        assertTrue(Arrays.equals(ii.getPeer().getPublicKeyPair().toBytes(), pubKeyPair.toBytes()));
    }

}
