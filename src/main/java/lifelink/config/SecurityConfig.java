package lifelink.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.multipart.support.MultipartFilter;

/** Adds CSRF protection while retaining LifeLink's custom login and role checks. */
@Configuration
public class SecurityConfig {

    @Bean
    UserDetailsService noFrameworkManagedAccounts() {
        return username -> {
            throw new UsernameNotFoundException("Authentication is managed by LifeLink.");
        };
    }

    @Bean
    FilterRegistrationBean<MultipartFilter> multipartFilterRegistration() {
        MultipartFilter filter = new MultipartFilter();
        filter.setMultipartResolverBeanName("multipartResolver");
        FilterRegistrationBean<MultipartFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    SecurityFilterChain lifeLinkSecurity(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
