package com.infamous.dungeons_libraries.client.renderer.gearconfig;

import com.infamous.dungeons_libraries.items.gearconfig.ArmorGear;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class ArmorGearRenderer<T extends ArmorGear> extends GeoArmorRenderer<T> {
    public ArmorGearRenderer() {
        super(new ArmorGearModel<>());
    }

    public ArmorGearRenderer(ArmorGearModel<T> model) {
        super(model);
    }
}