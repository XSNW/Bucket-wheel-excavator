/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.kingdomdiggin;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
/**
 *
 * 
 */
public class Unzip {
    

    public static void main(String[] args) throws IOException {
          Path inputDir;
        Path outputDir;

        if (args.length == 2) {
            inputDir = Path.of(args[0]);
            outputDir = Path.of(args[1]);

        } else {
            inputDir = Path.of("C:/Users/user/Downloads/Les git diggin/in");
            outputDir = Path.of("C:/Users/user/Downloads/Les git diggin/out");
        }

        Files.createDirectories(outputDir);

        try (DirectoryStream<Path> zips =
                     Files.newDirectoryStream(inputDir, "*.zip")) {
            for (Path zipPath : zips) {
                unzip(zipPath, outputDir);
            }
        }
    }

    private static void unzip(Path zipPath, Path outputDir) throws IOException {
        String zipName = zipPath.getFileName().toString();
        String prefix = zipName.substring(0, zipName.length() - 4);

        Set<String> namesCreatedFromThisZip = new HashSet<>();

        try (InputStream fileIn = Files.newInputStream(zipPath);
             ZipInputStream zipIn = new ZipInputStream(fileIn)) {

            ZipEntry entry;
            byte[] buffer = new byte[8192];

            while ((entry = zipIn.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    // Use only the final path component, not folders inside the ZIP.
                    String entryName = entry.getName().replace('\\', '/');
                    String originalName =
                            entryName.substring(entryName.lastIndexOf('/') + 1);

                    if (!originalName.isEmpty()) {
                        String baseName = prefix + "_" + originalName;
                        String outputName = baseName;
                        int counter = 1;

                        while (namesCreatedFromThisZip.contains(outputName)
                                || Files.exists(outputDir.resolve(outputName))) {
                            outputName = addCounter(baseName, counter++);
                        }

                        Path outputFile = outputDir.resolve(outputName);
                        namesCreatedFromThisZip.add(outputName);

                        try (OutputStream out = Files.newOutputStream(outputFile)) {
                            int bytesRead;
                            while ((bytesRead = zipIn.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                        }
                    }
                }
                zipIn.closeEntry();
            }
        }
    }

    private static String addCounter(String filename, int counter) {
        int dot = filename.lastIndexOf('.');
        if (dot > 0) {
            return filename.substring(0, dot) + "_" + counter
                    + filename.substring(dot);
        }
        return filename + "_" + counter;
    }
}
