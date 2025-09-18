import os
import matplotlib.pyplot as plt

P_LIST = [0.2, 0.4, 0.6, 0.8]
A = 0.06
B = 0.05
MAX_THREADS = 64
OUT_DIR = "../data"


def ideal_speedup(N, p):
    # S(N) = 1 / ((1 - p) + p/N)
    return 1.0 / (p + (1.0 - p) / float(N))

def delta_overhead(N, a, b):
    # delta(N)=a+bN (N>1), delta(1)=0
    if N == 1:
        return 0.0
    return a + b * float(N)

def model_speedup(N, p, a, b):
    # S(N) = 1 / ((1 - p) + p/N + delta(N))
    return 1.0 / (p + (1.0 - p) / float(N) + delta_overhead(N, a, b))

def plot_family(title, Ns, series_dict, out_png):
    plt.figure()
    for label, ys in series_dict.items():
        plt.plot(Ns, ys, label=label)
    plt.xlabel("threads")
    plt.ylabel("speedup")
    plt.title(title)
    plt.legend()
    plt.savefig(out_png, dpi=160, bbox_inches="tight")
    plt.close()

def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    Ns = list(range(1, MAX_THREADS + 1))

    # ideal Amdahl's law
    # ideal_curves = {}
    # for p in P_LIST:
    #     ys = [ideal_speedup(N, p) for N in Ns]
    #     ideal_curves[f"p={p}"] = ys
    # plot_family("Ideal Amdahl's Law", Ns, ideal_curves,
    #             os.path.join(OUT_DIR, "plot_ideal.png"))

    # our overhead model
    overhead_curves = {}
    for p in P_LIST:
        ys = [model_speedup(N, p, A, B) for N in Ns]
        overhead_curves[f"p={p}"] = ys
    plot_family(f"Overhead model (delta(N)=a+bN, a={A}, b={B})", Ns, overhead_curves,
                os.path.join(OUT_DIR, "plot_overhead2.png"))

    print("Done. Saved figures to:", OUT_DIR)

if __name__ == "__main__":
    main()
