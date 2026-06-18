package com.drone.training;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * WAR 包部署支持 (Tomcat 9.0)
 *
 * @author 罗健 202308852
 */
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(AgriDroneTrainingApplication.class);
    }
}
