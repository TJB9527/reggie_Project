package com.bruce.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bruce.reggie.common.Result;
import com.bruce.reggie.dto.SetmealDto;
import com.bruce.reggie.entity.Category;
import com.bruce.reggie.entity.Setmeal;
import com.bruce.reggie.service.CategoryService;
import com.bruce.reggie.service.SetmealDishService;
import com.bruce.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @CacheEvict(value = "setmealCache",allEntries = true)
    @PostMapping
    public Result<String> add(@RequestBody SetmealDto setmealDto) {
        //对两张表做插入操作：setmeal、setmeal_dish
        setmealService.saveWithDish(setmealDto);
        return Result.success("添加成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page,int pageSize,String name) {
        //分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.like(name != null, Setmeal::getName, name);
        lqw.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo,lqw);

        //对象拷贝（基于Page对象的除records属性之外的基本属性的拷贝）
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");

        List<Setmeal> setmealList = pageInfo.getRecords();

        //构造setmealDtoPage对象的records集合
        List<SetmealDto> setmealDtoList = setmealList.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();

            BeanUtils.copyProperties(item, setmealDto);

            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtoList);
        return Result.success(setmealDtoPage);
    }

    /**
     * 根据id查询套餐信息和关联菜品信息
     * 修改套餐时按id后台回显套餐信息及关联菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<SetmealDto> getById(@PathVariable Long id) {

        SetmealDto setmealDto = setmealService.getByIdWithDish(id);

        return Result.success(setmealDto);
    }

    /**
     * 按条件修改指定套餐
     * @param setmealDto
     * @return
     */
    @CacheEvict(value = "setmealCache",allEntries = true)
    @PutMapping
    public Result<String> update(@RequestBody SetmealDto setmealDto) {

        setmealService.updateWithDish(setmealDto);
        return Result.success("修改成功");
    }

    /**
     * 单删/批量删除套餐
     * @param ids
     * @return
     */
    @CacheEvict(value = "setmealCache",allEntries = true)
    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids) {
        //删除套餐需要删除两张表：setmeal、setmeal_dish，且后者按照外键约束规则应先删
        setmealService.deleteWithDish(ids);
        return Result.success("删除成功");
    }


    /**
     * 停售/批量停售
     * @param idArray
     * @return
     */
    @PostMapping("/status/0")
    public Result<String> updateStatus0(@RequestParam("ids") Long[] idArray) {

        for (Long id : idArray) {
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(0);
            setmealService.updateById(setmeal);
        }

        return Result.success("状态修改成功");
    }

    /**
     * 启售/批量启售
     * @param idArray
     * @return
     */
    @PostMapping("/status/1")
    public Result<String> updateStatus1(@RequestParam("ids") Long[] idArray) {

        for (Long id : idArray) {
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(1);
            setmealService.updateById(setmeal);
        }

        return Result.success("状态修改成功");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    @GetMapping("/list")
    public Result<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        lqw.eq( Setmeal::getStatus, 1);
        lqw.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> setmealList = setmealService.list(lqw);

        return Result.success(setmealList);
    }

}
