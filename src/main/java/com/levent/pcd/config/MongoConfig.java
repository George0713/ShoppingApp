package com.levent.pcd.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
public class MongoConfig {

  @Value("${spring.data.mongodb.host}")
  private  String host;

  @Value("#{new Integer('${spring.data.mongodb.port}')}")
  private  Integer port;

  @Value("${spring.data.mongodb.database}")
  private  String database;

  @Value("${spring.data.mongodb.username}")
  private  String username;

  @Value("${spring.data.mongodb.password}")
  private  String password;

 
}