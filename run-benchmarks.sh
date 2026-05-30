#!/usr/bin/env bash
set -euo pipefail

RESULTS_DIR="benchmark-results"
TMP_DIR="${RESULTS_DIR}/tmp"

mkdir -p "$RESULTS_DIR"
mkdir -p "$TMP_DIR"

# Detect available logical CPUs
CORES=${SLURM_CPUS_PER_TASK:-$(nproc)}   # $(getconf _NPROCESSORS_ONLN)
MAX_THREADS=$((CORES * 2))

# Load hwloc if available
module load hwloc

# Generate system layout PNG
lstopo --of svg system_layout.svg

# Build thread list: 2, 4, 8, ... up to 2x cores
THREADS=()
t=2
while [ "$t" -le "$MAX_THREADS" ]; do
  THREADS+=("$t")
  t=$((t * 2))
done

TREES=("snaptree" "chromatic6" "vcas" "kary")
WORKLOADS=("readonly", "readHeavy", "balanced", "writeHeavy")
DISTS=("UNIFORM" "SKEWED")

for tree in "${TREES[@]}"; do
  for workload in "${WORKLOADS[@]}"; do
    for dist in "${DISTS[@]}"; do

      GROUP_FILE="${RESULTS_DIR}/jmh_${tree}_${workload}_${dist}.csv"
      FIRST=true

      for threads in "${THREADS[@]}"; do
        TMP_FILE="${TMP_DIR}/tmp_${tree}_${workload}_${dist}_${threads}t.csv"

        echo "Running: tree=${tree} workload=${workload} dist=${dist} threads=${threads}"

        ./gradlew jmh \
          -PjmhThreads="${threads}" \
          -PjmhResultFormat=CSV \
          -PjmhResultsFile="${TMP_FILE}" \
          -PjmhIncludes=TreeBenchmark \
          -PjmhBenchmarkParameters="treeType=${tree},workloadName=${workload},keyDistributionName=${dist}"

        if [ "$FIRST" = true ]; then
          cp "$TMP_FILE" "$GROUP_FILE"
          FIRST=false
        else
          tail -n +2 "$TMP_FILE" >> "$GROUP_FILE"
        fi
      done

    done
  done
done