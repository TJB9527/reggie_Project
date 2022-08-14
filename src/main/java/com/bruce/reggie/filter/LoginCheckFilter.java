package com.bruce.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.bruce.reggie.common.BaseContext;
import com.bruce.reggie.common.MyMetaObjectHandler;
import com.bruce.reggie.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1. 获取本次请求的urI
        String requestURI = request.getRequestURI();

        log.info("拦截到请求:{}",requestURI);

        //2. 判断本次请求是否需要处理
        //声明直接放行的请求路径(注：静态资源可直接供用户访问，但访问静态资源是看不到数据的所以不影响系统安全性)
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };

        boolean check = check(urls, requestURI);

        //3. 如果不需要处理，放行
        if (check) {
//            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //4-1. 判断员工登录状态，如果已登录则直接放行
        if (request.getSession().getAttribute("employee") != null) {
            log.info("员工已登录，员工id为：{}",request.getSession().getAttribute("employee"));

            //获取当前登录员工id保存到线程变量中，便于在其他地方(不能获取到HttpSession的同线程类)取到当前登录员工id
            BaseContext.setCurrentId((Long) request.getSession().getAttribute("employee"));

            filterChain.doFilter(request, response);
            return;
        }

        //4-2. 判断用户登录状态，如果已登录则直接放行
        if (request.getSession().getAttribute("user") != null) {
            log.info("移动端用户已登录，id为：{}",request.getSession().getAttribute("user"));

            //获取当前登录用户id保存到线程变量中，便于在其他地方(不能获取到HttpSession的同线程类)取到当前登录用户id
            BaseContext.setCurrentId((Long) request.getSession().getAttribute("user"));

            filterChain.doFilter(request, response);
            return;
        }

        log.info("当前属于未登录状态，请登录后访问网址");
        //5. 如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));
        return;
    }

    /**
     * 路径匹配，检测本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match == true) {
                log.info("匹配成功，直接放行");
                return true;
            }
        }
        return false;
    }
}
