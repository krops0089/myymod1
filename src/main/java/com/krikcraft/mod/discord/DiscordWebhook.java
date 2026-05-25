package com.krikcraft.mod.discord;

import com.krikcraft.mod.KrikCraftMod;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Отправляет красивые Discord Embed сообщения при нахождении объектов.
 *
 * Пример сообщения:
 * [⚡ Анархия-308] Найден шалкер на X: -448, Y: 34, Z: 2482
 */
public class DiscordWebhook {

    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1508123650379743272/Au9pe91KJZEteLr2e4tCO5S9f9TgcNK4AjGbIz-cVaOq02kumkJI194cod5k818BY2QI";

    /**
     * @param anarchy  название анархии, например "Анархия-308"
     * @param what     что найдено, например "📦 Шалкер"
     * @param x, y, z  координаты
     */
    public static void send(String anarchy, String what, int x, int y, int z) {
        // Определяем цвет embed по типу находки
        int color = getColor(what);

        // Формируем заголовок в нужном формате
        String title = "[" + anarchy + "] Найдено: " + what;
        String coordsField = "X: " + x + ", Y: " + y + ", Z: " + z;
        String timestamp = Instant.now().toString();

        // Discord Embed JSON
        String json = "{"
                + "\"embeds\": [{"
                + "\"title\": \"" + escapeJson(title) + "\","
                + "\"color\": " + color + ","
                + "\"fields\": ["
                + "{"
                + "\"name\": \"📍 Координаты\","
                + "\"value\": \"```" + coordsField + "```\","
                + "\"inline\": false"
                + "}"
                + "],"
                + "\"footer\": {"
                + "\"text\": \"KrikCraft Scanner\""
                + "},"
                + "\"timestamp\": \"" + timestamp + "\""
                + "}]"
                + "}";

        // Отправляем асинхронно, чтобы не лагать игру
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("User-Agent", "KrikCraft-Mod/1.0");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != 204 && responseCode != 200) {
                    KrikCraftMod.LOGGER.warn("Discord webhook ответил кодом: " + responseCode);
                }
                conn.disconnect();

            } catch (Exception e) {
                KrikCraftMod.LOGGER.error("Ошибка отправки в Discord: " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.setName("KrikCraft-Discord");
        thread.start();
    }

    /** Цвет embed зависит от типа находки */
    private static int getColor(String what) {
        if (what.contains("Игрок"))           return 0xFF4444; // красный
        if (what.contains("Спавнер"))         return 0xFFA500; // оранжевый
        if (what.contains("Структурный"))     return 0x00BFFF; // голубой
        if (what.contains("Шалкер"))          return 0x8B008B; // фиолетовый
        if (what.contains("Заряженный"))      return 0x00FF00; // зелёный (⚡ крипер)
        if (what.contains("Ифрит"))           return 0xFF6600; // огненный
        if (what.contains("Зимогор"))         return 0x87CEEB; // светло-голубой
        if (what.contains("Житель"))          return 0xFFD700; // золотой
        if (what.contains("Эндермен"))        return 0x1A1A2E; // тёмный
        if (what.contains("Магмовый"))        return 0xFF4500; // красно-оранжевый
        if (what.contains("Страж"))           return 0x00CED1; // бирюзовый
        if (what.contains("пазл"))            return 0xFF69B4; // розовый
        return 0x7289DA; // дефолт Discord синий
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
