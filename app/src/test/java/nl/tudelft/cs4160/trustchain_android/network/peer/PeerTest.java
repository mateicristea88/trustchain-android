package nl.tudelft.cs4160.trustchain_android.network.peer;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.main.OverviewConnectionsActivity;

public class PeerTest extends TestCase {
    String id1;
    String id2;
    InetSocketAddress address;
    PublicKeyPair publicKeyPair;

    @Before
    public void setUp() {
        address = new InetSocketAddress(11);
        publicKeyPair = Key.createNewKeyPair().getPublicKeyPair();
        id1 = "123";
        id2 = "24";
    }

    @Test
    public void testEqual() {
        Peer peer1 = new Peer(address, publicKeyPair, id1);
        Peer peer2 = new Peer(address, publicKeyPair, id1);
        assertTrue(peer1.equals(peer2));
    }

    @Test
    public void testNotEqual() {
        Peer peer1 = new Peer(address, publicKeyPair, id1);
        Peer peer2 = new Peer(address, publicKeyPair, id2);
        assertFalse(peer1.equals(peer2));
    }

    @Test
    public void testCreationTime(){
        Peer peer1 = new Peer(address, publicKeyPair, id1);
        try {
            TimeUnit.SECONDS.sleep(1);
        }catch (Exception e){
            e.printStackTrace();
        }
        Peer peer2 = new Peer(address, publicKeyPair, id2);
        assertFalse(peer1.getCreationTime() == peer2.getCreationTime());
        assertTrue(peer1.getCreationTime() < System.currentTimeMillis());
    }



    @Test
    public void testHasReceivedData(){
        Peer peer1 = new Peer(address, publicKeyPair, "firstPEER");
        ByteBuffer buf = ByteBuffer.allocate(100);
        assertFalse(peer1.isReceivedFrom());
        peer1.receivedData();
        assertTrue(peer1.isReceivedFrom());
    }

    @Test
    public void testToString(){
        Peer peer1 = new Peer(address, publicKeyPair, "firstPEER");
        peer1.setConnectionType(1);
        assertEquals("Peer{" + "address=" + address + ", peerId='" + "firstPEER" + '\'' +
                        ", hasReceivedData=" + false + ", connectionType=" + 1 + '}'
                ,peer1.toString());
    }

    @Test
    public void testChangeParameters() {
        Peer peer1 = new Peer(address, publicKeyPair,"firstPEER");
        peer1.setConnectionType(1);
        assertEquals(1, peer1.getConnectionType());
        peer1.setName("PEER");
        assertEquals("PEER", peer1.getName());
        peer1.setAddress(new InetSocketAddress("host", 11));
        assertEquals(new InetSocketAddress("host", 11), peer1.getAddress());
        assertNull(peer1.getAddress());
    }

    @Test
    public void testSendData(){
        Peer peer1 = new Peer(address, publicKeyPair, "firstPEER");
        assertTrue(peer1.isAlive());
        long lastSendTime = peer1.getLastSentTime();
        peer1.sentData();
        assertNotSame(lastSendTime, peer1.getLastSentTime());
        assertTrue(peer1.isAlive());
    }

    @Test
    public void testReceiveData(){
        Peer peer1 = new Peer(address, publicKeyPair, "firstPEER");
        ByteBuffer buf = ByteBuffer.allocate(100);
        long lastReceivedTime = peer1.getLastReceivedTime();
        peer1.receivedData();
        assertNotSame(lastReceivedTime, peer1.getLastReceivedTime());
    }

    @Test
    public void testHashCode() {
        Peer peer1 = new Peer(address, publicKeyPair, "firstPEER");
        assertEquals(132867431, peer1.hashCode());
    }

    @Test
    public void testIsBootstrap() {
        Peer bootstrap = new Peer(new InetSocketAddress(OverviewConnectionsActivity.CONNECTABLE_ADDRESS,OverviewConnectionsActivity.DEFAULT_PORT), null, null);
        assertTrue(bootstrap.isBootstrap());
    }
}
