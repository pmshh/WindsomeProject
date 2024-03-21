package com.windsome.config.security;

import com.windsome.config.security.oauth.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationFailureHandler customAuthFailureHandler;
    private final CustomUserDetailsService customUserDetails;
    private final DataSource dataSource;
    private final DefaultOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .antMatchers("/h2-console/**")
                .antMatchers("/node_modules/**")
                .antMatchers("/imgs/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/", "/login/**", "/logout", "/members/new/**", "/members/check-userid", "/members/email-verification",
                        "/forgot-credentials/**", "/product/**", "/board/**", "/payment", "/oauth2/**","/payment/**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated();

        http.sessionManagement(
                session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS) // 세션 생성 정책 설정
                        .invalidSessionUrl("/login") // 세션이 유효하지 않을 때 리다이렉트할 URL
                        .maximumSessions(1) // 최대 동시 세션 수
                        .expiredUrl("/login")); // 세션이 만료된 경우 리다이렉트할 URL

        http.formLogin()
                .loginPage("/login")
                .usernameParameter("userId")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureHandler(customAuthFailureHandler)
                .permitAll();

        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .redirectionEndpoint(endpoint ->
                        endpoint.baseUri("/oauth2/callback/*"))
                .userInfoEndpoint(endpoint -> endpoint.userService(oAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
        );

        http.logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/");

        http
                .rememberMe()
                .userDetailsService(customUserDetails)
                .tokenRepository(getJDBCRepository());

//        http.csrf().disable();

        return http.build();
    }

    // RememberMe 기능 구현에 필요한 토큰 저장소
    private PersistentTokenRepository getJDBCRepository(){
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);
        return repo;
    }
}

