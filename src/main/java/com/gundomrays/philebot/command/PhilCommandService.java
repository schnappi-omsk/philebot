package com.gundomrays.philebot.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class PhilCommandService {

    private static final Logger log = LoggerFactory.getLogger(PhilCommandService.class);

    private final ApplicationContext applicationContext;

    public PhilCommandService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public PhilCommand command(final String name) {
        try {
            return applicationContext.getBean(name, PhilCommand.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

}
