package nl.tudelft.cs4160.trustchain_android.storage.database.converter;

import com.google.protobuf.ByteString;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.libsodium.jni.NaCl;

import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.storage.database.entity.DbBlock;

import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.EMPTY_SIG;

@RunWith(JUnit4.class)
public class BlockConverterTest {
    private byte[] pkpbytes = { (byte) 0x01, (byte) 0x02, (byte) 0x03};
    private byte[] linkpkpbytes = {(byte) 0x10, (byte) 0x11, (byte) 0x12};
    private int linkSeq = 1;
    private int seq = 5;
    private byte[] prevHash = { (byte) 0x1a, (byte) 0x1b, (byte) 0x1c};
    private byte[] transaction = "this is a transaction".getBytes();
    private MessageProto.TrustChainBlock block = MessageProto.TrustChainBlock.newBuilder()
            .setPublicKey(ByteString.copyFrom(pkpbytes))
            .setSequenceNumber(seq)
            .setLinkPublicKey(ByteString.copyFrom(linkpkpbytes))
            .setLinkSequenceNumber(linkSeq)
            .setPreviousHash(ByteString.copyFrom(prevHash))
            .setSignature(EMPTY_SIG)
            .setTransaction(MessageProto.TrustChainBlock.Transaction.newBuilder()
                    .setUnformatted(ByteString.copyFrom(transaction)).build())
            .build();

    static {
        NaCl.sodium();
    }

    @Test
    public void toAndFromDbBlock() {
        DbBlock dbBlock = BlockConverter.toDbBlock(block);
        MessageProto.TrustChainBlock decodedBlock = BlockConverter.fromDbBlock(dbBlock);
        Assert.assertArrayEquals(transaction, decodedBlock.getTransaction().getUnformatted().toByteArray());
        Assert.assertEquals("", decodedBlock.getTransaction().getFormat());
        Assert.assertArrayEquals(pkpbytes, decodedBlock.getPublicKey().toByteArray());
        Assert.assertEquals(seq, decodedBlock.getSequenceNumber());
        Assert.assertArrayEquals(linkpkpbytes, decodedBlock.getLinkPublicKey().toByteArray());
        Assert.assertEquals(linkSeq, decodedBlock.getLinkSequenceNumber());
        Assert.assertArrayEquals(prevHash, decodedBlock.getPreviousHash().toByteArray());
        Assert.assertEquals(EMPTY_SIG, decodedBlock.getSignature());
    }
}
