package com.krikcraft.mod.autopilot;

import com.krikcraft.mod.KrikCraftMod;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Автопилот — летит змейкой по квадрату от -2200 до 2200 на высоте Y=250.
 * Работает только если надеты элитры и активна кастомная команда /fly сервера.
 *
 * Схема змейки:
 *   → строка Z=-2200..2200 при X=-2200
 *   ↓ шаг X += STEP
 *   ← строка Z=2200..-2200 при X=-2200+STEP
 *   и т.д.
 */
public class AutoPilot {

    private static final int MIN_COORD = -2200;
    private static final int MAX_COORD = 2200;
    private static final int STEP = 32;       // шаг между строками змейки (чанк = 16, берём 2 чанка)
    private static final double TARGET_Y = 250.0;
    private static final double SPEED = 2.5;  // блоков за тик (~24 б/с)

    // Состояние маршрута
    private static int currentX = MIN_COORD;
    private static boolean goingPositiveZ = true; // направление по Z

    // Целевая точка текущего отрезка
    private static double targetX = MIN_COORD;
    private static double targetZ = MIN_COORD;
    private static boolean initialized = false;

    public static void start(Minecraft mc) {
        if (mc.player == null) return;
        currentX = MIN_COORD;
        goingPositiveZ = true;
        targetX = currentX;
        targetZ = MIN_COORD;
        initialized = true;
        KrikCraftMod.autopilotActive = true;
        sendChat(mc, "§a[KrikCraft] Автопилот запущен! Маршрут: змейка -2200..2200");
    }

    public static void stop(Minecraft mc) {
        KrikCraftMod.autopilotActive = false;
        initialized = false;
        if (mc.player != null) {
            sendChat(mc, "§c[KrikCraft] Автопилот остановлен.");
        }
    }

    public static void tick(Minecraft mc) {
        PlayerEntity player = mc.player;
        if (player == null || !initialized) return;

        // Проверяем элитры
        if (!hasElytra(player)) {
            sendChat(mc, "§c[KrikCraft] Автопилот: нет элитр! Остановка.");
            stop(mc);
            return;
        }

        double px = player.getPosX();
        double py = player.getPosY();
        double pz = player.getPosZ();

        // Сначала набираем высоту Y=250
        if (py < TARGET_Y - 2) {
            moveToward(player, px, TARGET_Y, pz);
            return;
        }

        // Летим к текущей цели
        double dx = targetX - px;
        double dz = targetZ - pz;
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist < 3.0) {
            // Достигли цели — вычисляем следующую точку маршрута
            nextWaypoint(mc);
        } else {
            // Двигаемся к цели
            double nx = px + (dx / dist) * SPEED;
            double nz = pz + (dz / dist) * SPEED;
            moveToward(player, nx, TARGET_Y, nz);
        }
    }

    private static void nextWaypoint(Minecraft mc) {
        // Дошли до конца строки по Z — переходим на следующую строку по X
        if (goingPositiveZ && targetZ >= MAX_COORD) {
            currentX += STEP;
            if (currentX > MAX_COORD) {
                // Весь маршрут пройден — начинаем заново
                currentX = MIN_COORD;
                sendChat(mc, "§e[KrikCraft] Маршрут завершён, начинаю заново.");
            }
            goingPositiveZ = false;
            targetX = currentX;
            targetZ = MAX_COORD; // следующий отрезок назад
        } else if (!goingPositiveZ && targetZ <= MIN_COORD) {
            currentX += STEP;
            if (currentX > MAX_COORD) {
                currentX = MIN_COORD;
                sendChat(mc, "§e[KrikCraft] Маршрут завершён, начинаю заново.");
            }
            goingPositiveZ = true;
            targetX = currentX;
            targetZ = MIN_COORD;
        } else {
            // Продолжаем текущую строку
            targetZ = goingPositiveZ ? MAX_COORD : MIN_COORD;
        }
    }

    private static void moveToward(PlayerEntity player, double tx, double ty, double tz) {
        // Устанавливаем позицию напрямую (требует серверной синхронизации через /fly)
        // На анархии с /fly сервер принимает движение клиента
    player.setVelocity(
            (tx - player.getPosX()) * 0.5,
            (ty - player.getPosY()) * 0.3,
            (tz - player.getPosZ()) * 0.5
    );
        player.setPosition(
                player.getPosX() + player.getMotion().x,
                player.getPosY() + player.getMotion().y,
                player.getPosZ() + player.getMotion().z
        );
    }

    private static boolean hasElytra(PlayerEntity player) {
        net.minecraft.item.ItemStack chest = player.inventory.armorInventory.get(2);
        return chest.getItem() == net.minecraft.item.Items.ELYTRA;
    }

    private static void sendChat(Minecraft mc, String msg) {
        if (mc.player != null) {
            mc.player.sendMessage(
                    new net.minecraft.util.text.StringTextComponent(msg),
                    mc.player.getUniqueID()
            );
        }
    }
}
