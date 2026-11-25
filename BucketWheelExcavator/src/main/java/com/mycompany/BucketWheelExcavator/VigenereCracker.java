/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.BucketWheelExcavator;

/**
 *
 * uses text from http://gwicks.net/dictionaries.htm
 */
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class VigenereCracker {

    private static final AtomicBoolean stopSearch = new AtomicBoolean(false);
    private static final int MAX_KEY_LENGTH = 20; // Increase cautiously: 26^5 = 11,881,376
    private static final int TOP_RESULTS = 100;
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    private static final class Result {

        final String key;
        final String plaintext;
        final int score;

        Result(String key, String plaintext, int score) {
            this.key = key;
            this.plaintext = plaintext;
            this.score = score;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java VigenereCracker <ciphertext>");
            return;
        }

        printHelp();

        String ciphertext = args[0];
        Set<String> dictionary = loadDictionary(Path.of("src\\main\\resources\\english3.txt"));
        if (dictionary.isEmpty()) {
            System.err.println("english3.txt is missing or empty.");
            return;
        }

        List<Result> results = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(() -> {
            for (int length = 1; length <= MAX_KEY_LENGTH; length++) {
                searchKeys(new char[length], 0, ciphertext, dictionary, results, stopSearch);
                System.out.println("Finished searching keys of length " + length);
            }
            System.out.println("Search complete.");
        });
        executor.shutdown();

        // An empty line prints the next highest-ranked result.
        // Type a key to decrypt directly. Type "quit" to exit.
        Set<String> shownKeys = new HashSet<>();
        Scanner input = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            if (!input.hasNextLine()) {
                break; // End of input
            }

            String line = input.nextLine().trim();
            if (line.equalsIgnoreCase("quit")) {
                break;
            }
            if (line.equalsIgnoreCase("help")) {
                printHelp();
            }

            if (!line.isEmpty()) {
                System.out.println(decrypt(ciphertext, line));
            } else {
                Result next = nextUnshownResult(results, shownKeys);
                if (next == null) {
                    System.out.println("No unshown results yet.");
                } else {
                    shownKeys.add(next.key);
                    System.out.printf("Key: %s | Dictionary words: %d%n%s%n",
                            next.key, next.score, next.plaintext);
                }
            }
        }

        executor.shutdownNow();
    }

    private static void printHelp() {
        System.out.println("""
Commands while running:
    Press Enter   Show the highest-ranked result not shown yet
    Type a key    Decrypt the ciphertext with that key
    Type quit    Stop the search and exit
                                   
The search checks lowercase keys up to 20.
    It reads its newline-separated word list from "src\\main\\resources\\english3.txt"
    in the current directory.""");
    }

    private static Set<String> loadDictionary(Path path) throws IOException {
        Set<String> words = new HashSet<>();
        for (String line : Files.readAllLines(path)) {
            String word = line.trim().toLowerCase(Locale.ROOT);
            if (!word.isEmpty()) {
                words.add(word);
            }
        }
        return words;
    }

    private static void searchKeys(
            char[] key, int position, String ciphertext,
            Set<String> dictionary, List<Result> results, AtomicBoolean stop) {

        if (stop.get() || Thread.currentThread().isInterrupted()) {
            return;
        }

        if (position == key.length) {
            String candidateKey = new String(key);
            String plaintext = decrypt(ciphertext, candidateKey);
            int score = countDictionaryWords(plaintext, dictionary);

            synchronized (results) {
                results.add(new Result(candidateKey, plaintext, score));
                results.sort(Comparator.comparingInt((Result r) -> r.score).reversed());
                if (results.size() > TOP_RESULTS) {
                    results.remove(results.size() - 1);
                }
            }
            return;
        }

        for (int i = 0; i < ALPHABET.length(); i++) {
            key[position] = ALPHABET.charAt(i);
            searchKeys(key, position + 1, ciphertext, dictionary, results, stop);
        }
    }

    private static Result nextUnshownResult(List<Result> results, Set<String> shownKeys) {
        synchronized (results) {
            for (Result result : results) {
                if (!shownKeys.contains(result.key)) {
                    return result;
                }
            }
        }
        return null;
    }

    private static int countDictionaryWords(String text, Set<String> dictionary) {
        int count = 0;
        for (String token : text.toLowerCase(Locale.ROOT).split("[^a-z]+")) {
            if (!token.isEmpty() && dictionary.contains(token)) {
                count++;
            }
        }
        return count;
    }

    private static String decrypt(String ciphertext, String key) {
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Key must not be empty.");
        }

        StringBuilder output = new StringBuilder(ciphertext.length());
        int keyIndex = 0;

        for (char c : ciphertext.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                int shift = Character.toLowerCase(key.charAt(keyIndex % key.length())) - 'a';
                output.append((char) ('a' + (c - 'a' - shift + 26) % 26));
                keyIndex++;
            } else if (c >= 'A' && c <= 'Z') {
                int shift = Character.toLowerCase(key.charAt(keyIndex % key.length())) - 'a';
                output.append((char) ('A' + (c - 'A' - shift + 26) % 26));
                keyIndex++;
            } else {
                output.append(c);
            }
        }
        return output.toString();
    }
}
