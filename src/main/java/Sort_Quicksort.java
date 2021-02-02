//快速排序

//总结要点：
//1：注意哨兵的选择，以及排序过程中index的寻找，特别是firstindex的赋值
//2：递归的调用
//思考过程：无序的数，怎么运用快排，找到一个哨兵，
// 然后从头开始，每一个进行判断，并且有个比哨兵数小的index位，
// 如果循环中这个数大于哨兵数，不做处理，
// 如果小的话，需要将这个数与哨兵前一位进行对换，
// 循环结束后，应该把哨兵数与索引数前一位进行互换。
// 从开始进行找到index数，然后对[firstindex,index][index+1,lastindex]进行递归。

import java.util.Arrays;

public class Sort_Quicksort {
    private static int Partition(int sortarray[], int firstindex, int lastindex) {
        int index = firstindex - 1;  // 注意此时index的赋值
        for (int m = firstindex; m < lastindex; m++) {
            if (sortarray[m] < sortarray[lastindex]) {
                index += 1;
                int temp = sortarray[m];
                sortarray[m] = sortarray[index];
                sortarray[index] = temp;
            }
        }
        int temp = sortarray[lastindex];  //最后将末尾的哨兵放入index+1位置
        sortarray[lastindex] = sortarray[index+1];
        sortarray[index+1] = temp;
        return index;
    }
    private static void Sort(int arr[], int firstindex, int lastindex){
        if (firstindex < lastindex){  //注意递归中一定要有判断递归结束的条件
            int index = Partition(arr, firstindex, lastindex);
            Sort(arr,firstindex,index);
            Sort(arr,index+2,lastindex);  //注意递归时候的索引位置，+1，+2都可以
        }
    }
    public static void main(String args[]){
        int a[]={4,5,1,8,7,0,9,6,3};
        System.out.println(Arrays.toString(a));
        Sort(a,0,a.length-1);
        System.out.println(Arrays.toString(a));
    }
}