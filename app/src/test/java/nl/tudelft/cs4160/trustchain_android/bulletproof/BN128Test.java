package nl.tudelft.cs4160.trustchain_android.bulletproof;

import org.junit.Test;

import java.math.BigInteger;

import cyclops.stream.Generator;
import edu.stanford.cs.crypto.efficientct.GeneratorParams;
import edu.stanford.cs.crypto.efficientct.VerificationFailedException;
import edu.stanford.cs.crypto.efficientct.circuit.groups.BN128Group;
import edu.stanford.cs.crypto.efficientct.circuit.groups.BouncyCastleECPoint;
import edu.stanford.cs.crypto.efficientct.circuit.groups.Group;
import edu.stanford.cs.crypto.efficientct.commitments.PeddersenCommitment;
import edu.stanford.cs.crypto.efficientct.rangeproof.RangeProof;
import edu.stanford.cs.crypto.efficientct.rangeproof.RangeProofProver;
import edu.stanford.cs.crypto.efficientct.rangeproof.RangeProofVerifier;
import edu.stanford.cs.crypto.efficientct.util.ProofUtils;

/**
 * Created by rico on 14-3-18.
 */

public class BN128Test {

    private Group<BouncyCastleECPoint> group = new BN128Group();

    @Test
    public void testCompletness() throws VerificationFailedException {
        BigInteger number = BigInteger.valueOf(255);
        BigInteger randomness = ProofUtils.randomNumber();

        GeneratorParams<BouncyCastleECPoint> parameters = GeneratorParams.generateParams(8,group);
        BouncyCastleECPoint v = parameters.getBase().commit(number, randomness);


        PeddersenCommitment<BouncyCastleECPoint> witness = new PeddersenCommitment<>(parameters.getBase(),number, randomness);
        System.out.println(witness.getCommitment());

        RangeProofProver<BouncyCastleECPoint> prover = new RangeProofProver<>();
        RangeProof<BouncyCastleECPoint> proof = prover.generateProof(parameters, v, witness);

        RangeProofVerifier<BouncyCastleECPoint> verifier = new RangeProofVerifier<>();
        verifier.verify(parameters, v, proof);
    }

}
