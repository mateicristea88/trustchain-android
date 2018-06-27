package nl.tudelft.cs4160.trustchain_android.peer;

import com.google.protobuf.ByteString;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.main.OverviewConnectionsActivity;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

import static org.junit.Assert.*;

public class PeerTest {
    Peer peer;
    MessageProto.Peer protoPeer;

    InetSocketAddress address = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 1873);
    String name = "peer1";
    int connectionType = 1;

    public PeerTest() throws UnknownHostException {
    }


    @Before
    public void init() {
        protoPeer = MessageProto.Peer.newBuilder()
                .setPublicKey(ByteString.EMPTY)
                .setAddress(ByteString.copyFrom(address.getAddress().getAddress()))
                .setPort(address.getPort())
                .setName(name)
                .build();
        peer = new Peer(address, null, name);
    }

    @Test
    public void testSentData() {
        assertEquals(-1, peer.getLastSentTime());
        peer.sentData();
        assertTrue(peer.getLastSentTime()<= System.currentTimeMillis());
    }

    @Test
    public void testReceivedData() {
        assertEquals(-1, peer.getLastReceivedTime());
        peer.receivedData();
        assertTrue(peer.getLastReceivedTime()<= System.currentTimeMillis());
    }

    @Test
    public void testIsBootstrap() {
        assertFalse(peer.isBootstrap());
    }

    @Test
    public void testIsBootstrap2() throws UnknownHostException {
        peer.setAddress(new InetSocketAddress(InetAddress.getByName(OverviewConnectionsActivity.CONNECTABLE_ADDRESS), 1873));
        assertTrue(peer.isBootstrap());
    }

    @Test
    public void testIsReceivedFrom() {
        assertFalse(peer.isReceivedFrom());
    }

    @Test
    public void testIsReceivedFrom2() {
        peer.receivedData();
        assertTrue(peer.isReceivedFrom());
    }

    @Test
    public void testIsSentTo() {
        assertFalse(peer.isSentTo());
    }

    @Test
    public void testIsSentTo2() {
        peer.sentData();
        assertTrue(peer.isSentTo());
    }

    @Test
    public void testCanBeRemoved() {
        assertFalse(peer.canBeRemoved());
    }

    @Test
    public void testCanBeRemoved2() {
        peer.receivedData();
        assertFalse(peer.canBeRemoved());
    }

    @Test
    public void testCanBeRemoved3() {
        peer.sentData();
        assertFalse(peer.canBeRemoved());
    }

    @Test
    public void testCanBeRemoved4() {
        peer.lastReceiveTime = System.currentTimeMillis() - Peer.REMOVE_TIMEOUT - 10;
        assertTrue(peer.canBeRemoved());
    }

    @Test
    public void testCanBeRemoved5() {
        peer.lastSentTime = System.currentTimeMillis() - Peer.REMOVE_TIMEOUT - 10;
        peer.creationTime = peer.creationTime - Peer.REMOVE_TIMEOUT - 10;
        assertTrue(peer.canBeRemoved());
    }

    @Test
    public void testCanBeRemovedBootstrap() throws UnknownHostException {
        peer.setAddress(new InetSocketAddress(InetAddress.getByName(OverviewConnectionsActivity.CONNECTABLE_ADDRESS), 1873));
        assertFalse(peer.canBeRemoved());
    }

    @Test
    public void testIsAlive() {
        peer.lastReceiveTime = System.currentTimeMillis() - Peer.TIMEOUT - 5;
        assertFalse(peer.isAlive());
    }

    @Test
    public void testIsAlive2() {
        peer.lastSentTime = System.currentTimeMillis() - Peer.TIMEOUT - 5;
        assertTrue(peer.isAlive());
    }

    @Test
    public void testIsAlive3() {
        peer.receivedData();
        assertTrue(peer.isAlive());
    }

    @Test
    public void testIsAlive4() {
        peer.lastReceiveTime = System.currentTimeMillis() - Peer.TIMEOUT - 5;
        peer.sentData();
        assertFalse(peer.isAlive());
    }

    @Test
    public void testGetLastSentTime() {
        peer.lastSentTime = 2;
        assertEquals(2, peer.getLastSentTime());
    }

    @Test
    public void testGetLastReceivedTime() {
        peer.lastReceiveTime = 5;
        assertEquals(5, peer.getLastReceivedTime());
    }

    @Test
    public void testGetProtoPeer() {
        assertEquals(protoPeer, peer.getProtoPeer());
    }

    @Test
    public void testGetProtoPeer2() {
        peer.setName("peer fake");
        assertNotEquals(protoPeer, peer.getProtoPeer());
    }

    @Test
    public void testGetCreationTime() {
        assertTrue(peer.getCreationTime() > System.currentTimeMillis() - 600);
    }

    @Test
    public void testGetAddress() {
        assertEquals(address, peer.getAddress());
    }

    @Test
    public void testSetAddress() throws UnknownHostException {
        InetSocketAddress expected = new InetSocketAddress(InetAddress.getByName(OverviewConnectionsActivity.CONNECTABLE_ADDRESS), 1873);
        peer.setAddress(expected);
        assertEquals(expected, peer.getAddress());
    }

    @Test
    public void testGetIpAddress() {
        assertEquals(address.getAddress(), peer.getIpAddress());
    }

    @Test
    public void testGetPort() {
        assertEquals(address.getPort(), peer.getPort());
    }

    @Test
    public void testGetName() {
        assertEquals(name, peer.getName());
    }

    @Test
    public void testSetName() {
        peer.setName("peer55");
        assertEquals("peer55", peer.getName());
    }

    @Test
    public void testSetNameNull() {
        peer.setName("");
        assertEquals("", peer.getName());
    }

    @Test
    public void testGetConnectionType() {
        assertEquals(0,peer.getConnectionType());
    }

    @Test
    public void testSetConnectionType() {
        peer.setConnectionType(connectionType);
        assertEquals(connectionType, peer.getConnectionType());
    }
}