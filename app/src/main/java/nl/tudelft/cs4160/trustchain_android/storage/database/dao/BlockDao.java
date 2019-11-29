package nl.tudelft.cs4160.trustchain_android.storage.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import nl.tudelft.cs4160.trustchain_android.storage.database.entity.DbBlock;

/**
 * The data access object for accessing the block table in the database.
 */
@Dao
public interface BlockDao {
    /**
     * Inserts a new block into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(DbBlock block);

    /**
     * Updates an existing block with the matching primary key. It is used to alter the half
     * block to a complete block.
     */
    @Update
    void update(DbBlock block);

    /**
     * Retrieves the block associated with the given public key and sequence number.
     */
    @Query("SELECT * FROM block WHERE public_key = :publicKey AND sequence_number = :sequenceNumber LIMIT 1")
    DbBlock getBlock(String publicKey, int sequenceNumber);

    /**
     * Retrieves the block linked with the given block or null if a linked block is not found.
     */
    @Query("SELECT * FROM block WHERE " +
            "(public_key = :linkPublicKey AND sequence_number = :linkSequenceNumber) OR " +
            "(link_public_key = :publicKey AND link_sequence_number = :sequenceNumber)")
    DbBlock getLinkedBlock(String publicKey, int sequenceNumber, String linkPublicKey, int linkSequenceNumber);

    /**
     * Returns the block with the highest sequence number smaller than the given sequence number and
     * the same public key: the previous block in the chain. Sequence number is allowed to be another
     * value than seqNumber - 1.
     */
    @Query("SELECT * FROM block WHERE public_key = :publicKey AND sequence_number < :sequenceNumber " +
            "ORDER BY sequence_number DESC LIMIT 1")
    DbBlock getBlockBefore(String publicKey, int sequenceNumber);

    /**
     * Returns the block with the lowest sequence number greater than the given sequence number and
     * the same public key: the next block in the chain. Sequence number is allowed to be another
     * value than seqNumber + 1.
     */
    @Query("SELECT * FROM block WHERE public_key = :publicKey AND sequence_number > :sequenceNumber " +
            "ORDER BY sequence_number ASC LIMIT 1")
    DbBlock getBlockAfter(String publicKey, int sequenceNumber);

    /**
     * Returns the latest block in the database associated with the given public key.
     */
    @Query("SELECT * FROM block WHERE public_key = :publicKey ORDER BY sequence_number DESC LIMIT 1")
    DbBlock getLatestBlock(String publicKey);

    /**
     * Get the maximum sequence number in the database associated with the given public key.
     */
    @Query("SELECT sequence_number FROM block WHERE public_key = :publicKey " +
            "ORDER BY sequence_number DESC LIMIT 1")
    int getMaxSequenceNumber(String publicKey);

    /**
     * Returns the total number of blocks in the database.
     */
    @Query("SELECT COUNT(sequence_number) FROM block")
    int getBlockCount();

    /**
     * Retrieves all the blocks inserted in the database.
     */
    @Query("SELECT * FROM block")
    List<DbBlock> getAllBlocks();

    /**
     * Returns all blocks for a given public key.
     *
     * @param publicKey Blocks with this key are returned.
     * @param inLinked If true, also return blocks linked to this public key.
     */
    @Query("SELECT * FROM block WHERE public_key = :publicKey OR " +
            "(:inLinked AND link_public_key = :publicKey) ORDER BY sequence_number")
    List<DbBlock> getBlocks(String publicKey, boolean inLinked);

    /**
     * Searches the database for the blocks from the given sequence number to some limit and returns
     * a list of these blocks.
     */
    @Query("SELECT * FROM block WHERE public_key = :publicKey AND sequence_number >= :sequenceNumber " +
            "ORDER BY sequence_number LIMIT :limit")
    List<DbBlock> crawl(String publicKey, int sequenceNumber, int limit);
}
