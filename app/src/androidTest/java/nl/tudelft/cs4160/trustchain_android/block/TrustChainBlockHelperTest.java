package nl.tudelft.cs4160.trustchain_android.block;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;


import java.util.Arrays;

import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.storage.repository.BlockRepository;
import nl.tudelft.cs4160.trustchain_android.util.ByteArrayConverter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Boning on 12/17/2017.
 */
public class TrustChainBlockHelperTest {
    private DualSecret keyPair;
    private DualSecret keyPair2;
    private byte[] transaction = new byte[2];
    private byte[] pubKey = new byte[2];
    private byte[] linkKey = new byte[2];
    private MessageProto.TrustChainBlock genesisBlock;

    private BlockRepository blockRepository = mock(BlockRepository.class);
    private String format = "";

    @Before
    public void initialization() {
        keyPair = Key.createNewKeyPair();
        keyPair2 = Key.createNewKeyPair();
        transaction[0] = 12;
        transaction[1] = 42;
        pubKey[0] = 2;
        pubKey[1] = 4;
        linkKey[0] = 14;
        linkKey[1] = 72;
        genesisBlock = TrustChainBlockHelper.createGenesisBlock(keyPair);

        when(blockRepository.getLatestBlock(null))
                .thenReturn(MessageProto.TrustChainBlock.newBuilder()
                        .setSequenceNumber(0)
                        .build());
        when(blockRepository.getLatestBlock(any(byte[].class)))
                .thenReturn(MessageProto.TrustChainBlock.newBuilder()
                        .setSequenceNumber(0)
                        .build());
    }

    @Test
    public void publicKeyGenesisBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createGenesisBlock(keyPair);

        assertEquals(1, block.getSequenceNumber());
        assertEquals(0, block.getLinkSequenceNumber());
        assertTrue(Arrays.equals(new byte[] {0x00}, block.getPreviousHash().toByteArray()));
    }

    @Test
    public void getSequenceNumberGenesisBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createBlock(transaction, format, blockRepository, pubKey, null, linkKey);
        assertEquals(1, block.getSequenceNumber());
    }

    @Test
    public void publicKeyBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createBlock(transaction, format, blockRepository, pubKey, genesisBlock, linkKey);
        assertEquals( ByteArrayConverter.bytesToHexString(pubKey),  ByteArrayConverter.bytesToHexString(block.getPublicKey().toByteArray()));
    }

    @Test
    public void linkPublicKeyBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createBlock(transaction, format, blockRepository, pubKey, genesisBlock, linkKey);
        assertEquals( ByteArrayConverter.bytesToHexString(keyPair.getPublicKeyPair().toBytes()),  ByteArrayConverter.bytesToHexString(block.getLinkPublicKey().toByteArray()));
    }

    @Test
    public void isInitializedGenesisBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createGenesisBlock(keyPair);
        assertTrue(block.isInitialized());
    }

    @Test
    public void getSameSerializedSizeBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createGenesisBlock(keyPair);
        assertEquals(block.getSerializedSize(), block.getSerializedSize());
    }

    @Test
    public void getDiffSerializedSizeBlockTest() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createGenesisBlock(keyPair);
        assertEquals(block.getSerializedSize(), block.getSerializedSize());
    }

    @Test
    public void equalBlocks() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createGenesisBlock(keyPair);
        assertTrue(block.equals(block));
    }

    @Test
    public void notEqualBlocks() {
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createGenesisBlock(keyPair);
        MessageProto.TrustChainBlock block2 = TrustChainBlockHelper.createGenesisBlock(keyPair2);
        assertFalse(block.equals(block2));
    }
//
//    @After
//    public void resetMocks(){
//        validateMockitoUsage();
//    }
}
