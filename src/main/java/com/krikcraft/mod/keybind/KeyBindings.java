package com.krikcraft.mod.keybind;

import com.krikcraft.mod.gui.ServerSelectGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    private static KeyBinding openMenuKey;

    public static void register() {
        // Ctrl+G — открыть меню выбора анархии
        openMenuKey = new KeyBinding(
                "key.krikcraft.open_menu",
                KeyConflictContext.IN_GAME,
                InputMappings.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "key.categories.krikcraft"
        );
        net.minecraftforge.client.settings.KeyConflictContext.IN_GAME.toString();
        net.minecraftforge.fml.client.registry.ClientRegistry.registerKeyBinding(openMenuKey);
    }

    public static void handleKeyInputs(Minecraft mc) {
        // Проверяем Ctrl+G
        if (openMenuKey.isPressed()) {
            boolean ctrlHeld = InputMappings.isKeyDown(
                    mc.getMainWindow().getHandle(),
                    GLFW.GLFW_KEY_LEFT_CONTROL
            ) || InputMappings.isKeyDown(
                    mc.getMainWindow().getHandle(),
                    GLFW.GLFW_KEY_RIGHT_CONTROL
            );

            if (ctrlHeld) {
                // Открываем GUI выбора анархии
                mc.displayGuiScreen(new ServerSelectGui());
            }
        }
    }
}
