package com.bruce.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bruce.reggie.common.BaseContext;
import com.bruce.reggie.common.Result;
import com.bruce.reggie.entity.ShoppingCart;
import com.bruce.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加菜品或套餐到购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车数据:{}", shoppingCart);

        //获取当前用户id
        shoppingCart.setUserId(BaseContext.getCurrentId());

        //查询当前菜品或套餐是否已存在于购物车中（用户添加某菜品或套餐多次）
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, shoppingCart.getUserId());
        if (dishId != null) {
            //添加到购物车的是菜品
            lqw.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            //添加到购物车的是套餐
            lqw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart one = shoppingCartService.getOne(lqw);

        if (one != null) {
            //存在则数量加1
            Integer number = one.getNumber();
            one.setNumber(number+1);
            shoppingCartService.updateById(one);
        } else {
            //不存在则添加到购物车，数量默认就是1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            one = shoppingCart;
        }

        return Result.success(one);
    }


    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        log.info("查看购物车...");

        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        lqw.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(lqw);

        return Result.success(list);
    }

    /**
     * 商品数量减一
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public Result<Integer> sub(@RequestBody ShoppingCart shoppingCart) {
        log.info("商品数量减一...");

        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        if (shoppingCart.getDishId() != null) {
            //减少的是菜品
            lqw.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            //减少的是套餐
            lqw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart one = shoppingCartService.getOne(lqw);

        //获取商品数量
        Integer number = one.getNumber();

        if (number == 1) {
            //购物车中商品数量等于1，直接删除该商品
            shoppingCartService.remove(lqw);
        } else {
            //购物车中商品数量大于1，数量减一
            one.setNumber(number - 1);
            shoppingCartService.updateById(one);
        }

        return Result.success(number-1);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public Result<String> delete() {
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        shoppingCartService.remove(lqw);

        return Result.success("清空购物车成功");
    }
}
