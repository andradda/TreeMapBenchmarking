package dk.itu.thesis.api;

public record WorkloadProfile(
        int readPercent,
        int insertPercent,
        int deletePercent,
        int updatePercent,
        int snapshotPercent,
        int rangeQueryPercent
) {
    public WorkloadProfile {
        int total = readPercent
                + insertPercent
                + deletePercent
                + updatePercent
                + snapshotPercent
                + rangeQueryPercent;

        if (total != 100) {
            throw new IllegalArgumentException(
                    "Workload percentages must sum to 100, but got " + total
            );
        }
    }

    public static WorkloadProfile readOnly() {
        return new WorkloadProfile(100, 0, 0, 0, 0, 0);
    }

    public static WorkloadProfile readHeavy() {
        return new WorkloadProfile(90, 5, 5, 0, 0, 0);
    }

    public static WorkloadProfile balanced() {
        return new WorkloadProfile(50, 25, 25, 0, 0, 0);
    }

    public static WorkloadProfile writeHeavy() {
        return new WorkloadProfile(10, 45, 30, 15, 0, 0);
    }

    public static WorkloadProfile snapshotHeavy() {
        return new WorkloadProfile(78, 10, 10, 0, 2, 0);
    }

    public static WorkloadProfile rangeQueryLight() {
        return new WorkloadProfile(79, 10, 10, 0, 0, 1);
    }

    public static WorkloadProfile snapshotAndRange() {
        return new WorkloadProfile(78, 10, 10, 0, 1, 1);
    }
    public static WorkloadProfile snapshotStress() {
        return new WorkloadProfile(70, 10, 10, 0, 10, 0);
    }

    public static WorkloadProfile rangeQueryStress() {
        return new WorkloadProfile(60, 15, 15, 0, 0, 10);
    }
}