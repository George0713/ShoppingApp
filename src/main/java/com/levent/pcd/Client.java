	package com.levent.pcd;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy.RepositoryDetectionStrategies;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.github.cloudyrock.mongock.SpringBootMongock;
import com.github.cloudyrock.mongock.SpringBootMongockBuilder;
import com.levent.pcd.changelog.MigrationChangeSet;
import com.levent.pcd.config.MongoConfig;
import com.levent.pcd.config.SecurityConfig;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

@SpringBootApplication
@Configuration
@EnableCaching
@EnableTransactionManagement
@EnableMongoRepositories(basePackages="com.levent.pcd.repository")
@Import(SecurityConfig.class)
//@EnableAutoConfiguration(exclude=MongoDataAutoConfiguration.class)
public class Client extends AbstractMongoConfiguration implements WebMvcConfigurer, RepositoryRestConfigurer  {
	

	@Autowired MongoConfig config;
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Client.class, args);
		
	}

	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
		config.setRepositoryDetectionStrategy(RepositoryDetectionStrategies.ANNOTATED);
		
	}
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addRedirectViewController("/", "/products");
		registry.addViewController("/payment").setViewName("payment");
	}
	
	//For email: specifying template
	@Primary
	@Bean
	public FreeMarkerConfigurationFactoryBean factoryBean() {
		FreeMarkerConfigurationFactoryBean bean= new FreeMarkerConfigurationFactoryBean();
		bean.setTemplateLoaderPath("classpath:/templates/email");
		return bean;
	}
	

	@Bean
	public SpringBootMongock mongock(ApplicationContext springContext, MongoClient mongoClient) {
		System.out.println(MigrationChangeSet.class.getPackage().getName());
		return new SpringBootMongockBuilder(mongoClient, "levent", MigrationChangeSet.class.getPackage().getName())
				.setApplicationContext(springContext).setLockQuickConfig().build();
	}

	@Override
	public MongoClient mongoClient() {
	       ServerAddress addr = new ServerAddress(config.getHost(), config.getPort());
	      MongoCredential credential = MongoCredential.createScramSha1Credential(config.getUsername(), "admin", config.getPassword().toCharArray());
	      List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
	        credentialsList.add(credential);
	        return new MongoClient(addr, credentialsList);
	}

	@Override
	protected String getDatabaseName() {
		return "levent";
	}

}
