package nl.tudelft.cs4160.trustchain_android.storage.repository;

import android.util.Base64;

import androidx.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;

import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.storage.database.converter.BlockConverter;
import nl.tudelft.cs4160.trustchain_android.storage.database.dao.BlockDao;
import nl.tudelft.cs4160.trustchain_android.storage.database.entity.DbBlock;

public class BlockRepository {
    private BlockDao blockDao;

    @Inject
    public BlockRepository(BlockDao blockDao) {
        this.blockDao = blockDao;
    }

    public void insertOrUpdate(MessageProto.TrustChainBlock block) {
        blockDao.insertOrUpdate(BlockConverter.toDbBlock(block));
    }

    public void update(MessageProto.TrustChainBlock block) {
        blockDao.update(BlockConverter.toDbBlock(block));
    }

    @Nullable
    public MessageProto.TrustChainBlock getBlock(byte[] publicKey, int sequenceNumber) {
        DbBlock dbBlock = blockDao.getBlock(encodeKey(publicKey), sequenceNumber);
        return (dbBlock != null) ? BlockConverter.fromDbBlock(dbBlock) : null;
    }

    @Nullable
    public MessageProto.TrustChainBlock getLinkedBlock(MessageProto.TrustChainBlock block) {
        DbBlock dbBlock = blockDao.getLinkedBlock(
                encodeKey(block.getPublicKey().toByteArray()),
                block.getSequenceNumber(),
                encodeKey(block.getLinkPublicKey().toByteArray()),
                block.getLinkSequenceNumber());
        return (dbBlock != null) ? BlockConverter.fromDbBlock(dbBlock) : null;
    }

    @Nullable
    public MessageProto.TrustChainBlock getBlockBefore(MessageProto.TrustChainBlock block) {
        DbBlock dbBlock = blockDao.getBlockBefore(
                encodeKey(block.getPublicKey().toByteArray()),
                block.getSequenceNumber());
        return (dbBlock != null) ? BlockConverter.fromDbBlock(dbBlock) : null;
    }

    @Nullable
    public MessageProto.TrustChainBlock getBlockAfter(MessageProto.TrustChainBlock block) {
        DbBlock dbBlock = blockDao.getBlockAfter(
                encodeKey(block.getPublicKey().toByteArray()),
                block.getSequenceNumber());
        return (dbBlock != null) ? BlockConverter.fromDbBlock(dbBlock) : null;
    }

    @Nullable
    public MessageProto.TrustChainBlock getLatestBlock(byte[] publicKey) {
        String encodedKey = Base64.encodeToString(publicKey, Base64.DEFAULT);
        DbBlock dbBlock = blockDao.getLatestBlock(encodedKey);
        return (dbBlock != null) ? BlockConverter.fromDbBlock(dbBlock) : null;
    }

    public int getMaxSequenceNumber(byte[] publicKey) {
        return blockDao.getMaxSequenceNumber(encodeKey(publicKey));
    }

    public int getBlockCount() {
        return blockDao.getBlockCount();
    }

    public List<MessageProto.TrustChainBlock> getAllBlocks() {
        return BlockConverter.fromDbBlocks(blockDao.getAllBlocks());
    }

    public List<MessageProto.TrustChainBlock> getBlocks(byte[] publicKey, boolean inLinked) {
        return BlockConverter.fromDbBlocks(blockDao.getBlocks(encodeKey(publicKey), inLinked));
    }

    /**
     * Returns the number of half blocks created by the peer with the provided public key that
     * are still waiting to be signed by the peer with the linked public key.
     */
    public int getHalfBlockCount(byte[] peerPublicKey, byte[] myPublicKey) {
        String encPeerPublicKey = encodeKey(peerPublicKey);
        String encMyPublicKey = encodeKey(myPublicKey);
        List<DbBlock> blocks = blockDao.getBlocks(encPeerPublicKey, false);
        int halfBlockCount = 0;
        for (DbBlock block : blocks) {
            // Is the first half block
            if (block.linkSequenceNumber == 0 && block.linkPublicKey.equals(encMyPublicKey)) {
                DbBlock linkedBlock = blockDao.getLinkedBlock(block.publicKey, block.sequenceNumber,
                        block.linkPublicKey, block.linkSequenceNumber);
                if (linkedBlock == null) {
                    // Is an uncompleted half block
                    halfBlockCount++;
                }
            }
        }
        return halfBlockCount;
    }

    private String encodeKey(byte[] key) {
        return Base64.encodeToString(key, Base64.DEFAULT);
    }
}
