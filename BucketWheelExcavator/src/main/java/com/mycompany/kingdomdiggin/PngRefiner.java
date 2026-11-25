/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.kingdomdiggin;
 import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.stream.Stream;
/**
 *
 * 
 */
public class PngRefiner {
    
   

    public static void main(String[] args) throws Exception {

         Path inputFolder;
        Path outputFolder;

        if (args.length == 2) {
            inputFolder = Path.of(args[0]);
            outputFolder = Path.of(args[1]);

        } else {
            inputFolder = Path.of("C:/Users/user/Downloads/Les git diggin/in");
            outputFolder = Path.of("C:/Users/user/Downloads/Les git diggin/out");
        }
        Files.createDirectories(outputFolder);

        try (Stream<Path> files = Files.walk(inputFolder)) {
            files.filter(Files::isRegularFile)
                 .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".png"))
                 .forEach(file -> {
                     try {
                         Path relative = inputFolder.relativize(file);
                         Path destination = outputFolder.resolve(relative);
                         Files.createDirectories(destination.getParent());
                         stretch(file, destination);
                         System.out.println("Wrote " + destination);
                     } catch (Exception e) {
                         System.err.println("Could not process " + file + ": " + e.getMessage());
                     }
                 });
        }
    }

private static void stretch(Path source, Path destination) throws Exception {
    BufferedImage input = ImageIO.read(source.toFile());
    if (input == null) {
        throw new IllegalArgumentException("File could not be read as an image");
    }

    // Average RGB channel value across visible pixels.
    long channelTotal = 0;
    long channelCount = 0;

    for (int y = 0; y < input.getHeight(); y++) {
        for (int x = 0; x < input.getWidth(); x++) {
            int argb = input.getRGB(x, y);
            int alpha = (argb >>> 24) & 0xFF;
            if (alpha == 0) continue;

            channelTotal += (argb >>> 16) & 0xFF; // red
            channelTotal += (argb >>> 8) & 0xFF;  // green
            channelTotal += argb & 0xFF;          // blue
            channelCount += 3;
        }
    }

    BufferedImage output =
            new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);

    if (channelCount == 0) {
        // Entire image is transparent.
        ImageIO.write(input, "png", destination.toFile());
        return;
    }

    double average = (double) channelTotal / channelCount;
    double targetAverage = 127.5; // Half of the 0–255 channel range
    double factor = average == 0 ? 1.0 : targetAverage / average;

    for (int y = 0; y < input.getHeight(); y++) {
        for (int x = 0; x < input.getWidth(); x++) {
            int argb = input.getRGB(x, y);
            int alpha = (argb >>> 24) & 0xFF;

            int red   = (argb >>> 16) & 0xFF;
            int green = (argb >>> 8)  & 0xFF;
            int blue  = argb & 0xFF;

            red   = clamp((int) Math.round(red * factor));
            green = clamp((int) Math.round(green * factor));
            blue  = clamp((int) Math.round(blue * factor));

            output.setRGB(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
        }
    }

    ImageIO.write(output, "png", destination.toFile());
}

private static int clamp(int value) {
    return Math.max(0, Math.min(255, value));
}

    private static int scale(int value, int min, int max) {
        return (value - min) * 255 / (max - min);
    }

}
