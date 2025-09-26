import re
import matplotlib.pyplot as plt
from pathlib import Path
import numpy as np

taskno = "1.1"
filename = f"task{taskno}.out"
hdr = re.compile(r"^=== T=(\d+)\s+S=\w+\s+D=(\w+)\s+V=\d+\s+MIX=([\d:]+)\s+O=\d+\s+W=\d+\s+M=\d+\s+===$")
meas = re.compile(r"^Measurement time:\s+(\d+)$")

labels = {
    ("Uniform","1:1:8"): "A.1",
    ("Uniform","1:1:0"): "A.2",
    ("Normal" ,"1:1:8"): "B.1",
    ("Normal" ,"1:1:0"): "B.2",
}

data = {}
with open(filename) as f:
    lines = [l.strip() for l in f]

i = 0
while i < len(lines):
    m = hdr.match(lines[i])
    if not m:
        i += 1
        continue
    T, D, MIX = int(m.group(1)), m.group(2), m.group(3)
    key = labels.get((D, MIX))
    i += 1
    times = []
    while i < len(lines) and not hdr.match(lines[i]):
        m2 = meas.match(lines[i])
        if m2: times.append(int(m2.group(1)))
        i += 1
    if key and times:
        data.setdefault(key, {}).setdefault(T, []).extend(times)

plt.figure()
for key in sorted(data.keys()):
    Ts = sorted(data[key].keys())

    avg_ms, err_ms = [], []
    for t in Ts:
        vals = np.array(data[key][t], dtype=float) / 1e6
        avg_ms.append(vals.mean())
        err_ms.append(vals.std(ddof=1) if len(vals) > 1 else 0.0)

    plt.errorbar(Ts, avg_ms, yerr=err_ms, marker="o", linestyle="-", capsize=3, label=key)

plt.xlabel("Threads")
plt.ylabel("Execution time (ms)")
plt.title(f"Average execution time {taskno}")
plt.grid(True, linestyle="--", alpha=0.4)
plt.legend()
out = f"{Path(filename).stem}_summary.png"
plt.savefig(out, bbox_inches="tight", dpi=160)
plt.close()
