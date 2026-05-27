package dk.itu.thesis.benchmark;

import dk.itu.thesis.benchmark.KeyDistribution;

import java.util.random.RandomGenerator;

import static dk.itu.thesis.benchmark.KeyDistribution.UNIFORM;

public class KeyGenerator {

    private final int minKey;
    private final int maxKeyExclusive;
    private final KeyDistribution distribution;

    // Used only for SKEWED mode
    private final int skewedMaxExclusive;
    private final int skewedProbabilityPercent;

    public KeyGenerator(int minKey,
                        int maxKeyExclusive,
                        KeyDistribution distribution,
                        int skewedMaxExclusive,
                        int skewedProbabilityPercent) {
        if (minKey >= maxKeyExclusive) {
            throw new IllegalArgumentException("minKey must be less than maxKeyExclusive");
        }

        if (distribution == KeyDistribution.SKEWED) {
            if (skewedMaxExclusive <= minKey || skewedMaxExclusive > maxKeyExclusive) {
                throw new IllegalArgumentException("Invalid skewed range");
            }
            if (skewedProbabilityPercent < 0 || skewedProbabilityPercent > 100) {
                throw new IllegalArgumentException("Skewed probability must be between 0 and 100");
            }
        }

        this.minKey = minKey;
        this.maxKeyExclusive = maxKeyExclusive;
        this.distribution = distribution;
        this.skewedMaxExclusive = skewedMaxExclusive;
        this.skewedProbabilityPercent = skewedProbabilityPercent;
    }

    public int nextKey(RandomGenerator random) {
        return switch (distribution) {
            case UNIFORM -> random.nextInt(minKey, maxKeyExclusive);
            case SKEWED -> nextHotspotKey(random);
        };
    }

    private int nextHotspotKey(RandomGenerator random) {
        int p = random.nextInt(100);

        if (p < skewedProbabilityPercent) {
            return random.nextInt(minKey, skewedMaxExclusive);
        }

        return random.nextInt(minKey, maxKeyExclusive);
    }
}