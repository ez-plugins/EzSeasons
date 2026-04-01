package com.skyblockexp.lifesteal.seasons.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TranslationCoverageUnitTest {

    private static final Path MESSAGES_DIRECTORY = Path.of("src/main/resources/messages");
    private static final String SOURCE_LANGUAGE_FILE = "en.yml";

    @Test
    void allTranslationsIncludeEveryEnglishMessageKey() throws IOException {
        Set<String> sourceKeys = loadLeafKeys(MESSAGES_DIRECTORY.resolve(SOURCE_LANGUAGE_FILE));
        assertFalse(sourceKeys.isEmpty(), "Expected English translation file to define at least one message key.");

        List<Path> translationFiles = Files.list(MESSAGES_DIRECTORY)
                .filter(path -> path.getFileName().toString().endsWith(".yml"))
                .filter(path -> !path.getFileName().toString().equals(SOURCE_LANGUAGE_FILE))
                .sorted()
                .toList();

        assertFalse(translationFiles.isEmpty(), "Expected at least one non-English translation file.");

        for (Path translationFile : translationFiles) {
            Set<String> translationKeys = loadLeafKeys(translationFile);
            Set<String> missingKeys = sourceKeys.stream()
                    .filter(key -> !translationKeys.contains(key))
                    .collect(Collectors.toCollection(java.util.TreeSet::new));

            assertTrue(
                    missingKeys.isEmpty(),
                    () -> String.format(
                            "Translation file '%s' is missing %d key(s): %s",
                            translationFile.getFileName(),
                            missingKeys.size(),
                            String.join(", ", missingKeys))
            );
        }
    }

    private static Set<String> loadLeafKeys(Path filePath) throws IOException {
        YamlConfiguration configuration = new YamlConfiguration();
        try (Reader reader = new InputStreamReader(Files.newInputStream(filePath), StandardCharsets.UTF_8)) {
            configuration.load(reader);
        } catch (Exception exception) {
            throw new IOException("Failed to parse YAML file: " + filePath, exception);
        }

        return configuration.getKeys(true).stream()
                .filter(key -> !configuration.isConfigurationSection(key))
                .collect(Collectors.toCollection(java.util.TreeSet::new));
    }
}
