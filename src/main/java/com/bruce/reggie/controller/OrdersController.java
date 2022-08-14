package com.bruce.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bruce.reggie.common.BaseContext;
import com.bruce.reggie.common.Result;
import com.bruce.reggie.dto.OrdersDto;
import com.bruce.reggie.entity.OrderDetail;
import com.bruce.reggie.entity.Orders;
import com.bruce.reggie.service.OrderDetailService;
import com.bruce.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 提交订单(点击“去支付”)
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders orders) {
        log.info("下单数据：{}",orders);

       ordersService.submit(orders);

        return Result.success("下单成功");
    }

    /**
     * 分页查询订单
     * @return
     */
    @GetMapping("/userPage")
    public Result<Page> userPageOrders(int page, int pageSize) {
        //分页构造器
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Orders::getUserId, BaseContext.getCurrentId());
        lqw.orderByDesc(Orders::getOrderTime);

        ordersService.page(ordersPage, lqw);//根据当前登录用户id查询当前用户的所有订单

        //对象拷贝
        BeanUtils.copyProperties(ordersPage, ordersDtoPage, "records");

        //为每一个订单查询查询订单明细，将订单orders与订单明细集合一并封装到对应的一个orderDto对象中
        List<Orders> ordersList = ordersPage.getRecords();
        List<OrdersDto> ordersDtoList = ordersList.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto); //对象基础属性拷贝

            //根据订单item的订单号查询订单明细
            LambdaQueryWrapper<OrderDetail> lqw1 = new LambdaQueryWrapper<>();
            lqw1.eq(OrderDetail::getOrderId, item.getId());
            List<OrderDetail> orderDetailList = orderDetailService.list(lqw1);

            ordersDto.setOrderDetails(orderDetailList);
            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(ordersDtoList);

        return Result.success(ordersDtoPage);
    }

    @GetMapping("/page")
    public Result<Page> page(int page,int pageSize,Long number) {   //暂不考虑前端通过时间段查询
        //分页构造器
        Page<Orders> pageInfo = new Page<>(page,pageSize);

        //条件构造器
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        lqw.eq(number != null, Orders::getId, number);

        ordersService.page(pageInfo, lqw);

        return Result.success(pageInfo);
    }

}
