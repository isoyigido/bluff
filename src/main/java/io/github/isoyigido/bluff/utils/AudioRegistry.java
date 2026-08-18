package io.github.isoyigido.bluff.utils;

import io.github.isoyigido.bluff.gui.audio.Audio;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class AudioRegistry {
    private AudioRegistry() {
        throw new UnsupportedOperationException("Registry class cannot be instantiated.");
    }

    private static final Map<String, Audio> registry = new ConcurrentHashMap<>(4);

    public static void register(String name, String path) {
        Objects.requireNonNull(name, "Audio name cannot be null.");
        Objects.requireNonNull(path, "Path to audio file cannot be null.");

        Audio.fromResources(path).ifPresent(audio -> registry.put(name, audio));
    }

    public static Optional<Audio> get(String name) {
        Objects.requireNonNull(name, "Audio name cannot be null.");

        return Optional.ofNullable(AudioRegistry.registry.get(name));
    }
}
