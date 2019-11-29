package nl.tudelft.cs4160.trustchain_android.storage.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import nl.tudelft.cs4160.trustchain_android.storage.database.entity.DbPeer;

@Dao
public interface PeerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DbPeer peer);

    @Query("SELECT * FROM peer")
    LiveData<List<DbPeer>> getAllPeers();

    @Query("SELECT * FROM peer WHERE public_key = :publicKey")
    DbPeer getByPublicKey(String publicKey);

    @Query("DELETE FROM peer")
    void deleteAll();
}
