package nl.tudelft.cs4160.trustchain_android.inbox;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.peer.Peer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

/**
 * Note that not all tests have to do with crypto can be run here. For those please use the AndroidTest.
 *
 */
public class InboxItemTest {

    private String userName;
    private ArrayList<Integer> halfBlockSequenceNumbers = new ArrayList<>();
    private String address;
    @Mock
    private PublicKeyPair publicKey;
    private byte[] pkpBytes = {0x01,0x02,0x03};

    private int port;
    private InboxItem ii;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        userName = "userName";
        halfBlockSequenceNumbers.add(1);
        halfBlockSequenceNumbers.add(2);
        halfBlockSequenceNumbers.add(3);
        when(publicKey.toBytes()).thenReturn(pkpBytes);
        address = "127.0.0.1";
        port = 123;
        Peer peer = new Peer(new InetSocketAddress(address, port),publicKey,userName);
        ii = new InboxItem(peer, halfBlockSequenceNumbers);
    }


    @Test
    public void testConstructorUserName() {
        assertEquals(ii.getPeer().getName(), userName);
    }

    @Test
    public void testConstructorBlocks() {
        assertEquals(ii.getHalfBlocks(), halfBlockSequenceNumbers);
    }

    @Test
    public void testConstructorAddress() {
        assertEquals(ii.getPeer().getIpAddress().getHostAddress(), address);
    }


    @Test
    public void testAddHalfblock() {
        int seqBlock = 12;
        ii.addHalfBlocks(seqBlock);
        halfBlockSequenceNumbers.add(seqBlock);
        assertEquals(ii.getHalfBlocks(), halfBlockSequenceNumbers);
    }

    @Test
    public void testEqualsFalseAddress() {
        Peer peer = new Peer(new InetSocketAddress("1.1.1.1",port),publicKey,userName);
        InboxItem ii2 = new InboxItem(peer, halfBlockSequenceNumbers);
        assertFalse(ii.equals(ii2));
    }

    @Test
    public void testEqualsFalsePort() {
        Peer peer = new Peer(new InetSocketAddress(address,port + 12),publicKey,userName);
        InboxItem ii2 = new InboxItem(peer, halfBlockSequenceNumbers);
        assertFalse(ii.equals(ii2));
    }

    @Test
    public void testEqualsFalseHalfBlockSequenceNumbers() {
        Peer peer = new Peer(new InetSocketAddress(address,port),publicKey,userName);
        ArrayList<Integer> halfBlockSequenceNumbers2 = new ArrayList<>();
        InboxItem ii2 = new InboxItem(peer, halfBlockSequenceNumbers2);
        assertFalse(ii.equals(ii2));
    }

    @Test
    public void testSetUserName() {
        String newUserName = "random";
        ii.getPeer().setName(newUserName);
        assertEquals(ii.getPeer().getName(), newUserName);
    }

    @Test
    public void testSetBlocks() {
        ArrayList<Integer> halfBlockSequenceNumbers2 = new ArrayList<>();
        halfBlockSequenceNumbers2.add(4);
        ii.setHalfBlocks(halfBlockSequenceNumbers2);
        assertEquals(ii.getHalfBlocks(), halfBlockSequenceNumbers2);
    }

    @Test
    public void testGetAmountUnread() {
        assertEquals(ii.getAmountUnread(), halfBlockSequenceNumbers.size());
    }
    @Test
    public void testGetAmountUnreadNull() {
        Peer peer = new Peer(new InetSocketAddress(address,port), publicKey,userName);
        InboxItem ii2 = new InboxItem(peer, null);
        assertEquals(ii2.getAmountUnread(),0);
    }

}
