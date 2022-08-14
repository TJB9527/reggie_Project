package com.bruce.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bruce.reggie.common.Result;
import com.bruce.reggie.entity.Employee;
import com.bruce.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request  员工登录成功后将员工id保存到sesseion中以示登录成功，后期要获取当前登录员工可直接通过request对象获取一个session，从中取即可
     * @param employee 前端传过来的数据时username、password，恰与Employee实体类两个属性属性名一致，故可以封装成Employee对象
     * @return
     */
    @PostMapping("/login")
    public Result<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //1. 对页面提交的明文密码进行MD5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2. 根据页面提交的用户名查询数据库
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper();
        lqw.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(lqw);   //此处能直接使用getOne()方法是因为表中的username定位unique故是唯一的~~~

        //3. 若用户名查找失败返回登录失败结果
        if (emp == null) {
            return Result.error("登录失败");
        }

        //4. 密码对比，如果不一致返回登录失败结果
        if (!password.equals(emp.getPassword())) {
            return Result.error("密码不正确");
        }

        //5. 查看员工状态(status)，如果员工已禁用，返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return Result.error("账号已禁用");
        }

        //6. 登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return Result.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        //1. 清除Session对象中的员工id
        request.getSession().removeAttribute("employee");

        //2. 返回结果给前端
        return Result.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public Result<String> addEmp(HttpServletRequest request,@RequestBody Employee employee) {
        log.info("新增员工");

        //设置初始密码123456并进行MD5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//
//        Long empId = (Long) request.getSession().getAttribute("employee"); //Session的getAttribute()方法返回值默认是Object类型，需要强转
//
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        employeeService.save(employee);

        return Result.success("新增员工成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> getPageEmp(int page,int pageSize,String name) {
        log.info("page = {}.pageSize = {}, name = {}",page,pageSize,name);

        //构造分页构造器
        Page<Employee> pageInfo = new Page<>(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper();
        //添加过滤条件
        lqw.like(StringUtils.isNotEmpty(name),Employee::getName, name);
        //添加排序条件
        lqw.orderByDesc(Employee::getUpdateTime);

        //执行分页查询
        employeeService.page(pageInfo, lqw);
        return Result.success(pageInfo);
    }

    /**
     * 按id修改员工信息（通用）
     * 编辑员工、管理员修改员工状态...
     * @param employee
     * @return
     */
    @PutMapping
    public Result<String> updateEmp(HttpServletRequest request,@RequestBody Employee employee) {
        log.info("当前待修改员工信息为：{}", employee.toString());

//        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));
        boolean boo = employeeService.updateById(employee);

        if (boo) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable String id) {
        log.info("按id查询员工...");
        Employee emp = employeeService.getById(id);
        if (emp == null) {
            return Result.error("查询失败");
        } else {
            return Result.success(emp);
        }
    }
}
