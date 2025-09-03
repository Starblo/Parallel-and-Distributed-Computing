import re
from pathlib import Path
import pandas as pd
import matplotlib.pyplot as plt

PATH = Path("D.out")
rows, run_id, mode = [], None, None

with PATH.open() as f:
    for line in f:
        line = line.strip()
        m = re.match(r"Run\s+(\d+)\s+\((B|C)\)\s*:", line)
        if m:
            run_id = int(m.group(1))
            mode = m.group(2)
            continue
        m2 = re.match(r"([BC]):\s*delay_ns=(\d+)", line)
        if m2 and run_id is not None:
            which = "Busy-wait" if m2.group(1) == "B" else "Guarded"
            ns = int(m2.group(2))
            rows.append({"run": run_id, "mode": which, "delay_ns": ns, "delay_us": ns / 1000.0})

df = pd.DataFrame(rows).sort_values(["mode", "run"]).reset_index(drop=True)

summary = df.groupby("mode")["delay_us"].agg(["count", "min", "median", "mean", "max"]).round(3)
print(summary.to_string())

plt.figure()
for mode, dsub in df.groupby("mode"):
    dsub = dsub.sort_values("run")
    plt.plot(dsub["run"], dsub["delay_us"], marker="o", label=mode)
plt.xlabel("Run #")
plt.ylabel("Delay (µs)")
plt.title("Notification delay per run")
plt.legend()
plt.tight_layout()
plt.savefig("task2D_line.png", dpi=150)

print("Saved figure: task2D_line.png")
