import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class TestCase {
    public static void main(String[] args) {
        Random rand = new Random();
        int numNodes = rand.nextInt(100) + 1; // Generate a random number of nodes between 1 and 10
        int[] processIds = new int[numNodes];
        for (int i = 0; i < numNodes; i++) {
            processIds[i] = i;
        }
        int[][] adjacencyMatrix = generateConnectedGraph(numNodes, rand);

        try {
            FileWriter writer = new FileWriter("input1.txt");
            writer.write(numNodes + "\n");
            for (int i = 0; i < numNodes; i++) {
                writer.write(processIds[i] + " ");
            }
            writer.write("\n");
            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numNodes; j++) {
                    writer.write(adjacencyMatrix[i][j] + " ");
                }
                writer.write("\n");
            }
            writer.close();
            System.out.println("Test case written to input.txt");
        } catch (IOException e) {
            System.out.println("An error occurred while writing the test case to input.txt");
            e.printStackTrace();
        }
    }

    private static int[][] generateConnectedGraph(int numNodes, Random rand) {
        int[][] adjacencyMatrix = new int[numNodes][numNodes];
        List<Integer> availableNodes = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            availableNodes.add(i);
        }
        int currentNode = availableNodes.remove(0);
        while (!availableNodes.isEmpty()) {
            int neighbor = availableNodes.remove(rand.nextInt(availableNodes.size()));
            adjacencyMatrix[currentNode][neighbor] = 1;
            adjacencyMatrix[neighbor][currentNode] = 1;
            currentNode = neighbor;
        }
        return adjacencyMatrix;
}
}