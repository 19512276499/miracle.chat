package com.miracle.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.miracle.dao")
public class MybatisConfiguration {



}
