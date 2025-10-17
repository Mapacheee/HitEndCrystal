package me.mapacheee.hitendcrystal.config;

import com.google.inject.Inject;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.service.annotation.Service;
import com.thewinterframework.service.annotation.lifecycle.OnReload;

@Service
public class ConfigService {

    private final Container<Config> configContainer;
    private final Container<Messages> messagesContainer;

    @Inject
    public ConfigService(Container<Config> configContainer, Container<Messages> messagesContainer) {
        this.configContainer = configContainer;
        this.messagesContainer = messagesContainer;
    }

    public Config getConfig() {
        return configContainer.get();
    }

    public Messages getMessages() {
        return messagesContainer.get();
    }

    @OnReload
    public void reload() {
        configContainer.reload();
        messagesContainer.reload();
    }
}
