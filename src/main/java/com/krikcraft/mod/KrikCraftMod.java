package com.krikcraft.mod;

import com.krikcraft.mod.autopilot.AutoPilot;
import com.krikcraft.mod.gui.ServerSelectGui;
import com.krikcraft.mod.keybind.KeyBindings;
import com.krikcraft.mod.scanner.Scanner;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(KrikCraftMod.MOD_ID)
public class KrikCraftMod {

    public static final String MOD_ID = "krikcraft";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    // Текущая выбранная анархия, например "Анархия-308"
    public static String currentAnarchy = "Анархия-101";

    // Включён ли автопилот
    public static boolean autopilotActive = false;

    public KrikCraftMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        KeyBindings.register();
        LOGGER.info("KrikCraft mod загружен!");
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.world == null) return;

        // Обработка нажатий клавиш
        KeyBindings.handleKeyInputs(mc);

        // Тик автопилота
        if (autopilotActive) {
            AutoPilot.tick(mc);
        }

        // Тик сканера (каждые 20 тиков = 1 секунда, чтобы не грузить)
        Scanner.tick(mc);
    }
}
