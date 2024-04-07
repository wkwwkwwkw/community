package cf.vsing.community.config;

import cf.vsing.community.controller.interceptor.HostHolderInterceptor;
import cf.vsing.community.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final HostHolderInterceptor hostHolderInterceptor;
    private final MessageInterceptor messageInterceptor;
    @Autowired
    WebMvcConfig(HostHolderInterceptor hostHolderInterceptor, MessageInterceptor messageInterceptor){
        this.hostHolderInterceptor = hostHolderInterceptor;
        this.messageInterceptor = messageInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(hostHolderInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
    }
}
