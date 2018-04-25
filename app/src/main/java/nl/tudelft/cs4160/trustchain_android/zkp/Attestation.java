package nl.tudelft.cs4160.trustchain_android.zkp;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Attestation {


    public static BigInteger[] generateModularAdditiveInverse(BigInteger p, int n) {
        SecureRandom random = new SecureRandom();
        BigInteger[] randomList = new BigInteger[n];
        for (int i = 0; i < randomList.length-1; i++) {
            randomList[i] = getRandomBigInteger(p, random);
        }
        BigInteger sum = BigInteger.ZERO;//
        for (int i = 0; i < randomList.length-1; i++) {
            sum = sum.add(randomList[i]);
        }
        //this is: p - (sum(randomList) % (p + 1)) + 1
        BigInteger append = p.subtract( sum.mod( p.add(BigInteger.ONE )) ).add(BigInteger.ONE);
        randomList[randomList.length-1] = append;
        shuffleArray(randomList);
        //TODO: what is easier a list or an array
        Collections.shuffle(Arrays.asList(randomList));
        return randomList;
    }

    private static BigInteger getRandomBigInteger(BigInteger n, Random rnd) {
        BigInteger r;
        BigInteger z = n.subtract(BigInteger.ONE);
        do  {
            r = new BigInteger(z.bitLength(), rnd);
        } while(r.compareTo(z)>=0 || BigInteger.ONE.compareTo(r) == 0 || BigInteger.ZERO.compareTo(r) == 0);
        return r;
    }

    private static <T> void shuffleArray(T[] ar)  {
        Random rnd = new SecureRandom();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            T a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
}
