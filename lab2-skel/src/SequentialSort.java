public class SequentialSort implements Sorter {

    public SequentialSort() {}

    public static void main(String[] args) {
        // int[] arr = {5, 4, 3, 2, 1, 5, 6, 7, 9, 11, 32, 1, 0};
        // SequentialSort sorter = new SequentialSort();
        // sorter.sort(arr);
        // System.out.println(java.util.Arrays.toString(arr));
    }

    @Override
    public void sort(int[] arr) {
        if (arr == null || arr.length < 2) return;
        int[] aux = new int[arr.length];
        mergesort(arr, aux, 0, arr.length - 1);
    }

    private void mergesort(int[] arr, int[] aux, int left, int right) {
        if (right - left <= 1) {
            if (arr[left] > arr[right]) {
                int t = arr[right];
                arr[right] = arr[left];
                arr[left] = t;
            }
            return;
        }
        int mid = left + ((right - left) >>> 1);
        mergesort(arr, aux, left, mid);
        mergesort(arr, aux, mid + 1, right);
        merge(arr, aux, left, mid, right);
    }

    private void merge(int[] arr, int[] aux, int left, int mid, int right) {
        int len = right - left + 1;
        int leftPtr = left;
        int rightPtr = mid + 1;

        for (int i = 0; i < len; i++) {
            if (leftPtr > mid || (rightPtr <= right && arr[rightPtr] < arr[leftPtr])) {
                aux[left + i] = arr[rightPtr++];
            } else {
                aux[left + i] = arr[leftPtr++];
            }
        }
        System.arraycopy(aux, left, arr, left, len);
    }

    @Override
    public int getThreads() {
        return 1;
    }
}
