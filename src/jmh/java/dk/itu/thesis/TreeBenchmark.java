package dk.itu.thesis;

import dk.itu.thesis.api.TreeOperation;
import dk.itu.thesis.benchmark.BenchmarkState;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 5, time = 5)
@Fork(1)
public class TreeBenchmark {

    @Benchmark
    public void mixedWorkload(BenchmarkState state) {
        RandomGenerator random = RandomGenerator.getDefault();

        TreeOperation operation = state.workloadGenerator.next(random);
        int key = state.keyGenerator.nextKey(random);

        switch (operation) {
            case READ -> state.tree.get(key);
            case INSERT -> state.tree.insert(key, key);
            case DELETE -> state.tree.delete(key);
            case UPDATE -> state.tree.update(key, key + 1);
            case SNAPSHOT -> state.tree.snapshot();
        }
    }
}