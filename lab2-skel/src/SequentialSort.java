public class SequentialSort implements Sorter {

        public SequentialSort() {
                
        }

    public static void main(String[] args){
        // int[] arr = {5,4,3,2,1,5,6,7,9,11,32,1,0};
        // SequentialSort sequentialSort = new SequentialSort();
        // sequentialSort.sort(arr);
        // System.out.println(Arrays.toString(arr));
    }

    public void sort(int[] arr) {
        mergesort(arr, 0, arr.length - 1);
    }

    public void mergesort(int[] arr, int left, int right)
    {
        if (right - left <= 1)
        {
            if (arr[left] > arr[right])
            {
                int tempt = arr[right];
                arr[right] = arr[left];
                arr[left] = tempt;
            }
            return;
        }
        int mid = (left + right) / 2;
        mergesort(arr, left, mid);
        mergesort(arr, mid + 1, right);
        merge(arr, left, mid, right);
    }

    public void merge(int[] arr, int left, int mid, int right)
    {
        int len = right - left + 1;
        int[] tempt_arr = new int[len];
        int left_ptr = left;
        int right_ptr = mid + 1;
        for(int i = 0; i < len; i++)
        {
            if (left_ptr > mid || (right_ptr <= right && arr[right_ptr] < arr[left_ptr]))
            {
                tempt_arr[i] = arr[right_ptr];
                right_ptr++;
            }
            else
            {
                tempt_arr[i] = arr[left_ptr];
                left_ptr++;
            }
        }
        System.arraycopy(tempt_arr, 0, arr, left, len);
    }

        public int getThreads() {
                return 1;
        }
}
