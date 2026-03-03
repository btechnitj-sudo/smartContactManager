package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
//    @Bean
//    public UserDetailsService userDetailsService() {
//    	return new UserDetailsServiceImpl();
//    }
	 @Autowired
	    private UserDetailsService userDetailsService;

        @Bean
        public PasswordEncoder passwordEncoder(){
            return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,PasswordEncoder passwordEncoder){
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
            provider.setPasswordEncoder(passwordEncoder);
            return provider;

        }
    
//     @Bean
//     public BCryptPasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }
    
//     @Bean
//    public DaoAuthenticationProvider authenticationProvider() {
//     	//contructor injection
    	
//     	//this is used when bean is defined of userDetailsService
//     	//DaoAuthenticationProvider daoAuthenticationProvider=new DaoAuthenticationProvider(userDetailsService());
    	
//     	//this is used when autowired userDetailsService beacause @service is used
//    	DaoAuthenticationProvider daoAuthenticationProvider=new DaoAuthenticationProvider(userDetailsService);
//    	daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
//    	return daoAuthenticationProvider;
// }
    	
    
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> 
            authorize
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/user/**").hasRole("USER")
                    .requestMatchers("/**","/")
                    .permitAll()
                    .anyRequest().authenticated()
            )
           
            .formLogin(form -> form
                .loginPage("/signin")
               .loginProcessingUrl("/dologin")
           .defaultSuccessUrl("/user/index")
             // .failureUrl("/login?error=true")
                .permitAll()
           );
        //    .logout(logout -> logout
        //        .logoutUrl("/logout")
        //        .logoutSuccessUrl("/login?logout")
        //        .permitAll()
        //     );
        
        return http.build();
    }
}
