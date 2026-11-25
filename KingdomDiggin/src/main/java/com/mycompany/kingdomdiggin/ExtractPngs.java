/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.kingdomdiggin;

import org.w3c.dom.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.*;
import java.util.Base64;
import java.util.stream.Stream;

/**
 *
 * 
 */
public class ExtractPngs {

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
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".svg"))
                    .forEach(svg -> {
                        try {
                            extractFromSvg(svg, outputFolder);
                        } catch (Exception e) {
                            System.err.println("Could not process " + svg + ": " + e.getMessage());
                        }
                    });
        }
    }

    private static void extractFromSvg(Path svg, Path outputFolder) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        // Prevent the SVG from loading external entities or DTDs.
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        Document doc = factory.newDocumentBuilder().parse(svg.toFile());
        NodeList images = doc.getElementsByTagNameNS("*", "image");
        String baseName = svg.getFileName().toString().replaceFirst("(?i)\\.svg$", "");
        int extracted = 0;

        for (int i = 0; i < images.getLength(); i++) {
            Element image = (Element) images.item(i);
            String href = image.getAttribute("href");

            // Some SVGs use the older xlink:href attribute.
            if (href.isBlank()) {
                href = image.getAttributeNS("http://www.w3.org/1999/xlink", "href");
            }

            String prefix = "data:image/png;base64,";
            if (href.regionMatches(true, 0, prefix, 0, prefix.length())) {
                byte[] png = Base64.getDecoder().decode(href.substring(prefix.length()).trim());
                Path destination = outputFolder.resolve(baseName + "-" + (++extracted) + ".png");
                Files.write(destination, png);
                System.out.println("Extracted " + destination);
            }
        }

        if (extracted == 0) {
            System.out.println("No embedded PNGs found in " + svg);
        }
    }

}
