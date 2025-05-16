package Multithreading;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentCPUAllocator {

    static ConcurrentHashMap<Integer, AtomicInteger> sharedAvailable = new ConcurrentHashMap<>();
    static final ReentrantLock lock = new ReentrantLock();

    public static Map<Integer, Integer> allocateCPU(List<Integer> instances, int requestedCpu) {
        Map<Integer, Integer> bestAllocation = new LinkedHashMap<>();
        int n = instances.size();

        lock.lock(); // Ensure thread-safe allocation
        try {
            if (requestedCpu <= 0) {
                System.out.println("Requested CPU must be > 0.");
                return Collections.emptyMap();
            }

            int totalAvailable = instances.stream()
                    .mapToInt(id -> sharedAvailable.getOrDefault(id, new AtomicInteger(0)).get())
                    .sum();
            if (requestedCpu > totalAvailable) {
                System.out.println("Requested CPU (" + requestedCpu + ") exceeds total available (" + totalAvailable + ")");
                return Collections.emptyMap();
            }

            // Take snapshot of available CPUs
            List<int[]> combined = new ArrayList<>();
            for (int id : instances) {
                int cpu = sharedAvailable.get(id).get(); // snapshot
                combined.add(new int[]{id, cpu});
            }

            combined.sort((a, b) -> Integer.compare(b[1], a[1])); // sort by available descending

            for (int[] entry : combined) {
                if (entry[1] >= requestedCpu) {
                    Map<Integer, Integer> direct = new LinkedHashMap<>();
                    direct.put(entry[0], requestedCpu);
                    sharedAvailable.get(entry[0]).addAndGet(-requestedCpu);
                    return direct;
                }
            }

            int low = 1, high = n;

            while (low <= high) {
                int mid = (low + high) / 2;
                int q = requestedCpu / mid;
                int r = requestedCpu % mid;

                boolean valid = true;
                Map<Integer, Integer> temp = new LinkedHashMap<>();

                for (int i = 0; i < mid; i++) {
                    int id = combined.get(i)[0];
                    int available = combined.get(i)[1]; // read from snapshot
                    int need = (i < r) ? q + 1 : q;

                    if (available >= need) {
                        temp.put(id, need);
                    } else {
                        valid = false;
                        break;
                    }
                }

                if (valid) {
                    bestAllocation = temp;
                    high = mid - 1;
                } else {
                    low = mid + 1;
                }
            }

            if (!bestAllocation.isEmpty()) {
                for (Map.Entry<Integer, Integer> entry : bestAllocation.entrySet()) {
                    sharedAvailable.get(entry.getKey()).addAndGet(-entry.getValue());
                }

                // Log updated pool
                System.out.println("Updated shared pool after allocation:");
                sharedAvailable.forEach((id, val) ->
                        System.out.println("Instance " + id + ": " + val.get() + " CPUs")
                );

                return bestAllocation;
            } else {
                System.out.println("Could not allocate. No valid plan.");
                return Collections.emptyMap();
            }

        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        List<Integer> instances = Arrays.asList(1, 2, 3);
        List<Integer> initialCpu = Arrays.asList(5, 5, 4);
        for (int i = 0; i < instances.size(); i++) {
            sharedAvailable.put(instances.get(i), new AtomicInteger(initialCpu.get(i)));
        }

        int[] requests = {9, 10, 11}; // simulate 3 users
        ExecutorService executor = Executors.newFixedThreadPool(requests.length);

        for (int i = 0; i < requests.length; i++) {
            final int userNum = i + 1;
            final int cpu = requests[i];

            executor.submit(() -> {
                System.out.println("\nUser " + userNum + " requests: " + cpu + " CPUs");
                Map<Integer, Integer> result = allocateCPU(instances, cpu);

                if (result.isEmpty()) {
                    System.out.println("Allocation failed for User " + userNum);
                } else {
                    System.out.println("Allocation for User " + userNum + ": " + result);
                }

                synchronized (System.out) {
                    System.out.println("Remaining CPU per instance:");
                    for (int id : instances) {
                        System.out.println("Instance " + id + ": " + sharedAvailable.get(id).get() + " CPUs");
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
}

