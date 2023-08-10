package com.miracle.health;

import org.springframework.stereotype.Component;


@Component
public class healthServiceImpl implements HealthService{

     private static final String HEALTH = "health";

    @Override
    public String health() {
        return HEALTH;
    }


}
