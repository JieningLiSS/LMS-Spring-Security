package com.ss.orch.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;


@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter
{
	@Autowired
	DataSource dataSource;
	
	@Autowired
	JdbcUserDetailsManager jdbcudm;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(jdbcudm.getDataSource()).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
    		.antMatchers("/user/**").hasRole("ADMIN")
        	.antMatchers("/lms/admin/**").hasRole("ADMIN")
        	.antMatchers("/lms/librarian/**").hasAnyRole("LIBRARIAN","ADMIN")
        	.antMatchers("/lms/borrower/**").hasAnyRole("BORROWER","LIBRARIAN","ADMIN")
        	.and().httpBasic().and().formLogin()
        	.and().csrf().disable();
    }
}