package com.naixue.nxdp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.naixue.nxdp.interceptor.AgentUserInterceptor;
import com.naixue.nxdp.interceptor.LoginValidationInterceptor;
import com.naixue.nxdp.interceptor.MainMenuInterceptor;
import com.naixue.nxdp.interceptor.RealmInterceptor;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private RealmInterceptor realmInterceptor;

    @Autowired
    private MainMenuInterceptor mainMenuInterceptor;

    @Autowired
    private LoginValidationInterceptor loginValidationInterceptor;

    @Autowired
    private AgentUserInterceptor userAgentInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(realmInterceptor).addPathPatterns("/**");
        registry.addInterceptor(mainMenuInterceptor).addPathPatterns("/**");
        registry
                .addInterceptor(userAgentInterceptor)
                .excludePathPatterns("/agentUser/**")
                .addPathPatterns("/**");
        // 仅对游客开放首页及自助服务部分及消息提示部分
        registry
                .addInterceptor(loginValidationInterceptor)
                .excludePathPatterns("/")
                .excludePathPatterns("/monitor/**")
                .excludePathPatterns("/self-service/**")
                .excludePathPatterns("/register/**")
                .excludePathPatterns("/announcement/**")
                .excludePathPatterns("/error**")
                .addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
