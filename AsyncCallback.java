package my.airo.infra.service;

import org.springframework.stereotype.Component;

@Component
public interface AsyncCallback {
    void execute();
}
