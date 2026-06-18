package com.drone.training;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 农业无人机培训管理系统 启动类
 *
 * @author 罗健 202308852
 */
@SpringBootApplication
@MapperScan("com.drone.training.mapper")
@EnableScheduling
public class AgriDroneTrainingApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgriDroneTrainingApplication.class, args);
        System.out.println("\n====== 农业无人机培训管理系统 启动成功 ======");
        System.out.println("接口文档前缀: http://localhost:8080/api\n");
    }
}
