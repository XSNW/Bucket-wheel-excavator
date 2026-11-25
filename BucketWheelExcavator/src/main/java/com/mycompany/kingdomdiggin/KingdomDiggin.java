/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.kingdomdiggin;

import java.util.Scanner;

public class KingdomDiggin {

    static int selectedAction = 0;

    public static void main(String[] args) throws Exception {

        print("""
              Welcome to the bucket wheel excavator. Please pick an action by entering its number:
              1: CeasarForcer (takes a string and spits out all ceasar variations of it)
              2: VigenereCracker (used to crack vigenere cyphertext)
              3: ExtractPngs (takes svgs and finds and extracts any embedded pngs (embedded as b64, does not work on embedded as reference))
              4: PngRefiner (takes pngs and normalizes brightness to see darkened or lightened images)
              5: WavRefiner (takes wavs and produces spectrograms of them
              6: Unzip (unzips a bunch of zipped folders into one folder, naming them to prevent collisions)
              """);

        boolean succeeded = false;
        Scanner scanner = new Scanner(System.in);
        while (!succeeded) {
            try {
                selectedAction = Integer.parseInt(scanner.nextLine());
                succeeded = true;
            } catch (Exception e) {
            }
        }
        try {
            switch (selectedAction) {
                case 1 -> {
                    print("CeasarForcer selected. Please enter a string to decode.");
                    String[] a = new String[1];
                    a[0] = scanner.nextLine();
                    CeasarForcer.main(a);
                }
                case 2 -> {
                    print("VigenereCracker selected. Please enter a string to decode.");
                    String[] a = new String[1];
                    a[0] = scanner.nextLine();
                    VigenereCracker.main(a);
                }
                case 3 -> {
                    print("ExtractPngs selected. Please enter a file path to take from.");
                    String[] a = new String[2];
                    a[0] = scanner.nextLine();
                    print("Please enter a file path to store to.");
                    a[1] = scanner.nextLine();
                    ExtractPngs.main(a);
                }
                case 4 -> {
                    print("PngRefiner selected. Please enter a file path to take from.");
                    String[] a = new String[2];
                    a[0] = scanner.nextLine();
                    print("Please enter a file path to store to.");
                    a[1] = scanner.nextLine();
                    PngRefiner.main(a);
                }
                case 5 -> {
                    print("WavRefiner selected. Please enter a file path to take from.");
                    String[] a = new String[2];
                    a[0] = scanner.nextLine();
                    print("Please enter a file path to store to.");
                    a[1] = scanner.nextLine();
                    WavRefiner.main(a);
                }
                case 6 -> {
                    print("Unzip selected. Please enter a file path to take from.");
                    String[] a = new String[2];
                    a[0] = scanner.nextLine();
                    print("Please enter a file path to store to.");
                    a[1] = scanner.nextLine();
                    Unzip.main(a);
                }
                default ->
                    print("Selection index was out of bounds. Please retry running the program.");

            }
        } catch (Exception ex) {
            print("The program ran into an exception and crashed \n"
                    + "Exception:");
            print(ex.toString());
        }
    }

    private static void print(String args) {
        System.out.println(args);
    }
}
