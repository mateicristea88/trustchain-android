package nl.tudelft.cs4160.trustchain_android.storage.database.converter;

import android.util.Base64;

import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.storage.database.entity.DbBlock;

public class BlockConverter {
    private BlockConverter() {}

    public static DbBlock toDbBlock(MessageProto.TrustChainBlock block) {
        DbBlock dbBlock = new DbBlock();
        dbBlock.tx = block.getTransaction().getUnformatted().toByteArray();
        dbBlock.txFormat = block.getTransaction().getFormat();
        dbBlock.publicKey = Base64.encodeToString(block.getPublicKey().toByteArray(), Base64.DEFAULT);
        dbBlock.sequenceNumber = block.getSequenceNumber();
        dbBlock.linkPublicKey = Base64.encodeToString(block.getLinkPublicKey().toByteArray(), Base64.DEFAULT);
        dbBlock.linkSequenceNumber = block.getLinkSequenceNumber();
        dbBlock.previousHash = Base64.encodeToString(block.getPreviousHash().toByteArray(), Base64.DEFAULT);
        dbBlock.signature = Base64.encodeToString(block.getSignature().toByteArray(), Base64.DEFAULT);
        dbBlock.blockHash = Base64.encodeToString(TrustChainBlockHelper.hash(block), Base64.DEFAULT);
        return dbBlock;
    }

    public static MessageProto.TrustChainBlock fromDbBlock(DbBlock dbBlock) {
        MessageProto.TrustChainBlock.Builder builder = MessageProto.TrustChainBlock.newBuilder();
        MessageProto.TrustChainBlock.Transaction transaction = MessageProto.TrustChainBlock.Transaction.newBuilder()
                .setUnformatted(ByteString.copyFrom(dbBlock.tx))
                .setFormat(dbBlock.txFormat)
                .build();
        builder.setTransaction(transaction)
                .setPublicKey(ByteString.copyFrom(Base64.decode(dbBlock.publicKey, Base64.DEFAULT)))
                .setSequenceNumber(dbBlock.sequenceNumber)
                .setLinkPublicKey(ByteString.copyFrom(Base64.decode(dbBlock.linkPublicKey, Base64.DEFAULT)))
                .setLinkSequenceNumber(dbBlock.linkSequenceNumber)
                .setPreviousHash(ByteString.copyFrom(Base64.decode(dbBlock.previousHash, Base64.DEFAULT)))
                .setSignature(ByteString.copyFrom(Base64.decode(dbBlock.signature, Base64.DEFAULT)));
        return builder.build();
    }

    public static List<MessageProto.TrustChainBlock> fromDbBlocks(List<DbBlock> dbBlocks) {
        List<MessageProto.TrustChainBlock> blocks = new ArrayList<>();
        for (DbBlock dbBlock : dbBlocks) {
            blocks.add(BlockConverter.fromDbBlock(dbBlock));
        }
        return blocks;
    }
}
