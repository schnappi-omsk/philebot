package com.gundomrays.philebot.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class MessageQueue {

    private static final Logger log = LoggerFactory.getLogger(MessageQueue.class);

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public void messageToSend(final String message) {
        if (queue.offer(message)) {
            log.info("Message is waiting in queue: {}", message);
        } else {
            log.error("Cannot add message to queue: {}", message);
        }
    }

    public String takeMessage() {
        final String message = queue.poll();
        if (message != null) {
            log.info("Message {} is taken from the queue", message);
        }
        return message;
    }

}
