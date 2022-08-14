package com.bruce.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bruce.reggie.common.CustomException;
import com.bruce.reggie.common.Result;
import com.bruce.reggie.dto.SetmealDto;
import com.bruce.reggie.entity.Setmeal;
import com.bruce.reggie.entity.SetmealDish;
import com.bruce.reggie.mapper.SetmealMapper;
import com.bruce.reggie.service.SetmealDishService;
import com.bruce.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐,同时更新setmeal_dish表
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //对setmeal表做插入
        this.save(setmealDto);

        Long setmealId = setmealDto.getId();

        //对setmeal_dish表做插入
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        boolean b = setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 根据id查询套餐信息及关联菜品信息
     * @param id
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        SetmealDto setmealDto = new SetmealDto();

        Setmeal setmeal = this.getById(id); //查套餐表获取套餐基本信息

        BeanUtils.copyProperties(setmeal,setmealDto);

        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper();
        lqw.eq(SetmealDish::getSetmealId, setmeal.getId());

        List<SetmealDish> setmealDishes = setmealDishService.list(lqw); //查setmeal_dish表获取套餐关联菜品信息

        setmealDto.setSetmealDishes(setmealDishes);

        return setmealDto;
    }

    /**
     * 修改套餐，同时修改关联菜品信息
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {

        //修改套餐表setmeal
        this.updateById(setmealDto);

        //清除原套餐关联菜品信息
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(setmealDto.getId() != null, SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(lqw);

        //添加新的套餐关联菜品信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐
     * @param ids
     */
    @Override
    @Transactional
    public void deleteWithDish(List<Long> ids) {

        //1. 判断是否套餐全都是停售状态，是则删除，否则抛出异常，提示不能删除启售状态套餐
        //select count(*) from setmeal where id in (1,2,3) and Status = 1
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(Setmeal::getId, ids);
        lqw.eq(Setmeal::getStatus, 1);

        int count = this.count(lqw);
        if (count > 0) {
            throw new CustomException("当前套餐属于启售状态，不能删除");
        }

        //2. 删除套餐关联菜品数据：setmeal_dish
        LambdaQueryWrapper<SetmealDish> lqw1 = new LambdaQueryWrapper<>();
        lqw1.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(lqw1);

        //3. 删除套餐数据：setmeal
        this.removeByIds(ids);
    }
}
