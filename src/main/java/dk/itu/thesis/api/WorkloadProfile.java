package dk.itu.thesis.api;

public record WorkloadProfile(
        int readPercent,
        int insertPercent,
        int deletePercent,
        int updatePercent,
        int snapshotPercent
) {
    public WorkloadProfile {
        int total = readPercent + insertPercent + deletePercent + updatePercent + snapshotPercent;
        if (total != 100) {
            throw new IllegalArgumentException(
                    "Workload percentages must sum to 100, but got " + total
            );
        }
    }

    public static WorkloadProfile readHeavy() {
        return new WorkloadProfile(90, 5, 0, 5, 0);
    }

    public static WorkloadProfile balanced() {
        return new WorkloadProfile(50, 25, 0, 25, 0);
    }

    public static WorkloadProfile writeHeavy() {
        return new WorkloadProfile(10, 45, 0, 45, 0);
    }

    public static WorkloadProfile snapshotHeavy() {
        return new WorkloadProfile(75, 10, 10, 0, 5);
    }
}