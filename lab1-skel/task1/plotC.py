import matplotlib.pyplot as plt

# Data
threads = [1, 2, 4, 8, 16, 32, 64]

dardel_avg = [12.094, 12.651, 86.977, 254.511, 432.010, 823.746, 1657.572]
dardel_std = [0.165, 0.906, 3.069, 31.100, 23.442, 24.832, 51.714]

local_avg = [4.373, 16.048, 40.512, 85.541, 163.263, 335.736, 676.113]
local_std = [0.713, 2.071, 2.064, 1.006, 1.956, 5.341, 11.077]

# Plot
plt.figure(figsize=(8, 5))
plt.errorbar(threads, dardel_avg, yerr=dardel_std, marker='o', label="Dardel", capsize=5)
plt.errorbar(threads, local_avg, yerr=local_std, marker='s', label="Local", capsize=5)

plt.xlabel("Threads")
plt.ylabel("Execution time (ms)")
plt.title("Synchronization overhead scaling")
plt.legend()
plt.grid(True, linestyle="--", alpha=0.6)
plt.xticks(threads)

plt.tight_layout()
plt.savefig("task1.jpg")
