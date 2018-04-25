package nl.tudelft.cs4160.trustchain_android.zkp;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class TestAttestation {



    @Test
    public void testGenerate_MinverseGroup() {
        BigInteger p = BigInteger.valueOf(12253454);
        BigInteger[] res = Attestation.generateModularAdditiveInverse(p, 20);
        assertEquals(20,res.length);

        BigInteger sum = BigInteger.ZERO;
        for (BigInteger val : res) {
            sum = sum.add(val);
        }
        assertEquals(0, sum.mod(p.add(BigInteger.ONE)).compareTo(BigInteger.ZERO));
    }

}
