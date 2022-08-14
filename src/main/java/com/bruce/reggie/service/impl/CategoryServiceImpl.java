package com.bruce.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bruce.reggie.common.CustomException;
import com.bruce.reggie.common.Result;
import com.bruce.reggie.entity.Category;
import com.bruce.reggie.entity.Dish;
import com.bruce.reggie.entity.Setmeal;
import com.bruce.reggie.mapper.CategoryMapper;
import com.bruce.reggie.service.CategoryService;
import com.bruce.reggie.service.DishService;
import com.bruce.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;


    /**
     * 根据id删除分类，删之前需要判断当前分类是否关联了菜品或套餐
     * 菜品表dish和套餐表setmeal都含字段categoryId，获取当前待删除的分类的id，查询获得分类的type，根据type对应查询两张表判断是否有关联记录
     * 查询示例：select count(*) from dish where categoryId = 当前待删除分类id
     */
    @Override
    public Result<String> remove(Long id) {

        Category category = this.getById(id);
        Integer type = category.getType();

        if (type == 1) {
            LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
            lqw.eq(Dish::getCategoryId, id);

            int count = dishService.count(lqw);

            if (count > 0) {
                throw new CustomException("当前分类关联了菜品，不能删除");
            } else {
                this.removeById(id);
                log.info("当前分类删除成功");
                return Result.success("删除成功");
            }
        } else {
            LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
            lqw.eq(Setmeal::getCategoryId, id);

            int count = setmealService.count(lqw);

            if (count > 0) {
                throw new CustomException("当前分类关联了套餐，不能删除");
            } else {
                this.removeById(id);
                log.info("当前分类删除成功");
                return Result.success("删除成功");
            }
        }
    }
}
