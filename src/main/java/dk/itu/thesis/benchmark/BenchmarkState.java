package dk.itu.thesis.benchmark;

import dk.itu.thesis.api.ConcurrentTree;
import dk.itu.thesis.benchmark.KeyDistribution;
import dk.itu.thesis.api.WorkloadProfile;
import dk.itu.thesis.factory.TreeFactory;
import org.openjdk.jmh.annotations.*;

import java.util.random.RandomGenerator;

@State(Scope.Benchmark)
public class BenchmarkState {

    @Param({"skiplist"})
    public String treeType;

    @Param({"readHeavy"})
    public String workloadName;

    @Param({"UNIFORM"})
    public String keyDistributionName;

    @Param({"100000"})
    public int prefillSize;

    @Param({"1"})
    public int minKey;

    @Param({"1000001"})
    public int maxKeyExclusive;

    // Used only if HOTSPOT is selected
    @Param({"10001"})
    public int hotspotMaxExclusive;

    @Param({"80"})
    public int hotspotProbabilityPercent;

    public ConcurrentTree<Integer, Integer, ?> tree;
    public WorkloadGenerator workloadGenerator;
    public KeyGenerator keyGenerator;

    @Setup(Level.Trial)
    public void setUp() {
        tree = TreeFactory.create(treeType);
        workloadGenerator = new WorkloadGenerator(resolveWorkload(workloadName));
        keyGenerator = new KeyGenerator(
                minKey,
                maxKeyExclusive,
                KeyDistribution.valueOf(keyDistributionName),
                hotspotMaxExclusive,
                hotspotProbabilityPercent
        );

        RandomGenerator random = RandomGenerator.getDefault();

        for (int i = 0; i < prefillSize; i++) {
            int key = keyGenerator.nextKey(random);
            tree.insert(key, key);
        }
    }

    private WorkloadProfile resolveWorkload(String workloadName) {
        return switch (workloadName.toLowerCase()) {
            case "balanced" -> WorkloadProfile.balanced();
            case "snapshotheavy" -> WorkloadProfile.snapshotHeavy();
            case "readheavy" -> WorkloadProfile.readHeavy();
            case "writeheavy" -> WorkloadProfile.writeHeavy();
            default -> throw new IllegalArgumentException("Unknown workload: " + workloadName);
        };
    }
}