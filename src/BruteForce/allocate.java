package BruteForce;

import java.util.*;

public class allocate {
    //Total TC = O(Nsquare)
    //TOtal SC = O(N)
    static Map<Integer, Integer> allocateCPU(List<Integer> instances, List<Integer> available, int requestedCpu) {

        //Calculating totalAvailable CPUs
        int totalAvailable = 0;
        for(int i = 0; i < available.size(); i++){
            totalAvailable += available.get(i);
        }

        //initializing the map to return {instances : cpu allocation}
        //SC = O(N)  the worst case store all the instance size
        Map<Integer, Integer> allocation = new LinkedHashMap<>();

        //Edge Condition if Requested CPU <= 0
        if(requestedCpu <= 0){
            System.out.println("The number of requested CPU is less than or equal to 0");
            return allocation;
        }
        //Edge Condition: if instances greater or less than available size are different
        if (instances.size() != available.size()) {
            System.out.println("The number of instances and available CPU entries must be the same.");
            return allocation;
        }
        //Edge Condition: Requested CPU > total Available CPU
        if(requestedCpu > totalAvailable){
            System.out.println("Requested CPU is greater than the available CPU");
            return allocation;
        }
        int n = instances.size();

        //Storing all the pairs {{instance id 1, available Cpu 2},{instance id 2, available Cpu 2}...{}}
        //combined = [[1,5], [2,5], [3,4]]
        List<int[]> combined = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            combined.add(new int[] { instances.get(i), available.get(i) });
        }

        // Sort by available CPU descending
        //TC = O(nlogn) (1, 5), (2, 5), (3, 3)
        combined.sort((a, b) -> Integer.compare(b[1], a[1]));

        //TC = O(N) where N is No of instance
        for (int k = 1; k <= n; k++) {
            int q = requestedCpu / k; //12/3 = 4
            int r = requestedCpu % k;// = 0

            boolean valid = true;

            //O() = 1..2..3..n = n (n + 1)/2 = O(nSq)
            for (int i = 0; i < k; i++) {
                // q = 4
                int need = (i < r) ? q + 1 : q;//need 4
                if (combined.get(i)[1] >= need) { //3 >= 4
                    allocation.put(combined.get(i)[0], need);//(1, 4),(2,4),
                } else {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                return allocation;
            }
        }
        System.out.println("No valid allocation found that satisfies the constraints with requested CPU: " + requestedCpu);
        return new HashMap<>();
    }

    public static void main(String[] args) {
        // List<Integer> instances = Arrays.asList();
        // List<Integer> available = Arrays.asList();
        // // int[] testCases = {10, 9, 11, 12, 13, 14, 0, 15, 7, 5, 3};
        // int[] testCases = {};

        // for (Integer requestedCpu : testCases) {
        //     Map<Integer, Integer> result = allocate(instances, available, requestedCpu);
        //     System.out.println("Requested CPU: " + requestedCpu + " → Allocation: " + result);
        // }


        List<Integer> instances = Arrays.asList(1,2,3);
        List<Integer> available = Arrays.asList(5,5,4); //4, 4, 4
        int requestedCpu =12;

        Map<Integer, Integer> result = allocateCPU(instances, available, requestedCpu);
        System.out.println("Optimal result: " + result);
    }
}

