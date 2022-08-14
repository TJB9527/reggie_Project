package com.bruce.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bruce.reggie.common.Result;
import com.bruce.reggie.entity.Category;
import com.bruce.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 添加分类（包括菜品分类和套餐分类）
     * @param category
     * @return
     */
    @PostMapping
    public Result<String> addCategory(@RequestBody Category category) {
        log.info("category：{}",category);
        boolean save = categoryService.save(category);
        return save == true ? Result.success("添加分类成功") : Result.error("添加分类失败");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Result<Page> getPage(int page,int pageSize) {
        //分页构造器
        Page<Category> pageInfo = new Page<>(page, pageSize);

        //条件构造器，根据sort进行排序
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        //进行分页查询
        categoryService.page(pageInfo, lqw);

        return Result.success(pageInfo);
    }

    /**
     * 根据d删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    public Result<String> delete(@RequestParam("ids") long id){
        log.info("删除分类，id：{}",id);
        Result<String> ret = categoryService.remove(id);
        return ret;
    }

    /**
     * 根据id修改分类
     * @param category
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody Category category) {
        log.info("修改分类信息：{}",category);

        categoryService.updateById(category);
        return Result.success("修改成功");
    }

    /**
     * 根据条件查询菜品分类
     * 该功能主要用于添加菜品或修改某菜品时进入菜品的add.html页面在页面右上角”菜品分类“下拉框中单击可显示菜品分类列表
     * @param category
     * @return
     */
    @GetMapping("/list")
    public Result<List<Category>> list(Category category) {
        //条件构造器
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.eq(null != category.getType(),Category::getType, category.getType());

        //添加排序条件
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        //查询
        List<Category> list = categoryService.list(lqw);
        return Result.success(list);
    }

}
