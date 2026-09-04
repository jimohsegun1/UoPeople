import java.util.Scanner;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TextAnalysisTool performs character and word analysis on user input.
 * 
 * @author Jeremiah Segun Jimoh
 */
public class TextAnalysisTool {

    /**
     * The main method controls program flow, UI, and exception handling.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            System.out.println("Enter a paragraph or lengthy text:");
            String text = scanner.nextLine();
            
            // Input Validation
            while (text.trim().isEmpty()) {
                System.out.println("Input cannot be empty. Try again:");
                text = scanner.nextLine();
            }

            // Task 1 & 2: Character and Word Count
            System.out.println("\n--- Analysis Results ---");
            System.out.println("Total characters: " + getCharacterCount(text));
            System.out.println("Total words: " + getWordCount(text));
            
            // Task 3: Most Common Character
            System.out.println("Most common character: " 
                + getMostCommonCharacter(text));
            
            // Task 4: Character Frequency
            System.out.println("\nEnter a character to check its frequency:");
            String charInput = scanner.nextLine();
            while (charInput.isEmpty()) {
                System.out.println("Please enter a valid character:");
                charInput = scanner.nextLine();
            }
            char searchChar = charInput.charAt(0);
            System.out.println("Frequency of '" + searchChar + "': " 
                + getCharacterFrequency(text, searchChar));
            
            // Task 5: Word Frequency
            System.out.println("\nEnter a word to check its frequency:");
            String searchWord = scanner.nextLine();
            System.out.println("Frequency of \"" + searchWord + "\": " 
                + getWordFrequency(text, searchWord));
            
            // Task 6: Unique Words
            System.out.println("Total unique words: " 
                + getUniqueWordCount(text));

        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " 
                + e.getMessage());
        } finally {
            scanner.close();
            System.out.println("\nAnalysis complete. Scanner closed.");
        }
    }

    /**
     * Calculates the total number of characters in the input text.
     * 
     * @param text The input string
     * @return The total character count
     */
    public static int getCharacterCount(String text) {
        return text.length();
    }

    /**
     * Calculates the total number of words in the input text.
     * Words are assumed to be separated by spaces.
     * 
     * @param text The input string
     * @return The total word count
     */
    public static int getWordCount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    /**
     * Finds the most common character in the text (excluding spaces).
     * 
     * @param text The input string
     * @return The most frequently occurring character
     */
    public static char getMostCommonCharacter(String text) {
        Map<Character, Integer> charCounts = new HashMap<>();
        char mostCommon = ' ';
        int maxCount = 0;

        for (char c : text.toCharArray()) {
            if (c != ' ') {
                int count = charCounts.getOrDefault(c, 0) + 1;
                charCounts.put(c, count);
                if (count > maxCount) {
                    maxCount = count;
                    mostCommon = c;
                }
            }
        }
        return mostCommon;
    }

    /**
     * Calculates the frequency of a specific character (case-insensitive).
     * 
     * @param text The input string
     * @param target The character to search for
     * @return The number of times the character appears
     */
    public static int getCharacterFrequency(String text, char target) {
        int count = 0;
        char lowerTarget = Character.toLowerCase(target);
        for (char c : text.toCharArray()) {
            if (Character.toLowerCase(c) == lowerTarget) {
                count++;
            }
        }
        return count;
    }

    /**
     * Calculates the frequency of a specific word (case-insensitive).
     * 
     * @param text The input string
     * @param targetWord The word to search for
     * @return The number of times the word appears
     */
    public static int getWordFrequency(String text, String targetWord) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] words = text.trim().split("\\s+");
        int count = 0;
        String lowerTarget = targetWord.toLowerCase().replaceAll("[^a-z]", "");

        for (String word : words) {
            String cleanWord = word.toLowerCase().replaceAll("[^a-z]", "");
            if (cleanWord.equals(lowerTarget)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Calculates the number of unique words in the text (case-insensitive).
     * 
     * @param text The input string
     * @return The count of unique words
     */
    public static int getUniqueWordCount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] words = text.trim().split("\\s+");
        Set<String> uniqueWords = new HashSet<>();

        for (String word : words) {
            String cleanWord = word.toLowerCase().replaceAll("[^a-z]", "");
            if (!cleanWord.isEmpty()) {
                uniqueWords.add(cleanWord);
            }
        }
        return uniqueWords.size();
    }
}