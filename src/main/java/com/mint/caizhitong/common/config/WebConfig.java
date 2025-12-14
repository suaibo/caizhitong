//package com.mint.caizhitong.common.config;
//
//import com.mint.caizhitong.common.interceptors.LoginInterceptor;
//import com.mint.caizhitong.common.interceptors.RefreshTokenInterceptor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        //登录接口和注册接口不拦截
//        registry.addInterceptor(new LoginInterceptor(stringRedisTemplate)).excludePathPatterns("/api/auth/login","/api/auth/register").order(1);
//        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate)).order(0);
//    }
//}
