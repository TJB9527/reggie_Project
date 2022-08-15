package com.bruce.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bruce.reggie.common.CustomException;
import com.bruce.reggie.dto.DishDto;
import com.bruce.reggie.entity.Dish;
import com.bruce.reggie.entity.DishFlavor;
import com.bruce.reggie.entity.Setmeal;
import com.bruce.reggie.entity.SetmealDish;
import com.bruce.reggie.mapper.DishMapper;
import com.bruce.reggie.service.DishFlavorService;
import com.bruce.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Override
    @Transactional   //涉及多张表——开启事务控制保证数据的一致性
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);  //一开始dishDto对象中dishId为空(主键id自动生成：ASSIN_ID)，经此一步(对dish表做insert操作)dishDto中dishId就自动生成且赋值了

        //给dishDto.getFlavors()中的每个口味对象（对应口味数据表中每条待插入记录）的dishId属性(字段)赋值
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());  //实现对集合中每个dishflavor对象的属性dishId赋值有两种方式：for循环、stream流做映射（高级）

        //保存菜品口味数据到口味表dish_flavor
        dishFlavorService.saveBatch(flavors); //仅仅这样设置不行，这样口味表新增记录数据只有name、value、createTime...菜品id（dish_Id）没有设置
    }

    /**
     * //根据id查询菜品信息和口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
       //查询菜品基本信息
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId, dish.getId());

        List<DishFlavor> flavors = dishFlavorService.list(lqw);

        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 修改菜品，同时修改菜品对应口味表中口味数据，需要操作两张表：dish、dish_flavor
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //修改菜品表dish
        this.updateById(dishDto);

        //清理口味表中对应菜品的口味数据---delete操作
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(lqw);

        //添加口味表dish_flavor
        List<DishFlavor> flavors = dishDto.getFlavors();//注意dishDto.getFlavors()获得的对象只有口味表中的name、value字段，必须给dish_Id字段赋值才能添加
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 删除菜品，同时删除菜品对应口味表中口味数据，需要操作两张表：dish、dish_flavor
     * @param ids
     */
    @Override
    public void deleteWithFlavor(List<Long> ids) {

        //1. 判断是否套餐全都是停售状态，是则删除，否则抛出异常，提示不能删除启售状态套餐
        //select count(*) from dish where id in (1,2,3) and Status = 1
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.in(Dish::getId, ids);
        lqw.eq(Dish::getStatus, 1);

        int count = this.count(lqw);
        if (count > 0) {
            throw new CustomException("当前菜品属于启售状态，不能删除");
        }

        //2. 删除菜品关联口味数据：dish_flavor
        LambdaQueryWrapper<DishFlavor> lqw1 = new LambdaQueryWrapper<>();
        lqw1.in(DishFlavor::getDishId, ids);
        boolean remove = dishFlavorService.remove(lqw1);

        //3. 删除菜品数据：dish
       this.removeByIds(ids);
    }
}
