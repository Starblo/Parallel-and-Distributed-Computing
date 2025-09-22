import re
import matplotlib.pyplot as plt
from pathlib import Path

filename = "task1.1.out"
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
    avg_ms = [sum(data[key][t]) / len(data[key][t]) / 1e6 for t in Ts]
    plt.plot(Ts, avg_ms, marker="o", label=key)

plt.xlabel("Threads")
plt.ylabel("Execution time (ms)")
plt.title(f"Average execution time 1.1")
plt.grid(True, linestyle="--", alpha=0.4)
plt.legend()
out = f"{Path(filename).stem}_summary.png"
plt.savefig(out, bbox_inches="tight", dpi=160)
plt.close()
