package com.bruce.reggie.config;

import com.bruce.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    /**
     * 设置静态资源映射
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始进行静态资源映射...");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");  //classpath就是resources目录
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
        //解释：手动将所有以“/backend”前缀开头的访问网址都映射到项目的“backend”目录下(静态资源如果放到resources目录下的static和tamplate目录下不需要设置可直接按路径名访问)
    }

    /**
     * 扩展springMVC框架的消息转换器
     * 作用：通过配置对象转换器将后端响应给前端的数据封装为指定格式的json对象
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用Jackson将java对象转换为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转换器对象追加到springMVC框架的转换器集合中，并通过指定扩展的转换器在转换器集合中的索引值进行优先使用
        converters.add(0, messageConverter);
    }
}
