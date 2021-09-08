package com.bountyhunter.tomato.autostart;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AutostartProcessor implements ApplicationRunner {

    // @Autowired private ThreadPoolService threadPoolService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
    }
}
