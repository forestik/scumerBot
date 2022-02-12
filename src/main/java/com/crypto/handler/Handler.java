package com.crypto.handler;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface Handler {
    void choose(Message message);
}
