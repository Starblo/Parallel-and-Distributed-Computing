import re
import matplotlib.pyplot as plt
from pathlib import Path
import numpy as np
import matplotlib.ticker as mtick

taskno = "2.5"
filename = f"task{taskno}_PDC.out"


hdr = re.compile(
    r"^=== T=(\d+)\s+S=\w+\s+D=(\w+)\s+V=\d+\s+MIX=([\d:]+)\s+O=(\d+)\s+W=\d+\s+M=\d+\s+===$"
)
meas_time = re.compile(r"^Measurement time:\s+(\d+)$")
meas_disc = re.compile(r"^Measurement discrepancy:\s+(\d+)$")

labels = {
    ("Uniform", "1:1:8"): "A.1",
    ("Uniform", "1:1:0"): "A.2",
    ("Normal", "1:1:8"): "B.1",
    ("Normal", "1:1:0"): "B.2",
}

# data[key]["time"][T] -> list of times
# data[key]["disc"][T] -> list of discrepancy
# data[key]["O"][T]    -> O value
data = {}
with open(filename) as f:
    lines = [l.strip() for l in f]

i = 0
while i < len(lines):
    m = hdr.match(lines[i])
    if not m:
        i += 1
        continue
    T, D, MIX, O = int(m.group(1)), m.group(2), m.group(3), int(m.group(4))
    key = labels.get((D, MIX))
    i += 1
    times, discs = [], []
    while i < len(lines) and not hdr.match(lines[i]):
        m1 = meas_time.match(lines[i])
        m2 = meas_disc.match(lines[i])
        if m1:
            times.append(int(m1.group(1)))
        elif m2:
            discs.append(int(m2.group(1)))
        i += 1
    if key:
        if times:
            data.setdefault(key, {}).setdefault("time", {}).setdefault(T, []).extend(times)
        if discs:
            data.setdefault(key, {}).setdefault("disc", {}).setdefault(T, []).extend(discs)
        data.setdefault(key, {}).setdefault("O", {})[T] = O


plt.figure()
for key in sorted(data.keys()):
    if "time" not in data[key]:
        continue
    Ts = sorted(data[key]["time"].keys())

    avg_ms, err_ms = [], []
    for t in Ts:
        vals = np.array(data[key]["time"][t], dtype=float) / 1e6  # ns → ms
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


plt.figure()
for key in sorted(data.keys()):
    if "disc" not in data[key]:
        continue
    Ts = sorted(data[key]["disc"].keys())

    avg_disc, err_disc = [], []
    for t in Ts:
        vals = np.array(data[key]["disc"][t], dtype=float)
        avg_disc.append(vals.mean())
        err_disc.append(vals.std(ddof=1) if len(vals) > 1 else 0.0)

    plt.errorbar(Ts, avg_disc, yerr=err_disc, marker="o", linestyle="-", capsize=3, label=key)

plt.xlabel("Threads")
plt.ylabel("Discrepancy")
plt.title(f"Average discrepancy {taskno}")
plt.grid(True, linestyle="--", alpha=0.4)
plt.legend()
out = f"{Path(filename).stem}_discrepancy.png"
plt.savefig(out, bbox_inches="tight", dpi=160)
plt.close()


plt.figure()
for key in sorted(data.keys()):
    if "disc" not in data[key]:
        continue
    Ts = sorted(data[key]["disc"].keys())

    avg_acc = []
    for t in Ts:
        vals = np.array(data[key]["disc"][t], dtype=float)
        avg_disc = vals.mean()
        O = data[key]["O"][t]
        total = t * O
        acc = (1.0 - avg_disc / total) * 100.0
        avg_acc.append(acc)

    plt.plot(Ts, avg_acc, marker="o", linestyle="-", label=key)

plt.gca().yaxis.set_major_formatter(mtick.FormatStrFormatter('%.6f'))
plt.xlabel("Threads")
plt.ylabel("Accuracy")
plt.title(f"Accuracy {taskno}")
plt.grid(True, linestyle="--", alpha=0.4)
plt.legend()
out = f"{Path(filename).stem}_accuracy.png"
plt.savefig(out, bbox_inches="tight", dpi=160)
plt.close()
