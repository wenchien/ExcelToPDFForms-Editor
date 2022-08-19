package com.TWReceipt.logUtil;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.google.common.base.Strings;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class CustomLoggerAppender extends AppenderBase<ILoggingEvent> {

    private ConcurrentMap<String, ILoggingEvent> eventMap = new ConcurrentHashMap<>();

    private ObservableList<String> eventQueue = FXCollections.observableArrayList();

    private String prefix;

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        if (Strings.isNullOrEmpty(prefix)) {
            this.addError("Prefix is not set for CustomLoggerAppender.");
            return;
        }

        LocalDateTime date =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault());

        eventMap.put(prefix, iLoggingEvent);
        Platform.runLater(() -> eventQueue.add(prefix + " : " + date + " " + iLoggingEvent.getFormattedMessage()));

    }

    public ObservableList<String> getEventQueue() {
        return eventQueue;
    }

    public Map<String, ILoggingEvent> getEventMap() {
        return eventMap;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}
