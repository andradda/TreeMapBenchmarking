package dk.itu.thesis.benchmark;

import dk.itu.thesis.api.TreeOperation;
import dk.itu.thesis.api.WorkloadProfile;

import java.util.random.RandomGenerator;

public class WorkloadGenerator {

    private final WorkloadProfile profile;

    public WorkloadGenerator(WorkloadProfile profile) {
        this.profile = profile;
    }

    public TreeOperation next(RandomGenerator random) {
    int p = random.nextInt(100);

    int limit = profile.readPercent();
    if (p < limit) {
        return TreeOperation.READ;
    }

    limit += profile.insertPercent();
    if (p < limit) {
        return TreeOperation.INSERT;
    }

    limit += profile.deletePercent();
    if (p < limit) {
        return TreeOperation.DELETE;
    }

    limit += profile.updatePercent();
    if (p < limit) {
        return TreeOperation.UPDATE;
    }

    limit += profile.snapshotPercent();
    if (p < limit) {
        return TreeOperation.SNAPSHOT;
    }

    return TreeOperation.RANGE_QUERY;
}
}