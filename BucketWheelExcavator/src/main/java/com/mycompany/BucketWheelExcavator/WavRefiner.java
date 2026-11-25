/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.BucketWheelExcavator;

import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 *
 * 
 */
public class WavRefiner {
    
    private static final int FFT_SIZE = 2048;
    private static final int HOP_SIZE = 512;
    private static final int IMAGE_HEIGHT = FFT_SIZE / 2;
    private static final double MIN_DB = -80.0;

    public static void main(String[] args) throws Exception {

        File inputFolder;
        File outputFolder;

        if (args.length == 2) {
            inputFolder = new File(args[0]);
            outputFolder = new File(args[1]);

        } else {
            inputFolder = new File("C:/Users/user/Downloads/Les git diggin/in");
            outputFolder = new File("C:/Users/user/Downloads/Les git diggin/out");
        }

        if (!inputFolder.isDirectory()) {
            throw new IllegalArgumentException("Input folder does not exist: " + inputFolder);
        }
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            throw new IOException("Could not create output folder: " + outputFolder);
        }

        File[] files = inputFolder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".wav"));

        if (files == null || files.length == 0) {
            System.out.println("No WAV files found in " + inputFolder);
            return;
        }

        for (File wavFile : files) {
            try {
                float[] samples = readMonoSamples(wavFile);
                BufferedImage image = makeSpectrogram(samples);
                String outputName = wavFile.getName().replaceAll("(?i)\\.wav$", ".png");
                File outputFile = new File(outputFolder, outputName);
                ImageIO.write(image, "png", outputFile);
                System.out.println("Saved " + outputFile);
            } catch (Exception e) {
                System.err.println("Could not process " + wavFile.getName() + ": "
                        + e.getMessage());
            }
        }
    }

    private static float[] readMonoSamples(File file)
            throws UnsupportedAudioFileException, IOException {
        try (AudioInputStream source = AudioSystem.getAudioInputStream(file)) {
            AudioFormat sourceFormat = source.getFormat();
            AudioFormat pcmFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(),
                    16,
                    sourceFormat.getChannels(),
                    sourceFormat.getChannels() * 2,
                    sourceFormat.getSampleRate(),
                    false
            );

            try (AudioInputStream pcm = AudioSystem.getAudioInputStream(pcmFormat, source)) {
                byte[] bytes = pcm.readAllBytes();
                int channels = pcmFormat.getChannels();
                int frameSize = pcmFormat.getFrameSize();
                int frameCount = bytes.length / frameSize;
                float[] mono = new float[frameCount];

                for (int frame = 0; frame < frameCount; frame++) {
                    double sum = 0;
                    for (int channel = 0; channel < channels; channel++) {
                        int offset = frame * frameSize + channel * 2;
                        int low = bytes[offset] & 0xff;
                        int high = bytes[offset + 1];
                        short sample = (short) ((high << 8) | low);
                        sum += sample / 32768.0;
                    }
                    mono[frame] = (float) (sum / channels);
                }
                return mono;
            }
        }
    }

    private static BufferedImage makeSpectrogram(float[] samples) {
        int columns = Math.max(1, (int) Math.ceil(
                Math.max(0, samples.length - FFT_SIZE) / (double) HOP_SIZE) + 1);

        BufferedImage image = new BufferedImage(
                columns, IMAGE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);

        double[] real = new double[FFT_SIZE];
        double[] imag = new double[FFT_SIZE];
        double[][] magnitudes = new double[columns][IMAGE_HEIGHT];
        double maxMagnitude = 0;

        for (int column = 0; column < columns; column++) {
            int start = column * HOP_SIZE;

            for (int i = 0; i < FFT_SIZE; i++) {
                int sampleIndex = start + i;
                double sample = sampleIndex < samples.length ? samples[sampleIndex] : 0.0;
                // Hann window reduces spectral leakage.
                double window = 0.5 - 0.5 * Math.cos(2.0 * Math.PI * i / (FFT_SIZE - 1));
                real[i] = sample * window;
                imag[i] = 0.0;
            }

            fft(real, imag);

            for (int bin = 0; bin < IMAGE_HEIGHT; bin++) {
                double magnitude = Math.hypot(real[bin], imag[bin]);
                magnitudes[column][bin] = magnitude;
                maxMagnitude = Math.max(maxMagnitude, magnitude);
            }
        }

        if (maxMagnitude == 0) maxMagnitude = 1;

        for (int x = 0; x < columns; x++) {
            for (int bin = 0; bin < IMAGE_HEIGHT; bin++) {
                double db = 20.0 * Math.log10(magnitudes[x][bin] / maxMagnitude + 1e-12);
                double scaled = (db - MIN_DB) / -MIN_DB;
                int gray = (int) Math.round(255 * Math.max(0, Math.min(1, scaled)));

                // Flip vertically so low frequencies appear at the bottom.
                image.getRaster().setSample(x, IMAGE_HEIGHT - 1 - bin, 0, gray);
            }
        }

        return image;
    }

    private static void fft(double[] real, double[] imag) {
        int n = real.length;

        // Bit-reversal permutation.
        for (int i = 1, j = 0; i < n; i++) {
            int bit = n >> 1;
            for (; (j & bit) != 0; bit >>= 1) j ^= bit;
            j ^= bit;

            if (i < j) {
                double temp = real[i];
                real[i] = real[j];
                real[j] = temp;
                temp = imag[i];
                imag[i] = imag[j];
                imag[j] = temp;
            }
        }

        for (int length = 2; length <= n; length <<= 1) {
            double angle = -2.0 * Math.PI / length;
            double stepReal = Math.cos(angle);
            double stepImag = Math.sin(angle);

            for (int start = 0; start < n; start += length) {
                double factorReal = 1.0;
                double factorImag = 0.0;
                int half = length / 2;

                for (int j = 0; j < half; j++) {
                    int even = start + j;
                    int odd = even + half;

                    double oddReal = factorReal * real[odd] - factorImag * imag[odd];
                    double oddImag = factorReal * imag[odd] + factorImag * real[odd];

                    real[odd] = real[even] - oddReal;
                    imag[odd] = imag[even] - oddImag;
                    real[even] += oddReal;
                    imag[even] += oddImag;

                    double nextReal = factorReal * stepReal - factorImag * stepImag;
                    factorImag = factorReal * stepImag + factorImag * stepReal;
                    factorReal = nextReal;
                }
            }
        }
    }
}
