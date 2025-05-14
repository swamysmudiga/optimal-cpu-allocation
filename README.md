
## ✅ Approaches

### 1. Brute Force
- **Time Complexity:** O(n²)
- **Space Complexity:** O(n)
- **Description:**  
    Iterates through all possible values of `k` (number of instances to use) from `1` to `n`, and checks for valid allocations each time.

### 2. Optimal Approach (Using Binary Search)

- **Time Complexity:** O(n log n)
- **Space Complexity:** O(n)
- **Description:**  
  Uses binary search to find the smallest number of instances `k` for which allocation is possible while keeping the CPU distribution balanced (difference ≤ 1). Sorting is done initially based on available CPUs.