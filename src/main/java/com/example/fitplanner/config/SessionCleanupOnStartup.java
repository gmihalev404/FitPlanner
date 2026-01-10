package com.example.fitplanner.config;

import com.example.fitplanner.service.SessionModelService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class SessionCleanupOnStartup implements ApplicationListener<ContextRefreshedEvent> {

    private final SessionModelService sessionModelService;

    public SessionCleanupOnStartup(SessionModelService sessionModelService) {
        this.sessionModelService = sessionModelService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        sessionModelService.clearAllSessions(); // implement this to remove stored session data
    }
}
