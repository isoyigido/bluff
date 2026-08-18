package io.github.isoyigido.bluff.gui.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Audio {
    private static final Logger logger = LoggerFactory.getLogger(Audio.class);

    public static Optional<Audio> fromResources(String path) {
        Objects.requireNonNull(path, "Path to audio file cannot be null.");

        if (path.isEmpty()) throw new IllegalArgumentException("Path to audio file cannot be empty.");

        if (path.charAt(0) != '/') path = '/' + path;

        URL resource = Audio.class.getResource(path);

        if (resource == null) {
            logger.warn("Cannot find audio file in resources. path={}", path);
            return Optional.empty();
        }

        try (AudioInputStream baseStream = AudioSystem.getAudioInputStream(resource)) {
            try (ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream()) {
                AudioFormat format = baseStream.getFormat();
                long frameLength = baseStream.getFrameLength();

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = baseStream.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, bytesRead);
                }

                byte[] audioBytes = byteBuffer.toByteArray();

                return Optional.of(new Audio(audioBytes, format, frameLength));
            }
        } catch (Exception e) {
            logger.error("Encountered an error while reading audio file. path={}", path, e);
            return Optional.empty();
        }
    }

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private final byte[] bytes;
    private final AudioFormat audioFormat;
    private final long frameLength;

    private Audio(byte[] bytes, AudioFormat audioFormat, long frameLength) {
        this.bytes = bytes;
        this.audioFormat = audioFormat;
        this.frameLength = frameLength;
    }

    public void play() {
        Audio.executor.submit(() -> {
            try (ByteArrayInputStream memoryInputStream = new ByteArrayInputStream(this.bytes)) {
                try (AudioInputStream audioStream = new AudioInputStream(memoryInputStream, this.audioFormat, this.frameLength)) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioStream);
                    clip.setFramePosition(0);
                    clip.start();
                }
            } catch (Exception e) {
                Audio.logger.error("Encountered an error while playing audio.", e);
            }
        });
    }
}