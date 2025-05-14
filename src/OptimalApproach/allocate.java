package OptimalApproach;

import java.util.*;
public class allocate {

    static Map<Integer, Integer> allocateCPU(List<Integer> instances, List<Integer> available, int requestedCpu) {

        int n = instances.size();
        int totalAvailable = 0;

        //Counting total available CPU's
        for (int cpu : available) totalAvailable += cpu;

        Map<Integer, Integer> bestAllocation = new LinkedHashMap<>();

        // edge case checks : requested CPU  <= 0
        if (requestedCpu <= 0) {
            System.out.println("Requested CPU is less than or equal to 0");
            return bestAllocation;
        }

        // edge case checks : No of Instances not equal to available CPUs
        if (n != available.size()) {
            System.out.println("Mismatch between instances and available list sizes");
            return bestAllocation;
        }

        // edge case checks : Requested CPUs greater than total Available CPUs
        if (requestedCpu > totalAvailable) {
            System.out.println("Requested CPU is greater than total available CPU");
            return bestAllocation;
        }

        // Combine instance ID with available CPU
        List<int[]> combined = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            combined.add(new int[]{instances.get(i), available.get(i)});
        }

        // Sort descending by available CPU
        combined.sort((a, b) -> Integer.compare(b[1], a[1]));

        int low = 1, high = n;

        while (low <= high) {
            int mid = low + (high - low)/2;

            // CPUs per instance if evenly distributed (quotient)
            int q = requestedCpu / mid;

            // Remaining CPUs after even distribution (remainder)
            int r = requestedCpu % mid;

            boolean valid = true;
            Map<Integer, Integer> temp = new LinkedHashMap<>();

            for (int i = 0; i < mid; i++) {
                int need = (i < r) ? q + 1 : q;
                if (combined.get(i)[1] >= need) {
                    temp.put(combined.get(i)[0], need);
                } else {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                // store best so far
                bestAllocation = temp;

                // try less instances
                high = mid - 1;
            } else {
                // need more instances
                low = mid + 1;
            }
        }

        if (bestAllocation.isEmpty()) {
            System.out.println("No valid allocation found that satisfies the constraints with requested CPU: " + requestedCpu);
        }

        return bestAllocation;
    }

    public static void main(String[] args) {
        List<Integer> instances = Arrays.asList(1, 2, 3);
        List<Integer> available = Arrays.asList(5, 5, 3);

        int requestedCpu = 11; // test with 9, 10, 11, 14, 15

        Map<Integer, Integer> result = allocateCPU(instances, available, requestedCpu);
        System.out.println("Optimal result: " + result);
    }
}