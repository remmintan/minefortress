package org.minefortress.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;

public class NetworkUtils {

    public static byte[] getCompressedBytes(byte[] bytes) {
        byte[] compressedBytes;
        try (
                final var bais = new ByteArrayInputStream(bytes);
                final var os = new DeflaterInputStream(bais, new Deflater(Deflater.BEST_COMPRESSION))
        ) {
            compressedBytes = os.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return compressedBytes;
    }

    public static byte[] getDecompressedBytes(byte[] bytes) {
        byte[] decompressedBytes;
        try (
                final var bais = new ByteArrayInputStream(bytes);
                final var is = new InflaterInputStream(bais)
        ) {
            decompressedBytes = is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return decompressedBytes;
    }

}
