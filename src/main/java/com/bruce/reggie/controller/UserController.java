package com.bruce.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bruce.reggie.common.Result;
import com.bruce.reggie.entity.User;
import com.bruce.reggie.service.UserService;
import com.bruce.reggie.utils.SMSUtils;
import com.bruce.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public Result<String> sendMsg(@RequestBody User user, HttpServletRequest request) {
        //获取手机号
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)) {
            //生成4位手机验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            //调用阿里云提供的短信服务API完成短信发送
            //SMSUtils.sendMessage("瑞吉外卖", "", phone, code);
            log.info("手机短信验证码：{}", code);

            //需要将生成的验证码保存到Session中，后续用户输入验证码以验证
//            request.getSession().setAttribute("code", code);

            //将生成的验证码保存到redis缓存中，设置有效期为5分钟
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);

            return Result.success("您的短信验证码是："+code);
        }
        return Result.success("短信验证码发送失败");
    }


    /**
     * 移动端用户登录
     * @param map
     * @return
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody Map map, HttpServletRequest request) {
        log.info(map.toString());

        //获取手机号和验证码
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();

        //从Session中获取验证码
//        String codeSession = (String) request.getSession().getAttribute("code");

        //从redis缓存中获取验证码
        String codeSession = (String) redisTemplate.opsForValue().get(phone);

        //比对验证码（页面提交的验证码与Session中保存的验证码相比对）
        if (code.equals(codeSession)) {
            //验证码比对成功，查询用户表判断当前手机号所属用户是否是新用户，如果是新用户则自动注册
            LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
            lqw.eq(User::getPhone, phone);

            User user = userService.getOne(lqw);

            if (user == null) {
                //新用户则自动注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
                log.info("新用户自动注册成功");
            } else {
                log.info("登录成功");
            }
            request.getSession().setAttribute("user",user.getId());

            //登录成功后，从redis缓存中删除验证码(登录成功后验证码无意义可删除)
            redisTemplate.delete(phone);

            return Result.success(user);   //注：返回前端的data数据需要是user对象，因为前端浏览器也需要对登录用户数据进行缓存
        }
        return Result.error("用户名或验证码不正确，请重新输入");
    }


    /**
     * 移动端用户退出登录
     * @param
     * @return
     */
    @PostMapping("/loginout")
    public Result<String> loginout(HttpServletRequest request) {
        //1. 清除Session对象中的用户id
        request.getSession().removeAttribute("user");

        //2. 返回结果给前端
        return Result.success("退出成功");
    }

}
