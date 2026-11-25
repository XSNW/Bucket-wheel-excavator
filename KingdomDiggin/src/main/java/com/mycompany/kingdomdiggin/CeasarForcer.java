/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.kingdomdiggin;

/**
 * igtut
 *
 * input = canon, pages found on 1,6,7,17,20,23
 */
public class CeasarForcer {
 
    public static void main(String[] args) {
        String input;
        if (args.length == 1) {
            input = args[0];
        } else {
            input = "abcdefghijklmnopqrstuvwxyz";
        }

        for (int shift = 0; shift < 26; shift++) {
            StringBuilder encoded = new StringBuilder();

            for (char ch : input.toCharArray()) {
                if (ch >= 'a' && ch <= 'z') {
                    char shifted = (char) ('a' + (ch - 'a' + shift) % 26);
                    encoded.append(shifted);
                } else if (ch >= 'A' && ch <= 'Z') {
                    char shifted = (char) ('A' + (ch - 'A' + shift) % 26);
                    encoded.append(shifted);
                } else {
                    encoded.append(ch);
                }
            }

            System.out.printf("%2d: %s%n", shift, encoded);
        }
    }
}
