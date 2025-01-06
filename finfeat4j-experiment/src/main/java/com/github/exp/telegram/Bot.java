package com.github.exp.telegram;

import com.github.exp.Application;
import org.slf4j.Logger;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class Bot implements LongPollingSingleThreadUpdateConsumer {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Bot.class);

    private final TelegramClient client;
    private final String chatId;

    public Bot(Application.Config config) {
        this.client = new OkHttpTelegramClient(config.getBotToken());
        this.chatId = config.getChatId();
    }

    @Override
    public void consume(Update update) {
        log.info("Update: {}", update);
    }

    public void sendMessage(String msg) {
        sendMessage(chatId, msg);
    }

    public void sendMessage(String chatId, String msg) {
        try {
            this.client.executeAsync(
                    SendMessage.builder()
                            .parseMode(ParseMode.HTML)
                            .disableWebPagePreview(true)
                            .disableNotification(true).chatId(chatId).text(msg).build()
            );
        } catch (TelegramApiException e) {
            log.error("Error sending message", e);
        }
    }
}
