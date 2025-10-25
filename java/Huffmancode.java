import java.io.*;
import java.util.*;

// Node class for Huffman Tree
class HuffmanNode implements Comparable<HuffmanNode> {
    String data;
    int frequency;
    HuffmanNode left, right;
    
    public HuffmanNode(String data, int frequency) {
        this.data = data;
        this.frequency = frequency;
        this.left = null;
        this.right = null;
    }
    
    @Override
    public int compareTo(HuffmanNode node) {
        return this.frequency - node.frequency;
    }
}

public class HuffmanCoding {
    
    // Build Huffman Tree
    private static HuffmanNode buildHuffmanTree(Map<String, Integer> frequencyMap) {
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();
        
        // Create leaf nodes and add to priority queue
        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            priorityQueue.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }
        
        // Build tree
        while (priorityQueue.size() > 1) {
            HuffmanNode left = priorityQueue.poll();
            HuffmanNode right = priorityQueue.poll();
            
            HuffmanNode parent = new HuffmanNode(null, left.frequency + right.frequency);
            parent.left = left;
            parent.right = right;
            
            priorityQueue.add(parent);
        }
        
        return priorityQueue.poll();
    }
    
    // Generate Huffman codes
    private static void generateCodes(HuffmanNode root, String code, Map<String, String> huffmanCodes) {
        if (root == null) {
            return;
        }
        
        // Leaf node
        if (root.data != null) {
            huffmanCodes.put(root.data, code.isEmpty() ? "0" : code);
            return;
        }
        
        // Traverse left and right
        generateCodes(root.left, code + "0", huffmanCodes);
        generateCodes(root.right, code + "1", huffmanCodes);
    }
    
    // Read CSV file and tokenize by character
    private static List<String> readCSV(String filename) throws IOException {
        List<String> data = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        
        String line = br.readLine(); // Skip header
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 2) {
                // Combine famsize and age (famsize first, then age)
                String token = parts[1].trim() + parts[0].trim();
                
                // TOKENIZE BY CHARACTER - each character becomes a separate token
                for (int i = 0; i < token.length(); i++) {
                    data.add(String.valueOf(token.charAt(i)));
                }
            }
        }
        br.close();
        return data;
    }
    
    // Calculate frequency
    private static Map<String, Integer> calculateFrequency(List<String> data) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String token : data) {
            frequencyMap.put(token, frequencyMap.getOrDefault(token, 0) + 1);
        }
        return frequencyMap;
    }
    
    // Encode data
    private static List<String> encodeData(List<String> data, Map<String, String> huffmanCodes) {
        List<String> encoded = new ArrayList<>();
        for (String token : data) {
            encoded.add(huffmanCodes.get(token));
        }
        return encoded;
    }
    
    public static void main(String[] args) {
        try {
            // Step 1: Read CSV file and tokenize by character
            String filename = "student-data-age-famsize.csv";
            List<String> data = readCSV(filename);
            
            System.out.println("Total characters processed: " + data.size());
            System.out.println("Unique characters: " + new HashSet<>(data).size());
            
            // Step 2: Calculate frequency
            Map<String, Integer> frequencyMap = calculateFrequency(data);
            
            // Step 3: Build Huffman Tree
            HuffmanNode root = buildHuffmanTree(frequencyMap);
            
            // Step 4: Generate Huffman codes
            Map<String, String> huffmanCodes = new HashMap<>();
            generateCodes(root, "", huffmanCodes);
            
            // Step 5: Encode data - one code per line
            List<String> encodedData = encodeData(data, huffmanCodes);
            
            // Step 6: Calculate compression statistics
            // Each character = 8 bits (1 byte)
            long uncompressedBits = data.size() * 8;
            long compressedBits = 0;
            for (String code : encodedData) {
                compressedBits += code.length();
            }
            double compressionRatio = (double) uncompressedBits / compressedBits;
            double spaceSavings = ((double) (uncompressedBits - compressedBits) / uncompressedBits) * 100;
            
            // Output 1: Save dictionary to file
            PrintWriter dictWriter = new PrintWriter(new FileWriter("huffman_dictionary.txt"));
            dictWriter.println("Character,Huffman_Code,Frequency");
            
            // Sort tokens for consistent output
            List<String> sortedTokens = new ArrayList<>(huffmanCodes.keySet());
            Collections.sort(sortedTokens);
            
            for (String token : sortedTokens) {
                dictWriter.println(token + "," + huffmanCodes.get(token) + "," + frequencyMap.get(token));
            }
            dictWriter.close();
            
            // Output 2: Save encoded data to file - ONE CODE PER LINE
            PrintWriter encodedWriter = new PrintWriter(new FileWriter("huffman_encoded.txt"));
            for (String code : encodedData) {
                encodedWriter.println(code);  // Each code on separate line
            }
            encodedWriter.close();
            
            // Output 3: Display and save compression statistics
            System.out.println("\n" + "=".repeat(60));
            System.out.println("HUFFMAN CODING COMPRESSION RESULTS (BY CHARACTER)");
            System.out.println("=".repeat(60));
            System.out.printf("Uncompressed Size: %d bits (%.2f bytes)%n", uncompressedBits, uncompressedBits / 8.0);
            System.out.printf("Compressed Size: %d bits (%.2f bytes)%n", compressedBits, compressedBits / 8.0);
            System.out.printf("Compression Ratio: %.4f:1%n", compressionRatio);
            System.out.printf("Space Savings: %.2f%%%n", spaceSavings);
            System.out.println("=".repeat(60));
            
            // Save statistics to file
            PrintWriter statsWriter = new PrintWriter(new FileWriter("compression_statistics.txt"));
            statsWriter.println("HUFFMAN CODING COMPRESSION RESULTS (BY CHARACTER)");
            statsWriter.println("=".repeat(60));
            statsWriter.printf("Uncompressed Size: %d bits (%.2f bytes)%n", uncompressedBits, uncompressedBits / 8.0);
            statsWriter.printf("Compressed Size: %d bits (%.2f bytes)%n", compressedBits, compressedBits / 8.0);
            statsWriter.printf("Compression Ratio: %.4f:1%n", compressionRatio);
            statsWriter.printf("Space Savings: %.2f%%%n", spaceSavings);
            statsWriter.println("=".repeat(60));
            statsWriter.close();
            
            System.out.println("\nFiles created:");
            System.out.println("1. huffman_dictionary.txt - Dictionary table with codes");
            System.out.println("2. huffman_encoded.txt - Encoded data (one code per line)");
            System.out.println("3. compression_statistics.txt - Compression statistics");
            
            // Show sample of dictionary
            System.out.println("\nSample Dictionary (first 10 entries):");
            System.out.println("-".repeat(40));
            int count = 0;
            for (String token : sortedTokens) {
                if (count >= 10) break;
                System.out.printf("'%s' -> %-12s (freq: %d)%n", token, huffmanCodes.get(token), frequencyMap.get(token));
                count++;
            }
            
            // Show sample of encoded data
            System.out.println("\nSample Encoded Data (first 10 lines):");
            System.out.println("-".repeat(40));
            for (int i = 0; i < Math.min(10, encodedData.size()); i++) {
                System.out.println(encodedData.get(i));
            }
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
