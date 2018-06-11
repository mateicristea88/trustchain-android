package nl.tudelft.cs4160.trustchain_android.network.peer;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.validateMockitoUsage;

/**
 * Created by Boning on 12/17/2017.
 */

public class PeerHandlerTest {
    private PeerHandler peerHandler;
    private ArrayList<Peer> originalIpList;
    private ArrayList<Peer> expectedIpList;
    InetSocketAddress randomInet = new InetSocketAddress(200);
    PublicKeyPair publicKeyPair = Key.createNewKeyPair().getPublicKeyPair();
    private String randomHashIdName = "randomHashIdName";

    @Before
    public void initialization() {
        Peer peer1 = new Peer(randomInet,Key.createNewKeyPair().getPublicKeyPair(),"peer1");
        Peer peer2 = new Peer(randomInet,Key.createNewKeyPair().getPublicKeyPair(), "peer2");
        Peer peer3 = new Peer(randomInet, Key.createNewKeyPair().getPublicKeyPair(), "peer3");

        originalIpList = new ArrayList<>();
        originalIpList.add(peer1);
        originalIpList.add(peer2);
        originalIpList.add(peer3);
        originalIpList.add(peer1);

        expectedIpList = new ArrayList<>();
        expectedIpList.add(peer1);
        expectedIpList.add(peer2);
        expectedIpList.add(peer3);

        peerHandler = new PeerHandler(publicKeyPair,randomHashIdName);
        peerHandler.setPeerList(originalIpList);
    }

    @Test
    public void removeDuplicatesTest() {
        peerHandler.removeDuplicates();
        ArrayList<Peer> newIPPeerList = peerHandler.getPeerList();
        boolean failed = false;

        for (Peer peer : newIPPeerList) {
            if (!expectedIpList.remove(peer)) {
                failed = true;
                break;
            }
        }
        if (expectedIpList.size() != 0) failed = false;
        assertFalse(failed);
    }

    @Test
    public void peerExistsInListTest() {
        Peer peer4 = new Peer(randomInet,Key.createNewKeyPair().getPublicKeyPair(),"peer4");
        assertTrue(peerHandler.peerExistsInList(originalIpList.get(0)));
        assertFalse(peerHandler.peerExistsInList(peer4));
    }

    @Test
    public void testCertainMethods() {
        peerHandler = new PeerHandler(Key.createNewKeyPair().getPublicKeyPair(),"name");
        Peer peer = new Peer(randomInet,Key.createNewKeyPair().getPublicKeyPair(),"peer");
        peerHandler.add(peer);
        assertEquals(1, peerHandler.size());
        peerHandler.remove(peer);
        assertEquals(0, peerHandler.size());
    }

    @Test
    public void testWanVoteNull() {
        assertEquals(peerHandler.getWanVote().getAddress(), null);
    }

    @Test
    public void testSetPeerlist() {
        assertEquals(expectedIpList.size(),peerHandler.getPeerList().size());
    }

    @Test
    public void testPeerlistAdd() {
        int size = peerHandler.getPeerList().size();
        Peer peer2 = new Peer(randomInet,Key.createNewKeyPair().getPublicKeyPair(), "peer2");
        peerHandler.add(peer2);
        assertEquals(peerHandler.getPeerList().size(), size + 1);
    }


    @Test
    public void testPeerListGetPublicKeyPair() {
        assertEquals(peerHandler.getPublicKeyPair(), publicKeyPair);
    }

    @Test
    public void testRemoveAPeers() {
        int size = peerHandler.getPeerList().size();
        peerHandler.remove(originalIpList.get(0));
        assertEquals(peerHandler.getPeerList().size(), size - 1);
    }

    @Test
    public void testExistsIn() {
        assertTrue(peerHandler.peerExistsInList(originalIpList.get(0)));
    }

    @Test
    public void testNotExistsIn() {
        assertFalse(peerHandler.peerExistsInList(new Peer(new InetSocketAddress(202), Key.createNewKeyPair().getPublicKeyPair(), "peerA??")));
    }

    @Test
    public void testAdd() {
        Peer randomPeer = new Peer(new InetSocketAddress(202), Key.createNewKeyPair().getPublicKeyPair(), "peerA??");
        peerHandler.add(randomPeer);
        assertTrue(peerHandler.peerExistsInList(randomPeer));
    }

    @Test
    public void testAddPeerAlreadyInList() {
        int size = peerHandler.getPeerList().size();
        peerHandler.addPeer(originalIpList.get(0).getAddress(), originalIpList.get(0).getPublicKeyPair(), originalIpList.get(0).getName());
        assertEquals(peerHandler.getPeerList().size(), size);
    }

    @Test
    public void testEligiblePeer() {
        List<Peer> excludePeers = new ArrayList<>();
        excludePeers.add(originalIpList.get(0));
        Peer peer = peerHandler.getEligiblePeer(excludePeers);
        assertNotEquals(peer.toString(), originalIpList.get(0).toString());
    }

    @After
    public void resetMocks() {
        validateMockitoUsage();
    }
}
