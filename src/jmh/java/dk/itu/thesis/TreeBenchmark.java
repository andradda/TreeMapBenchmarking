package dk.itu.thesis;

import dk.itu.thesis.api.RangeQueryableTree;
import dk.itu.thesis.api.TreeOperation;
import dk.itu.thesis.benchmark.BenchmarkState;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 5, time = 5)
@Fork(1)
public class TreeBenchmark {

    @Benchmark
    public void mixedWorkload(BenchmarkState state, Blackhole blackhole) {
        RandomGenerator random = RandomGenerator.getDefault();

        TreeOperation operation = state.workloadGenerator.next(random);
        int key = state.keyGenerator.nextKey(random);

        switch (operation) {
            case READ -> blackhole.consume(state.tree.get(key));
            case INSERT -> blackhole.consume(state.tree.insert(key, key));
            case DELETE -> blackhole.consume(state.tree.delete(key));
            case UPDATE -> blackhole.consume(state.tree.update(key, key + 1));
            case SNAPSHOT -> blackhole.consume(state.tree.snapshot());
            case RANGE_QUERY -> {
                int lo = state.keyGenerator.nextKey(random);
                int hi = Math.min(state.maxKeyExclusive - 1, lo + state.rangeSize);

                if (state.tree instanceof RangeQueryableTree<?, ?, ?> rawRangeTree) 
                {
                    @SuppressWarnings("unchecked")
                    RangeQueryableTree<Integer, Integer, ?> rangeTree = (RangeQueryableTree<Integer, Integer, ?>) rawRangeTree;
                    blackhole.consume(rangeTree.rangeQuery(lo, hi));
                } 
                else 
                {
                    throw new UnsupportedOperationException(state.tree.getClass().getSimpleName() + " does not support range queries");
                }
            }
        }
    }
}