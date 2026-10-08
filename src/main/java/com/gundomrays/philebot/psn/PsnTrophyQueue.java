package com.gundomrays.philebot.psn;

import com.gundomrays.philebot.psn.domain.PsnTrophy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class PsnTrophyQueue {

    private static final Logger log = LoggerFactory.getLogger(PsnTrophyQueue.class);

    private final BlockingQueue<PsnTrophy> trophies = new LinkedBlockingQueue<>();

    public void placeTrophy(final PsnTrophy trophy) {
        if (trophies.offer(trophy)) {
            log.info("PSN trophy added to queue, player={}, trophy={}", trophy.getProfile().getOnlineId(), trophy.getTrophyName());
        } else {
            log.error("Cannot add PSN trophy to queue, player={}, trophy={}", trophy.getProfile().getOnlineId(), trophy.getTrophyName());
        }
    }

    public PsnTrophy takeTrophy() {
        final PsnTrophy trophy = trophies.poll();
        if (trophy != null) {
            log.info("Got PSN trophy from queue. Player: {}, trophy: {}", trophy.getProfile().getOnlineId(), trophy.getTrophyName());
        }
        return trophy;
    }

}
