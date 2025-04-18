package com.artillexstudios.axminions.config;

import com.artillexstudios.axapi.libs.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axminions.AxMinionsPlugin;
import com.artillexstudios.axminions.minions.skins.Skin;
import com.artillexstudios.axminions.minions.skins.SkinRegistry;
import com.artillexstudios.axminions.utils.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

public final class Skins {
    private static final Skins INSTANCE = new Skins();
    private com.artillexstudios.axapi.config.Config config = null;

    public static boolean reload() {
        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        File file = FileUtils.PLUGIN_DIRECTORY.resolve("skins.yml").toFile();
        if (file.exists()) {
            if (!YamlUtils.suggest(file)) {
                return false;
            }
        }

        if (this.config != null) {
            this.config.reload();
        } else {
            this.config = new com.artillexstudios.axapi.config.Config(file, AxMinionsPlugin.instance().getResource("skins.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
        }

        SkinRegistry.clear();
        for (String route : this.config.getBackingDocument().getRoutesAsStrings(false)) {
            List<Map<Object, Object>> mapList = this.config.getMapList(route);
            Skin skin = Skin.of(route, mapList);
            SkinRegistry.register(skin);
        }

        return true;
    }
}
