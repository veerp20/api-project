package com.cloud.aws.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;

@Configuration
@EnableWebSecurity
public class WebSecurity {
	
	  @Autowired 
	  private CustomAccessDeniedHandler customAccessDeniedHandler;
	  
		/*
		 * @Autowired private CustomVendorServiceImpl customVendorServiceImpl;
		 */
	 
	   @Value("${cloud.aws.credentials.access-key}")
	    private String awsAccessKey;

	    @Value("${cloud.aws.credentials.secret-key}")
	    private String awsSecretKey;

	    @Value("${cloud.aws.region.static}")
	    private String awsRegion;
	  
	  

	    @Bean
	    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
	        return httpSecurity
	            .csrf(AbstractHttpConfigurer::disable)  // Disable CSRF protection if needed
	            .authorizeHttpRequests(requests -> 
	                requests.requestMatchers("/api/**","/actuator/health", "/actuator/info").permitAll()  // Permit all requests to /api/**
	                .anyRequest().authenticated()  // Require authentication for any other requests
	            )
	            .exceptionHandling(exception -> 
	                exception.accessDeniedHandler(customAccessDeniedHandler)  // Custom access denied handler
	            )
	            .build();
	    }

	
	
	@Bean
    public AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder.standard()
                .withRegion("us-east-1")
                .build();
    }

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	 @Bean
	    public AmazonTextract amazonTextract() {
	        // Replace with your AWS access key and secret
	        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
	        
	        return AmazonTextractClientBuilder.standard()
	                .withRegion(awsRegion) // e.g., "us-east-1"
	                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
	                .build();
	    }
}
