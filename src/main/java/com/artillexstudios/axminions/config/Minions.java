package com.artillexstudios.axminions.config;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axminions.minions.MinionType;
import com.artillexstudios.axminions.minions.MinionTypes;
import com.artillexstudios.axminions.utils.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Minions {
    private static final Minions INSTANCE = new Minions();
    private final File minionsDirectory = com.artillexstudios.axminions.utils.FileUtils.PLUGIN_DIRECTORY.resolve("minions").toFile();
    private final ObjectArrayList<File> failedToLoad = new ObjectArrayList<>();
    private final ConcurrentLinkedQueue<CompletableFuture<Void>> loadingMinions = new ConcurrentLinkedQueue<>();
    private final List<File> failedToLoadImmutable = Collections.unmodifiableList(failedToLoad);

    public static void reload() {
        INSTANCE.reload0();
    }

    public void reload0() {
        for (CompletableFuture<Void> loadingMinion : this.loadingMinions) {
            loadingMinion.join();
        }
        this.loadingMinions.clear();

        LogUtils.debug("Reloading minions!");
        for (String minion : MinionTypes.types().toArray(new String[0])) {
            LogUtils.debug("Unregistering {}", minion);
            MinionTypes.unregister(minion);
        }

        this.failedToLoad.clear();
        if (this.minionsDirectory.mkdir()) {
            com.artillexstudios.axminions.utils.FileUtils.copyFromResource("minions");
        }
        Collection<File> files = FileUtils.listFiles(this.minionsDirectory, new String[]{"yaml", "yml"}, true);

        LogUtils.debug("Parsing minion configs {}", String.join(", ", files.stream().map(File::getName).toList()));

        for (File file : files) {
            if (!YamlUtils.suggest(file)) {
                this.failedToLoad.add(file);
                continue;
            }

            String name = file.getName()
                    .replace(".yml", "")
                    .replace(".yaml", "");

            Config config = new Config(file);
            MinionType type = new MinionType(name, config);
            this.loadingMinions.add(type.load().toCompletableFuture());
        }
    }

    public static ConcurrentLinkedQueue<CompletableFuture<Void>> loadingMinions() {
        return INSTANCE.loadingMinions;
    }

    public static List<File> failedToLoad() {
        return INSTANCE.failedToLoadImmutable;
    }
}
