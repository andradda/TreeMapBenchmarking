#!/usr/bin/env bash
set -euo pipefail

# Use RESULT_TAG=ARM or RESULT_TAG=X86 from the SLURM script
RESULT_TAG="${RESULT_TAG:-generic}"

RESULTS_DIR="benchmark-results_${RESULT_TAG}"
TMP_DIR="${RESULTS_DIR}/tmp"

mkdir -p "$RESULTS_DIR"
mkdir -p "$TMP_DIR"

# Detect available logical CPUs
CORES=${SLURM_CPUS_PER_TASK:-$(nproc)}
MAX_THREADS=$((CORES * 2))

echo "Result tag: ${RESULT_TAG}"
echo "Results dir: ${RESULTS_DIR}"
echo "Detected CPUs: ${CORES}"
echo "Max threads: ${MAX_THREADS}"

# Save metadata
hostname > "${RESULTS_DIR}/hostname.txt"
uname -m > "${RESULTS_DIR}/architecture.txt"
uname -a > "${RESULTS_DIR}/uname.txt"
java -version > "${RESULTS_DIR}/java-version.txt" 2>&1 || true
lscpu > "${RESULTS_DIR}/lscpu.txt" || true
git rev-parse HEAD > "${RESULTS_DIR}/git-commit.txt" 2>/dev/null || echo "unknown" > "${RESULTS_DIR}/git-commit.txt"

# Load hwloc if available
if command -v module >/dev/null 2>&1; then
  module load hwloc || true
fi

# Generate system layout
if command -v lstopo >/dev/null 2>&1; then
  lstopo --of svg "${RESULTS_DIR}/system_layout.svg" || true
fi

# Build thread list: 1, 2, 4, 8, ... up to 2x cores
THREADS=(1)
t=2
while [ "$t" -le "$MAX_THREADS" ]; do
  THREADS+=("$t")
  t=$((t * 2))
done

echo "Thread counts: ${THREADS[*]}" | tee "${RESULTS_DIR}/thread-counts.txt"

TREES=("snaptree" "chromatic6" "vcas" "kary")
WORKLOADS=("readonly" "readHeavy" "balanced" "writeHeavy")
DISTS=("UNIFORM" "SKEWED")

for tree in "${TREES[@]}"; do
  for workload in "${WORKLOADS[@]}"; do
    for dist in "${DISTS[@]}"; do

      GROUP_FILE="${RESULTS_DIR}/jmh_${tree}_${workload}_${dist}.csv"
      FIRST=true

      for threads in "${THREADS[@]}"; do
        TMP_FILE="${TMP_DIR}/tmp_${tree}_${workload}_${dist}_${threads}t.csv"

        echo "Running: tree=${tree} workload=${workload} dist=${dist} threads=${threads}"

        ./gradlew --no-daemon jmh \
          -PjmhThreads="${threads}" \
          -PjmhResultFormat=CSV \
          -PjmhResultsFile="${TMP_FILE}" \
          -PjmhIncludes=TreeBenchmark \
          -PjmhBenchmarkParameters="treeType=${tree},workloadName=${workload},keyDistributionName=${dist}"

        if [ ! -s "$TMP_FILE" ]; then
          echo "ERROR: Missing or empty result file: $TMP_FILE" >&2
          exit 1
        fi

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

echo "Done. Results written to ${RESULTS_DIR}"