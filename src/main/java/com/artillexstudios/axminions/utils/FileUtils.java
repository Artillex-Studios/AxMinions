package com.artillexstudios.axminions.utils;

import com.artillexstudios.axminions.AxMinionsPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class FileUtils {
    public static final Path PLUGIN_DIRECTORY = AxMinionsPlugin.getInstance().getDataFolder().toPath();
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    public static void copyFromResource(@NotNull String path) {
        try (ZipFile zip = new ZipFile(Paths.get(AxMinionsPlugin.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toFile())) {
            for (Iterator<? extends ZipEntry> it = zip.entries().asIterator(); it.hasNext(); ) {
                ZipEntry entry = it.next();
                if (!entry.getName().startsWith(path + "/")) continue;
                if (!entry.getName().endsWith(".yaml") && !entry.getName().endsWith(".yml")) continue;

                InputStream resource = AxMinionsPlugin.getInstance().getResource(entry.getName());
                if (resource == null) {
                    log.error("Could not find file {} in plugin's assets!", entry.getName());
                    continue;
                }
                Path to = PLUGIN_DIRECTORY.resolve(entry.getName());
                if (to.toFile().exists()) {
                    continue;
                }

                Files.createDirectories(to.getParent());
                Files.copy(resource, to);
            }
        } catch (IOException | URISyntaxException exception) {
            log.error("An unexpected error occurred while extracting directory {} from plugin's assets!", path, exception);
        }
    }
}