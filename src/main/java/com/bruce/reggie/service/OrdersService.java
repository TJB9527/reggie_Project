package com.bruce.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bruce.reggie.entity.Orders;

public interface OrdersService extends IService<Orders> {

    //下单
    void submit(Orders orders);
}
