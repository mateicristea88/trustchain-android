package nl.tudelft.cs4160.trustchain_android.storage.repository;

import android.util.Base64;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.List;

import javax.inject.Inject;

import nl.tudelft.cs4160.trustchain_android.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.storage.database.converter.PeerConverter;
import nl.tudelft.cs4160.trustchain_android.storage.database.dao.PeerDao;
import nl.tudelft.cs4160.trustchain_android.storage.database.entity.DbPeer;

import static nl.tudelft.cs4160.trustchain_android.util.LiveDataTransformations.mapAsync;

public class PeerRepository {
    private PeerDao peerDao;

    @Inject
    public PeerRepository(PeerDao peerDao) {
        this.peerDao = peerDao;
    }

    public void insertOrUpdate(Peer peer) {
        peerDao.insert(PeerConverter.toDbPeer(peer));
    }

    public LiveData<List<Peer>> getAllPeers() {
        return mapAsync(peerDao.getAllPeers(), PeerConverter::fromDbPeers);
    }

    @Nullable
    public Peer getByPublicKey(byte[] publicKey) {
        DbPeer dbPeer = peerDao.getByPublicKey(Base64.encodeToString(publicKey, Base64.DEFAULT));
        return dbPeer != null ? PeerConverter.fromDbPeer(dbPeer) : null;
    }

    public void deleteAllPeers() {
        peerDao.deleteAll();
    }
}
