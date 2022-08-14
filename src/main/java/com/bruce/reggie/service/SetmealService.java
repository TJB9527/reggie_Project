package com.bruce.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bruce.reggie.common.Result;
import com.bruce.reggie.dto.SetmealDto;
import com.bruce.reggie.entity.Setmeal;
import org.springframework.stereotype.Service;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {


    //新增套餐，对两张表做插入：setmeal、setmeal_dish
    void saveWithDish(SetmealDto setmealDto);

    //根据id查询套餐信息及关联菜品信息
    SetmealDto getByIdWithDish(Long id);

    //修改套餐，同时修改关联菜品信息
    void updateWithDish(SetmealDto setmealDto);

    //删除指定id套餐
    void deleteWithDish(List<Long> ids);
}
