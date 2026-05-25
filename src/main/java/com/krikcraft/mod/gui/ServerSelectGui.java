package com.krikcraft.mod.gui;

import com.krikcraft.mod.KrikCraftMod;
import com.krikcraft.mod.autopilot.AutoPilot;
import com.krikcraft.mod.scanner.Scanner;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI выбора анархии + управление автопилотом.
 * Открывается по Ctrl+G.
 */
public class ServerSelectGui extends Screen {

    // Все анархии KrikCraft
    private static final List<String> SERVERS = new ArrayList<>();
    static {
        for (int i = 101; i <= 110; i++) SERVERS.add("Анархия-" + i);
        for (int i = 201; i <= 225; i++) SERVERS.add("Анархия-" + i);
        for (int i = 301; i <= 315; i++) SERVERS.add("Анархия-" + i);
        for (int i = 401; i <= 410; i++) SERVERS.add("Анархия-" + i);
        for (int i = 501; i <= 510; i++) SERVERS.add("Анархия-" + i);
        for (int i = 601; i <= 601; i++) SERVERS.add("Анархия-" + i);
    }

    // Скролл
    private int scrollOffset = 0;
    private static final int BUTTONS_PER_PAGE = 10;

    // Высота кнопки
    private static final int BTN_H = 18;
    private static final int BTN_W = 160;

    public ServerSelectGui() {
        super(new StringTextComponent("KrikCraft — Выбор анархии"));
    }

    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }

    private void rebuildButtons() {
        this.buttons.clear();
        this.children.clear();

        int startX = this.width / 2 - BTN_W - 10;
        int startY = 50;

        // Кнопки серверов
        int end = Math.min(scrollOffset + BUTTONS_PER_PAGE, SERVERS.size());
        for (int i = scrollOffset; i < end; i++) {
            final String server = SERVERS.get(i);
            boolean selected = server.equals(KrikCraftMod.currentAnarchy);

            String label = (selected ? "§a✔ " : "§7") + server;

            int yPos = startY + (i - scrollOffset) * (BTN_H + 3);
            addButton(new Button(startX, yPos, BTN_W, BTN_H,
                    new StringTextComponent(label), btn -> {
                KrikCraftMod.currentAnarchy = server;
                Scanner.reset(); // сбрасываем кэш при смене анархии

                // Отправляем команду телепортации /an101, /an201 и т.д.
                String cmd = "/an" + server.replace("Анархия-", "");
                Minecraft.getInstance().player.sendChatMessage(cmd);

                rebuildButtons();
            }));
        }

        // Кнопки прокрутки
        int rightX = startX + BTN_W + 5;
        addButton(new Button(rightX, startY, 20, BTN_H,
                new StringTextComponent("▲"), btn -> {
            if (scrollOffset > 0) { scrollOffset--; rebuildButtons(); }
        }));
        addButton(new Button(rightX, startY + (BTN_H + 3) * (BUTTONS_PER_PAGE - 1), 20, BTN_H,
                new StringTextComponent("▼"), btn -> {
            if (scrollOffset + BUTTONS_PER_PAGE < SERVERS.size()) { scrollOffset++; rebuildButtons(); }
        }));

        // ── Правая панель: управление автопилотом ──
        int panelX = this.width / 2 + 20;
        int panelY = 50;

        // Статус
        String autopilotStatus = KrikCraftMod.autopilotActive
                ? "§aАвтопилот: ВКЛ" : "§cАвтопилот: ВЫКЛ";
        addButton(new Button(panelX, panelY, 160, BTN_H,
                new StringTextComponent(autopilotStatus), btn -> {
            Minecraft mc = Minecraft.getInstance();
            if (KrikCraftMod.autopilotActive) {
                AutoPilot.stop(mc);
            } else {
                AutoPilot.start(mc);
            }
            rebuildButtons();
        }));

        // Кнопка /fly — отправляет команду серверу
        addButton(new Button(panelX, panelY + BTN_H + 5, 160, BTN_H,
                new StringTextComponent("§e/fly — включить полёт"), btn -> {
            Minecraft.getInstance().player.sendChatMessage("/fly");
        }));

        // Кнопка сброса кэша сканера
        addButton(new Button(panelX, panelY + (BTN_H + 5) * 2, 160, BTN_H,
                new StringTextComponent("§bСбросить кэш сканера"), btn -> {
            Scanner.reset();
            Minecraft.getInstance().player.sendMessage(
                    new StringTextComponent("§a[KrikCraft] Кэш сканера сброшен."),
                    Minecraft.getInstance().player.getUniqueID()
            );
        }));

        // Закрыть
        addButton(new Button(this.width / 2 - 60, this.height - 30, 120, 20,
                new StringTextComponent("§fЗакрыть"), btn -> {
            this.onClose();
        }));
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);

        // Заголовок
        drawCenteredString(ms, this.font,
                "§6§lKrikCraft §r§7— Управление серверами",
                this.width / 2, 15, 0xFFFFFF);

        drawCenteredString(ms, this.font,
                "§7Текущая: §b" + KrikCraftMod.currentAnarchy,
                this.width / 2, 28, 0xFFFFFF);

        // Подписи колонок
        int leftX = this.width / 2 - BTN_W - 10;
        drawString(ms, this.font, "§7Выбери анархию:", leftX, 40, 0xAAAAAA);
        drawString(ms, this.font, "§7Управление:", this.width / 2 + 20, 40, 0xAAAAAA);

        super.render(ms, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // игра не останавливается
    }
}
