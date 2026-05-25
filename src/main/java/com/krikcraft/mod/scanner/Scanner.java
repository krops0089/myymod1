package com.krikcraft.mod.scanner;

import com.krikcraft.mod.KrikCraftMod;
import com.krikcraft.mod.discord.DiscordWebhook;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Сканер блоков и мобов вокруг игрока.
 * Запускается каждые 20 тиков (1 секунда).
 */
public class Scanner {

    // Радиус сканирования блоков
    private static final int BLOCK_RADIUS = 230;
    // Радиус сканирования мобов/игроков
    private static final int ENTITY_RADIUS = 230;

    // Уже найденные позиции блоков (не спамим)
    private static final Set<BlockPos> foundBlocks = new HashSet<>();
    // Уже найденные Entity по UUID (не спамим)
    private static final Set<java.util.UUID> foundEntities = new HashSet<>();

    private static int tickCounter = 0;

    public static void tick(Minecraft mc) {
        if (mc.player == null || mc.world == null) return;

        tickCounter++;
        if (tickCounter < 20) return; // раз в секунду
        tickCounter = 0;

        scanBlocks(mc);
        scanEntities(mc);
    }

    // ─── БЛОКИ ──────────────────────────────────────────────────────────────

    private static void scanBlocks(Minecraft mc) {
        BlockPos playerPos = mc.player.getPosition();

        for (int x = -BLOCK_RADIUS; x <= BLOCK_RADIUS; x++) {
            for (int y = -240; y <= 0; y++) {  // сканируем 240 блоков вниз от позиции игрока
                for (int z = -BLOCK_RADIUS; z <= BLOCK_RADIUS; z++) {

                    BlockPos pos = playerPos.add(x, y, z);
                    Block block = mc.world.getBlockState(pos).getBlock();

                    String blockName = getBlockName(block);
                    if (blockName == null) continue;
                    if (foundBlocks.contains(pos)) continue;

                    foundBlocks.add(pos);
                    String message = formatFoundMessage(blockName, pos);
                    notifyPlayer(mc, message);
                    DiscordWebhook.send(KrikCraftMod.currentAnarchy, blockName, pos.getX(), pos.getY(), pos.getZ());
                }
            }
        }
    }

    private static String getBlockName(Block block) {
        if (block == Blocks.SPAWNER)          return "🏴 Спавнер мобов";
        if (block == Blocks.STRUCTURE_BLOCK)  return "🔷 Структурный блок";
        // Блок-дамагер (пазл) — в 1.16.5 это Jigsaw Block
        if (block == Blocks.JIGSAW)           return "🧩 Блок пазл (Jigsaw)";
        if (block == Blocks.COMMAND_BLOCK)    return "⚙️ Командный блок";
        if (block == Blocks.CHAIN_COMMAND_BLOCK) return "⚙️ Командный блок (цепной)";
        if (block == Blocks.REPEATING_COMMAND_BLOCK) return "⚙️ Командный блок (повторяющийся)";
        return null;
    }

    // ─── МОБЫ И ИГРОКИ ──────────────────────────────────────────────────────

    private static void scanEntities(Minecraft mc) {
        PlayerEntity self = mc.player;
        double px = self.getPosX(), py = self.getPosY(), pz = self.getPosZ();

        AxisAlignedBB box = new AxisAlignedBB(
                px - ENTITY_RADIUS, py - ENTITY_RADIUS, pz - ENTITY_RADIUS,
                px + ENTITY_RADIUS, py + ENTITY_RADIUS, pz + ENTITY_RADIUS
        );

        List<Entity> entities = mc.world.getEntitiesInAABBexcluding(self, box, e -> true);

        for (Entity entity : entities) {
            if (foundEntities.contains(entity.getUniqueID())) continue;

            String name = getEntityName(entity, self);
            if (name == null) continue;

            foundEntities.add(entity.getUniqueID());

            BlockPos pos = entity.getPosition();
            String message = formatFoundMessage(name, pos);
            notifyPlayer(mc, message);
            DiscordWebhook.send(KrikCraftMod.currentAnarchy, name, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    private static String getEntityName(Entity entity, PlayerEntity self) {
        // Игроки (кроме себя)
        if (entity instanceof PlayerEntity) {
            PlayerEntity p = (PlayerEntity) entity;
            if (p.getUniqueID().equals(self.getUniqueID())) return null;
            return "👤 Игрок: " + p.getGameProfile().getName();
        }

        if (entity instanceof CreeperEntity) {
            CreeperEntity creeper = (CreeperEntity) entity;
            return creeper.getPowered() ? "⚡ Заряженный крипер" : null; // только заряженные
        }
        if (entity instanceof GuardianEntity && !(entity instanceof ElderGuardianEntity)) {
            return "🐟 Страж";
        }
        if (entity instanceof ElderGuardianEntity) {
            return "🐋 Зимогор (Elder Guardian)";
        }
        if (entity instanceof MagmaCubeEntity) {
            MagmaCubeEntity cube = (MagmaCubeEntity) entity;
            if (cube.getSlimeSize() >= 3) return "🔥 Магмовый куб (большой)";
            return null; // маленькие не репортим
        }
        if (entity instanceof EndermanEntity)  return "🌑 Эндермен";
        if (entity instanceof ShulkerEntity)   return "📦 Шалкер";
        if (entity instanceof BlazeEntity)     return "🔥 Ифрит (Blaze)";
        if (entity instanceof VillagerEntity)  return "🧑‍🌾 Житель";

        return null;
    }

    // ─── ВСПОМОГАТЕЛЬНЫЕ ────────────────────────────────────────────────────

    private static String formatFoundMessage(String what, BlockPos pos) {
        return "§e[KrikCraft] §fНайдено: §b" + what
                + " §7| X:" + pos.getX()
                + " Y:" + pos.getY()
                + " Z:" + pos.getZ();
    }

    private static void notifyPlayer(Minecraft mc, String msg) {
        if (mc.player != null) {
            mc.player.sendMessage(
                    new StringTextComponent(msg),
                    mc.player.getUniqueID()
            );
        }
    }

    /** Сбрасываем кэш при смене сервера/мира */
    public static void reset() {
        foundBlocks.clear();
        foundEntities.clear();
    }
}
