package com.skyblockexp.lifesteal.seasons.resources;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class ResourceYamlValidationUnitTest {

    @Test
    void allResourceYmlFilesShouldBeValidYaml() throws IOException {
        Path resourceDirectory = Path.of("src", "main", "resources");

        try (Stream<Path> paths = Files.walk(resourceDirectory)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml"))
                    .forEach(path -> assertDoesNotThrow(() -> parseYaml(path),
                            () -> "Failed to parse YAML resource: " + path));
        }
    }

    private void parseYaml(Path yamlFilePath) throws IOException, InvalidConfigurationException {
        String yamlContent = Files.readString(yamlFilePath);
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.loadFromString(yamlContent);
    }
}
