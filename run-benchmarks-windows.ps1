$ErrorActionPreference = "Stop"

$resultsDir = "benchmark-results"
$tempDir = Join-Path $resultsDir "tmp"

New-Item -ItemType Directory -Force -Path $resultsDir | Out-Null
New-Item -ItemType Directory -Force -Path $tempDir | Out-Null

$cores = [Environment]::ProcessorCount
$maxThreads = $cores * 2

$threads = @()
$t = 2
while ($t -le $maxThreads) {
    $threads += $t
    $t *= 2
}

$trees = @("snaptree", "skiplist")
$workloads = @("readHeavy", "balanced", "snapshotHeavy", "writeHeavy")
$dists = @("UNIFORM", "SKEWED")

foreach ($tree in $trees) {
    foreach ($workload in $workloads) {
        foreach ($dist in $dists) {

            $groupFile = Join-Path $resultsDir "jmh_${tree}_${workload}_${dist}.csv"
            $first = $true

            foreach ($thread in $threads) {
                $tempFile = Join-Path $tempDir "tmp_${tree}_${workload}_${dist}_${thread}t.csv"

                Write-Host "Running: tree=$tree workload=$workload dist=$dist threads=$thread"

                & .\gradlew.bat `
                    "jmh" `
                    "-PjmhThreads=$thread" `
                    "-PjmhResultFormat=CSV" `
                    "-PjmhResultsFile=$tempFile" `
                    "-PjmhIncludes=TreeBenchmark" `
                    "-PjmhBenchmarkParameters=treeType=$tree,workloadName=$workload,keyDistributionName=$dist"

                if ($first) {
                    Get-Content $tempFile | Set-Content $groupFile
                    $first = $false
                } else {
                    Get-Content $tempFile | Select-Object -Skip 1 | Add-Content $groupFile
                }
            }
        }
    }
}