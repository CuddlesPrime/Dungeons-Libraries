package com.infamous.dungeons_libraries.datagen;

import com.infamous.dungeons_libraries.DungeonsLibraries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput  output, String locale) {
        super(output, DungeonsLibraries.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        addConfigOptions();
        addTips();
    }

    private void addTips() {
    }

    private void addConfigOptions() {
    }
}
