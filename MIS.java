import java.io.*;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Simulates a synchronous distributed system and implements MIS algorithm using multi threading
 */
public class MIS {

    // Shared memory
    static Map<String, Integer> randomIdsMap = new HashMap<>();
    static Map<String, Boolean> isCandidateMap = new HashMap<>();
    static Map<String, Boolean> isIndependentMap = new HashMap<>();
    static CyclicBarrier gate = null;
    static int rounds = 0;
    static int n = 0;

    /**
     * Simulates the work done by a process node. Here, every process has its own thread
     */
    private static class ProcessNode implements Runnable {

        private final List<Integer> neighbors;
        int roundCount = 0;

        ProcessNode(int pid, List<Integer> neighbors) {

            int randomId = 1 + new Random().nextInt((int) Math.pow(n, 4)); // Assign a random id
            randomIdsMap.put(Integer.toString(pid), randomId);

            this.neighbors = neighbors; // Contains ids of neighboring process nodes

            // Initially every process nodes are candidate nodes and none are present in the independent set
            isCandidateMap.put(Integer.toString(pid), true);
            isIndependentMap.put(Integer.toString(pid), false);
        }

        /**
         * MIS algorithm implementation
         */
        public void run() {
            try {
                gate.await();
                String currentThread = Thread.currentThread().getName();

                while(isCandidateMap.get(currentThread)) {

                    int randomId = 1 + new Random().nextInt((int) Math.pow(n, 4)); // Assign a random id in range 1...n^4
                    randomIdsMap.put(currentThread, randomId);
                    System.out.println("Current process id: " + Thread.currentThread().getName() + "; Randomly assigned id: " + randomId);

                    // This is done to maintain a synchronous network
                    Thread.sleep(2000); // Wait for other processes to assign a random id

                    int currId = randomIdsMap.get(currentThread);
                    boolean isMax = true;
                    boolean isRoundWasted = false;
                    for(int neighbor : neighbors) {
                        String neighborThreadName = Integer.toString(neighbor);
                        if (isCandidateMap.get(neighborThreadName)) {
                            if(randomIdsMap.get(neighborThreadName) > currId) {
                                isMax = false; // Some other node has id greater than the current node
                                break;
                            } else if(randomIdsMap.get(neighborThreadName) == currId) {
                                isRoundWasted = true; // If two nodes have the same id, this round is wasted
                                break;
                            }
                        }
                    }

                    if(isRoundWasted) { // Invoked when two nodes have the same temp id
                        roundCount++;
                        continue;
                    }

                    // This is done to maintain a synchronous network
                    Thread.sleep(2000); // Wait for other processes to find leaders in their neighborhoods

                    if(isMax) { // Current node has max id among all its neighbors
                        isIndependentMap.put(currentThread, true); // Put the current node in MIS
                        isCandidateMap.put(currentThread, false);
                        for(int neighbor : neighbors) { // Neighbors won't be in MIS
                            isCandidateMap.put(Integer.toString(neighbor), false);
                        }
                    }

                    roundCount++;

                    // This is done to maintain a synchronous network
                    Thread.sleep(2000); // Wait for other processes to update their candidacy for next round
                }
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
            rounds = Math.max(rounds, roundCount);
        }
    }

    /**
     * Prints the MIS process ids
     * @return Set containing MIS process ids
     */
    private static Set<Integer> printMIS() {

        Set<Integer> misSet = new HashSet<>();
        for(Map.Entry<String, Boolean> node : isIndependentMap.entrySet()) {
            if(node.getValue()) {
                misSet.add(Integer.parseInt(node.getKey()));
            }
        }

        return misSet;
    }

    /**
     * Verifies if the MIS constructed is indeed correct
     * @param ids Ids corresponding to each process
     * @param misSet Set containing MIS process ids
     * @param processNeighbors Contains a list of neighboring process ids for each process
     */
    private static boolean verifyMIS(int[] ids, Set<Integer> misSet, List<List<Integer>> processNeighbors) {

        Set<Integer> checkMISSet = new HashSet<>();
        for(int i=0; i<n; i++) {
            for(int j=0; j<processNeighbors.get(i).size(); j++) {

                // Check if neighbors added in MIS
                if(misSet.contains(ids[i]) && misSet.contains(processNeighbors.get(i).get(j))) {
                    return false;
                }

                // checkMISSet is used to make sure that all nodes have been considered for MIS
                if(misSet.contains(ids[i])) {
                    checkMISSet.add(ids[i]);
                    checkMISSet.add(processNeighbors.get(i).get(j));
                }
            }
        }
        return checkMISSet.size() == n;
    }

    public static void main(String[] args) {

        // Pass on input & output file through command line argument
        if(args.length != 2) {
            System.err.println("Error: Input and/or output file not provided in the argument!");
            System.exit(1);
        }

        Scanner sc = null;
        try {
            sc = new Scanner(new File(args[0]));
        } catch (FileNotFoundException e) {
            System.err.println("Error: Input file not found!");
            System.exit(1);
        }

        n = sc.nextInt(); // Total processes

        // Every process is identified through the provided id
        // This id is not used for comparison though, it's just used for identification
        int[] ids = new int[n];
        for(int i=0; i<n; i++) {
            ids[i] = sc.nextInt();
        }

        MIS.ProcessNode[] processNodes = new MIS.ProcessNode[n];

        List<List<Integer>> processNeighbors = new ArrayList<>(); // Used for checking if MIS is indeed correct
        for(int i=0; i<n; i++) {
            List<Integer> currentProcessNeighbors = new ArrayList<>();
            for(int j=0; j<n; j++) {
                if(sc.nextInt() == 1) {
                    currentProcessNeighbors.add(ids[j]); // Add neighbors of current process
                }
            }

            // Create a new instance of a process node
            processNodes[i] = new MIS.ProcessNode(ids[i], currentProcessNeighbors);
            processNeighbors.add(currentProcessNeighbors);
        }

        // Start multi-threading!
        try {
            Thread[] threads = new Thread[processNodes.length];

            gate = new CyclicBarrier(processNodes.length + 1); // Acts as the main thread

            // Assign a thread to each process node
            for (int i = 0; i < processNodes.length; i++) {
                threads[i] = new Thread(processNodes[i]);
                threads[i].setName(Integer.toString(ids[i])); // Process id is the name of the thread
                threads[i].start();
            }

            gate.await();

            // Main thread checks if the other threads have been terminated
            for (int i = 0; i < processNodes.length; i++) {
                while (threads[i].isAlive()) {
                    System.out.println("Computing MIS...");
                    threads[i].join(1000);
                }
            }
            System.out.println();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        System.out.println("Finished computing MIS...Writing results to output file...");

        // Write final output to a file
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(args[1]));

            output.append("Number of rounds (phases): ").append(String.valueOf(rounds));
            output.newLine();

            output.append("MIS has the processes with IDs: ");
            Set<Integer> misSet = printMIS(); // Prints processes in the MIS
            for (Integer misId : misSet) {
                output.append(String.valueOf(misId)).append(" ");
            }
            output.newLine();

            output.append("Checking if  the MIS constructed is correct...");
            output.newLine();
            if(verifyMIS(ids, misSet, processNeighbors)) { // Verify correctness of the MIS
                output.append("The MIS constructed is correct!");
            } else {
                output.append("The MIS constructed is not correct!");
            }

            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}