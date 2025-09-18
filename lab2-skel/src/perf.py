import re
import matplotlib.pyplot as plt

FILES = [
    ("task1.out",       "Sequential"),
    ("task3_65536.out", "ExecutorService"),
    ("task4_65536.out", "ForkJoin"),
    ("task5.out",       "Parallel"),
]

OUT_DIR = "../data"
TIME_PNG = f"{OUT_DIR}/time_perf.png"
SPEEDUP_PNG = f"{OUT_DIR}/speedup_perf.png"

LINE_RE = re.compile(r'^([A-Za-z]+)\s+(\d+)\s+(\d+\.\d{2})\s+(\d+\.\d{2})$')

def parse_one(path):
    rows = []
    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        for s in f:
            m = LINE_RE.match(s.strip())
            if m:
                t = int(m.group(2))
                tm = float(m.group(3))
                rows.append((t, tm))
    return rows

def main():
    data = {label: parse_one(fn) for fn, label in FILES}

    base_rows = data.get("Sequential", [])
    if not base_rows:
        raise SystemExit("Sequential baseline not found.")
    base_time = base_rows[0][1]

    xs = sorted({t for label, rows in data.items() if label != "Sequential" for t, _ in rows})
    if not xs:
        xs = sorted({t for t, _ in base_rows})

    plt.figure()
    for label, rows in data.items():
        if label == "Sequential":
            continue
        rows = sorted(rows)
        plt.plot([t for t, _ in rows], [tm for _, tm in rows], marker='o', label=label)
    plt.plot([xs[0], xs[-1]], [base_time, base_time], '--', label="Sequential (baseline)")
    plt.xlabel("Threads")
    plt.ylabel("Time")
    plt.title("Runtime vs Threads")
    plt.grid(True, linestyle='--', linewidth=0.5)
    plt.legend()
    plt.tight_layout()
    plt.savefig(TIME_PNG, dpi=160)
    plt.close()

    plt.figure()
    for label, rows in data.items():
        if label == "Sequential":
            continue
        rows = sorted(rows)
        plt.plot([t for t, _ in rows], [base_time / tm for _, tm in rows], marker='o', label=label)
    plt.plot([xs[0], xs[-1]], [1, 1], '--', label="Sequential (baseline)")
    plt.xlabel("Threads")
    plt.ylabel("Speedup")
    plt.title("Speedup vs Threads")
    plt.grid(True, linestyle='--', linewidth=0.5)
    plt.legend()
    plt.tight_layout()
    plt.savefig(SPEEDUP_PNG, dpi=160)
    plt.close()

if __name__ == "__main__":
    main()
