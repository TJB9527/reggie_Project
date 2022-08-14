package com.bruce.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bruce.reggie.dto.DishDto;
import com.bruce.reggie.entity.Dish;
import org.springframework.stereotype.Service;

import java.util.List;

public interface DishService extends IService<Dish> {

    //新增菜品，同时向口味表插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
    void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和口味信息
    DishDto getByIdWithFlavor(Long id);

    //修改菜品，同时修改菜品对应口味表中口味数据，需要操作两张表：dish、dish_flavor
    void updateWithFlavor(DishDto dishDto);

    //删除菜品，同时删除菜品对应口味表中口味数据，需要操作两张表：dish、dish_flavor
    void deleteWithFlavor(List<Long> ids);
}
