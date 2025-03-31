package com.infamous.dungeons_libraries.client.artifactBar;

import com.infamous.dungeons_libraries.client.gui.elementconfig.GuiElementConfig;
import com.infamous.dungeons_libraries.client.gui.elementconfig.GuiElementConfigRegistry;
import com.infamous.dungeons_libraries.integration.curios.client.CuriosKeyBindings;
import com.infamous.dungeons_libraries.items.artifacts.ArtifactItem;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;

import static com.infamous.dungeons_libraries.DungeonsLibraries.MODID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID)
public class ArtifactsBarRender {
    private static final ResourceLocation ARTIFACT_BAR_RESOURCE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/artifact_bar.png");
    private static final ResourceLocation GUI_ICONS_LOCATION = ResourceLocation.parse("textures/gui/icons.png");

    @SubscribeEvent
    public static void displayArtifactBar(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay().equals(VanillaGuiOverlay.HOTBAR.type()) && Minecraft.getInstance().getCameraEntity() instanceof Player renderPlayer) {
            if (renderPlayer == null) return;
            GuiElementConfig guiElementConfig = GuiElementConfigRegistry.getConfig(ResourceLocation.fromNamespaceAndPath(MODID, "artifact_bar"));
            if (guiElementConfig.isHidden()) return;


            Window sr = event.getWindow();
            int scaledWidth = sr.getGuiScaledWidth();
            int scaledHeight = sr.getGuiScaledHeight();

            int x = guiElementConfig.getXPosition(scaledWidth);
            int y = guiElementConfig.getYPosition(scaledHeight);

            CuriosApi.getCuriosInventory(renderPlayer).ifPresent(iCuriosItemHandler -> {
                renderBar(event.getGuiGraphics(), renderPlayer, x, y, iCuriosItemHandler);
            });

            RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
        }


    }

    private static void renderBar(GuiGraphics guiGraphics, Player renderPlayer, int x, int y, ICuriosItemHandler iCuriosItemHandler) {
        Optional<ICurioStacksHandler> artifactStackHandler = iCuriosItemHandler.getStacksHandler("artifact");
        if (artifactStackHandler.isPresent()) {
            IDynamicStackHandler stacks = artifactStackHandler.get().getStacks();
            if(noArtifactEquipped(stacks)) return;
            int slots = stacks.getSlots();
            renderSlotBg(guiGraphics, x, y, slots);
            for (int slot = 0; slot < slots; slot++) {
                ItemStack artifact = stacks.getStackInSlot(slot);
                if (!artifact.isEmpty() && artifact.getItem() instanceof ArtifactItem) {
                    int xPos = x + slot * 20 + 3;
                    int yPos = y + 3;
                    renderSlot(guiGraphics, xPos, yPos, renderPlayer, artifact);
                }
                renderSlotKeybind(guiGraphics, x, y, slot);
            }
        }
    }

    private static boolean noArtifactEquipped(IDynamicStackHandler stacks) {
        int slots = stacks.getSlots();
        for (int slot = 0; slot < slots; slot++) {
            ItemStack artifact = stacks.getStackInSlot(slot);
            if (!artifact.isEmpty() && artifact.getItem() instanceof ArtifactItem) {
                return false;
            }
        }
        return true;
    }

    private static void renderSlot(GuiGraphics guiGraphics, int xPos, int yPos, Player renderPlayer, ItemStack artifactStack) {
        if (!artifactStack.isEmpty()) {
            float f = (float) artifactStack.getPopTime() - 0;
            if (f > 0.0F) {
                float f1 = 1.0F + f / 5.0F;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate((float) (xPos + 8), (float) (yPos + 12), 0.0F);
                guiGraphics.pose().scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                guiGraphics.pose().translate((float) (-(xPos + 8)), (float) (-(yPos + 12)), 0.0F);
                RenderSystem.applyModelViewMatrix();
            }

            guiGraphics.renderItem(renderPlayer, artifactStack, xPos, yPos, 1);
            if (f > 0.0F) {
              guiGraphics.pose().popPose();
            }

            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, artifactStack, xPos, yPos);
        }
    }

    private static void renderSlotBg(GuiGraphics guiGraphics, int xPos, int yPos, int slots) {
        RenderSystem.setShaderTexture(0, ARTIFACT_BAR_RESOURCE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(GUI_ICONS_LOCATION, xPos, yPos, 0, 0, 62, 22, 62, 22);
        RenderSystem.disableBlend();
    }

    private static void renderSlotKeybind(GuiGraphics guiGraphics, int x, int y, int slot) {
        String keybind = "";
        if (slot == 0) {
            KeyMapping keyMapping = CuriosKeyBindings.activateArtifact1;
            keybind = getString(keyMapping);
        } else if (slot == 1) {
            KeyMapping keyMapping = CuriosKeyBindings.activateArtifact2;
            keybind = getString(keyMapping);
        } else if (slot == 2) {
            KeyMapping keyMapping = CuriosKeyBindings.activateArtifact3;
            keybind = getString(keyMapping);
        }
        int keybindWidth = Minecraft.getInstance().font.width(keybind);
        int xPosition = x + 1 + slot * 20 + 18 - keybindWidth;
        int yPosition = y + 3;
        guiGraphics.drawString(Minecraft.getInstance().font, keybind, xPosition, yPosition, 0xFFFFFF);
    }

    private static String getString(KeyMapping keyMapping) {
//        return keyMapping.getKeyModifier().getCombinedName(keyMapping.getKey(), () -> keyMapping.getKey().getDisplayName()).getString();
        return keyMapping.getKey().getDisplayName().getString();
    }

}
