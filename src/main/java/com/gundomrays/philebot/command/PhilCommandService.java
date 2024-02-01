package com.gundomrays.philebot.command;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class PhilCommandService {

    private final ApplicationContext applicationContext;

    public PhilCommandService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public PhilCommand command(final String name) {
        return applicationContext.getBean(name, PhilCommand.class);
    }

}
