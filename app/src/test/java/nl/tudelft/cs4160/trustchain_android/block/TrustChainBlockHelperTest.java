package nl.tudelft.cs4160.trustchain_android.block;

import com.google.protobuf.ByteString;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;

import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.EMPTY_SIG;
import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.GENESIS_HASH;
import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.GENESIS_SEQ;
import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.containsBinaryFile;
import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.isGenesisBlock;
import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.pubKeyToString;
import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.transferDataToString;
import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.validateTransaction;
import static nl.tudelft.cs4160.trustchain_android.util.ByteArrayConverter.bytesToHexString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TrustChainBlockHelperTest {
    byte[] pkpbytes = { (byte) 0x01, (byte) 0x02, (byte) 0x03};
    byte[] linkpkpbytes = {(byte) 0x10, (byte) 0x11, (byte) 0x12};
    int linkSeq = 1;
    int seq = 5;
    byte[] prevHash = { (byte) 0x1a, (byte) 0x1b, (byte) 0x1c};
    byte[] transaction = "this is a transaction".getBytes();
    MessageProto.TrustChainBlock latestBlock = MessageProto.TrustChainBlock.newBuilder()
                                                            .setSequenceNumber(seq)
                                                            .build();
    MessageProto.TrustChainBlock genesisBlock = MessageProto.TrustChainBlock.newBuilder()
            .setSequenceNumber(GENESIS_SEQ)
            .setPreviousHash(GENESIS_HASH)
            .build();

    MessageProto.TrustChainBlock block = MessageProto.TrustChainBlock.newBuilder()
            .setPublicKey(ByteString.copyFrom(pkpbytes))
            .setSequenceNumber(seq)
            .setLinkPublicKey(ByteString.copyFrom(linkpkpbytes))
            .setLinkSequenceNumber(linkSeq)
            .setPreviousHash(ByteString.copyFrom(prevHash))
            .setSignature(EMPTY_SIG)
            .setTransaction(MessageProto.TrustChainBlock.Transaction.newBuilder()
                    .setUnformatted(ByteString.copyFrom(transaction)).build())
            .build();


    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void testIsGenesisBlock() throws Exception {
        assertTrue(isGenesisBlock(genesisBlock));
    }

    @Test
    public void testIsGenesisBlock2() throws Exception {
        assertFalse(isGenesisBlock(latestBlock));
    }

    @Test
    public void testValidateTransaction() throws Exception {
        assertEquals(ValidationResult.VALID, validateTransaction(block, null).getStatus());
    }

    @Test
    public void testToString() throws Exception {
        String expected = "Trustchainblock: {\n" +
                "\tPublic key: 010203\n" +
                "\tSequence number: " + seq + "\n" +
                "\tLink public key: 101112\n" +
                "\tLink sequence number: " + linkSeq + "\n" +
                "\tPrevious hash: 1A1B1C\n" +
                "\tSignature: 00\n" +
                "\tTransaction: \nthis is a transaction\n" +
                "}";
        assertEquals(expected, TrustChainBlockHelper.toString(block));
    }

    @Test
    public void testToShortString() throws Exception {
        String expected = "Trustchainblock: {\n" +
                "\tPublic key: 010203 (size: 3)\n" +
                "\tSequence number: " + seq + "\n" +
                "\tLink public key: 101112 (size: 3)\n" +
                "\tLink sequence number: " + linkSeq + "\n" +
                "}";
        assertEquals(expected, TrustChainBlockHelper.toShortString(block));
    }

    @Test
    public void testTransferDataToString() throws Exception {
        String expected = "Trustchainblock: { data: " + new String(transaction) + "\n}";
        assertEquals(expected,transferDataToString(block));
    }

    @Test
    public void testTransferDataToStringNull() throws Exception {
        String expected = "Trustchainblock: { data: \n}";
        assertEquals(expected,transferDataToString(block.toBuilder().setTransaction(MessageProto.TrustChainBlock.Transaction.newBuilder().setUnformatted(ByteString.copyFrom("".getBytes())).build()).build()));
    }

    @Test
    public void testPubKeyToString() throws Exception {
        int length = 64;
        byte[] pubKey = new byte[length];
        for(int i = 0; i<length; i++) {
            pubKey[i] = (byte) i;
        }
        String expected = bytesToHexString(pubKey) + " (size: " + length + ")";
        assertEquals(expected, pubKeyToString(pubKey,length*2));
    }

    @Test
    public void testPubKeyToString3() throws Exception {
        int length = 0;
        byte[] pubKey = new byte[length];
        for(int i = 0; i<length; i++) {
            pubKey[i] = (byte) i;
        }
        String expected = " (size: " + length + ")";
        assertEquals(expected, pubKeyToString(pubKey,32));
    }

    @Test
    public void testPubKeyToString4() throws Exception {
        int length = 64;
        byte[] pubKey = new byte[length];
        for(int i = 0; i<length; i++) {
            pubKey[i] = (byte) i;
        }
        String expected = bytesToHexString(new byte[]{pubKey[0]}).substring(0,1) + "(..)" + bytesToHexString(new byte[]{pubKey[length - 1]}).substring(1)
                + " (size: " + length + ")";
        assertEquals(expected, pubKeyToString(pubKey,0));
    }

    @Test
    public void testPubKeyToString2() throws Exception {
        int length = 64;
        byte[] pubKey = new byte[length];
        for(int i = 0; i<length; i++) {
            pubKey[i] = (byte) i;
        }
        String expected = bytesToHexString(Arrays.copyOfRange(pubKey,0,7))
                + "(..)"
                + bytesToHexString(Arrays.copyOfRange(pubKey,length-7,length))
                + " (size: " + length + ")";
        assertEquals(expected, pubKeyToString(pubKey,32));
    }

    @Test
    public void testPubKeyToStringNull() throws Exception {
        byte[] pubKey = null;
        String expected = "";
        assertEquals(expected, pubKeyToString(pubKey,32));
    }

    @Test
    public void testContainsBinaryFile() throws Exception {
        assertFalse(containsBinaryFile(block));
    }

    @Test
    public void testContainsBinaryFile2() throws Exception {
        assertTrue(containsBinaryFile(block.toBuilder().setTransaction(block.getTransaction().toBuilder().setFormat(".pdf")).build()));
    }


}