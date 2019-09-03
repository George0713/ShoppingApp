package com.levent.pcd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
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
import com.levent.pcd.config.SecurityConfig;
import com.mongodb.MongoClient;

@SpringBootApplication
@Configuration
@EnableCaching
@EnableTransactionManagement
@EnableMongoRepositories(basePackages="com.levent.pcd.repository")
@Import(SecurityConfig.class)
public class Client  implements WebMvcConfigurer, RepositoryRestConfigurer {
	

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
	

	/*@Bean
	public JavaMailSenderImpl mailSender() {
		JavaMailSenderImpl impl= new JavaMailSenderImpl();
		impl.setProtocol(protocol);
	}
	<beans:bean id="mailSender"
			class="org.springframework.mail.javamail.JavaMailSenderImpl">

			<!-- configured in systems.properties -->
			<beans:property name="protocol" value="${mailSender.protocol}" />
			<beans:property name="host" value="${mailSender.host}" />
			<beans:property name="port" value="${mailSender.port}" />

			<beans:property name="username">
				<beans:value>${mailSender.username}</beans:value>
			</beans:property>

			<beans:property name="password">
				<beans:value>${mailSender.password}</beans:value>
			</beans:property>
			
			<beans:property name="javaMailProperties">
				<beans:props>
					<beans:prop key="mail.smtp.auth">${mailSender.mail.smtp.auth}</beans:prop>
					<beans:prop key="mail.smtp.starttls.enable">${mail.smtp.starttls.enable}</beans:prop>
				</beans:props>
			</beans:property>
		</beans:bean>*/

	@Bean
	public SpringBootMongock mongock(ApplicationContext springContext, MongoClient mongoClient) {
		System.out.println(MigrationChangeSet.class.getPackage().getName());
		return new SpringBootMongockBuilder(mongoClient, "levent", MigrationChangeSet.class.getPackage().getName())
				.setApplicationContext(springContext).setLockQuickConfig().build();
	}

}
