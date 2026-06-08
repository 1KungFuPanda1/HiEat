package com.sky.config;

import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;
    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */

    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        //只要前端请求路径是 /admin/xxx，且不是上面放行的 6个接口，
        // 就会进入 JwtTokenAdminInterceptor 的 preHandle 方法
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login")
                .excludePathPatterns("/admin/employee/register")
                .excludePathPatterns("/admin/employee/logout")
                .excludePathPatterns("/admin/common/**")
                .excludePathPatterns("/admin/shopCategory/list")
                .excludePathPatterns("/admin/login")
                .excludePathPatterns("/admin/logout");

        //只要前端请求路径是 /user/xxx，且不是上面放行的 6个接口，
        // 就会进入 JwtTokenUserInterceptor 的 preHandle 方法
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/user/login", "/user/user/register", "/user/user/resetPassword")
                .excludePathPatterns("/user/user/login/account") // 排除账号密码登录接口
                .excludePathPatterns("/user/shop/status/**", "/user/shop/page/**", "/user/shop/count/**", "/user/shop/score/**")
                .excludePathPatterns("/user/shop/category/**", "/user/shop/search/**") // 分类列表和搜索无需登录
                .excludePathPatterns("/user/shopCategory/list")
                .excludePathPatterns("/user/notice/list")
                .excludePathPatterns("/user/common/**");
    }


    /**
     * H5 / 微信小程序 / App / 网页 / 别的项目，全都能直接调用接口，不需要前端做任何跨域配置！
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 允许所有前端来源（UniApp、手机浏览器、小程序等）
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)    // 允许携带 Cookie、Token
                .allowedHeaders("*")       // 允许所有请求头
                .maxAge(3600);            // 预检请求缓存时间
    }

    @Bean
    public Docket docket1() {
        log.info("准备生成接口文档...");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("HiEat外卖项目接口文档")
                .version("2.0")
                .description("HiEat外卖项目接口文档")
                .build();

        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName("管理端接口")
                .apiInfo(apiInfo)
                .select()
                // 指定生成接口需要扫描的包
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.admin"))
                .paths(PathSelectors.any())
                .build();

        return docket;
    }

    @Bean
    public Docket docket2() {
        log.info("准备生成接口文档...");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("HiEat外卖项目接口文档")
                .version("2.0")
                .description("HiEat外卖项目接口文档")
                .build();

        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName("用户端接口")
                .apiInfo(apiInfo)
                .select()
                // 指定生成接口需要扫描的包
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.user"))
                .paths(PathSelectors.any())
                .build();

        return docket;
    }

    /**
     * 设置静态资源映射，主要是访问接口文档（html、js、css）
     * 
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {

        log.info("开始设置静态资源映射...");
        // 1. Swagger UI 文档页面
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        // 2. Swagger Webjars（依赖库）
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        // 3. 上传文件访问路径
        //uploadDir=C:\Users\23953\.hieat\ uploads
        //String location = 图片存在电脑哪个文件夹里
        String location = Paths.get(uploadDir).resolve("").toUri().toString();
        //建立一个网址 ↔ 本地文件夹 的映射关系  ; /files/xxx.png <--> C:\Users\23953\.hieat\ uploads

        registry.addResourceHandler("/files/**").addResourceLocations(location);
    }

    /**
     * 扩展Spring MVC框架的消息转化器
     * 
     * @param converters
     */
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        // 创建一个消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        //  使用我自定义的对象映射器
        // 作用：把Java对象 → JSON，并且自动处理：
        // Long → String  不会丢失精度
        // Date → yyyy-MM-dd HH:mm:ss  不会格式错误
        converter.setObjectMapper(new JacksonObjectMapper());
        // 将自己的消息转化器加入容器中
        converters.add(0, converter);
    }
}

/**
 *                   图片回显完整流程
 *
 *     用户上传头像
 *     POST /admin/common/upload
 *
 *     后端保存文件到本地磁盘
 *     C:/Users/23953/.hieat/uploads/avatar.png
 *
 *     后端返回完整URL
 *     http://localhost:8080/files/avatar.png
 *
 *     前端存储URL
 *     数据库保存: http://localhost:8080/files/avatar.png
 *
 *    前端显示图片
 *     <img src="http://localhost:8080/files/avatar.png">
 *
 *     浏览器解析HTML，发现img标签
 *
 *     浏览器发起HTTP请求
 *     GET http://localhost:8080/files/avatar.png
 *
 *   后端处理请求
 *     Spring MVC 匹配到 /files/**
 *
 *     addResourceHandlers 配置生效
 *
 *     从本地文件系统读取图片
 *     C:/Users/23953/.hieat/uploads/avatar.png
 *                                                     
 *     返回图片二进制数据给浏览器
 *
 *    浏览器显示图片
 *
 *
 */