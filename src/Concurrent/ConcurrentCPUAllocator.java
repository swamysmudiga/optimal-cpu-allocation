package Concurrent;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentCPUAllocator {

    // Shared resource map: instance ID → available CPU
    static ConcurrentHashMap<Integer, AtomicInteger> sharedAvailable = new ConcurrentHashMap<>();

    /**
     * Allocates requested whole number CPUs from shared instances.
     * Thread-safe using atomic compare-and-set per instance.
     *
     * @param instances List of instance IDs
     * @param requestedCpu CPU count to allocate
     * @return Map of instance ID to allocated CPU count, or empty map if not possible
     */
    public static Map<Integer, Integer> allocate(List<Integer> instances, int requestedCpu) {
        Map<Integer, Integer> allocation = new LinkedHashMap<>();
        int remaining = requestedCpu;

        // Validate requested CPU
        if (requestedCpu <= 0) {
            System.out.println("Requested CPU must be greater than 0.");
            return Collections.emptyMap();
        }

        // Optional early exit if total available is insufficient
        int totalAvailable = instances.stream()
                .mapToInt(id -> sharedAvailable.getOrDefault(id, new AtomicInteger(0)).get())
                .sum();
        if (requestedCpu > totalAvailable) {
            System.out.println("Requested CPU (" + requestedCpu + ") exceeds total available (" + totalAvailable + ").");
            return Collections.emptyMap();
        }

        try {
            // Sort instances by available CPU (descending)
            List<Integer> sorted = new ArrayList<>(instances);
            sorted.sort((a, b) -> Integer.compare(
                    sharedAvailable.get(b).get(),
                    sharedAvailable.get(a).get()
            ));

            for (int id : sorted) {
                AtomicInteger ref = sharedAvailable.get(id);
                if (ref == null) {
                    System.out.println("Instance " + id + " not found in shared pool.");
                    continue;
                }

                while (true) {
                    int available = ref.get();
                    if (available <= 0) break;

                    int toAllocate = Math.min(available, remaining);
                    if (ref.compareAndSet(available, available - toAllocate)) {
                        allocation.put(id, toAllocate);
                        remaining -= toAllocate;
                        break;
                    }
                }

                if (remaining <= 0) break;
            }

            // If allocation failed, rollback
            if (remaining > 0) {
                for (Map.Entry<Integer, Integer> entry : allocation.entrySet()) {
                    sharedAvailable.get(entry.getKey()).addAndGet(entry.getValue());
                }
                System.out.println("Could not allocate full request. Rolled back.");
                return Collections.emptyMap();
            }

            return allocation;

        } catch (Exception e) {
            System.err.println("Exception during allocation: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Setup: Initialize shared pool
        List<Integer> instances = Arrays.asList(1, 2, 3);
        List<Integer> initialCpu = Arrays.asList(5, 5, 4);

        for (int i = 0; i < instances.size(); i++) {
            sharedAvailable.put(instances.get(i), new AtomicInteger(initialCpu.get(i)));
        }

        int[] requests = {9, 10, 11}; // Simulate 3 users

        int userNum = 1;
        for (int cpu : requests) {
            System.out.println("\nUser " + userNum + " requests: " + cpu + " CPUs");
            Map<Integer, Integer> result = allocate(instances, cpu);
            if (result.isEmpty()) {
                System.out.println("Allocation failed for User " + userNum);
            } else {
                System.out.println("Allocation for User " + userNum + ": " + result);
            }

            System.out.println("Remaining CPU per instance:");
            for (int id : instances) {
                System.out.println("Instance " + id + ": " + sharedAvailable.get(id).get() + " CPUs");
            }

            userNum++;
        }
    }
}

