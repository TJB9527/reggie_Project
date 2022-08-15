package com.bruce.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bruce.reggie.common.Result;
import com.bruce.reggie.dto.DishDto;
import com.bruce.reggie.entity.Category;
import com.bruce.reggie.entity.Dish;
import com.bruce.reggie.entity.DishFlavor;
import com.bruce.reggie.service.CategoryService;
import com.bruce.reggie.service.DishFlavorService;
import com.bruce.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 * 注：口味管理DishFlavor也使用该同一个controller处理不单独定义一个controller
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加菜品
     * 涉及两张表的插入操作: 1.菜品表dish 2.口味表dishflavor
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public Result<String> add(@RequestBody DishDto dishDto) {
        log.info("添加菜品：{}",dishDto.toString());
        //菜品表insert操作
        dishService.saveWithFlavor(dishDto);

        //清理所有菜品分类下的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //清理指定菜品分类下的缓存数据
        String key = "dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return Result.success("添加成功");
    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {

        //分页构造器
        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.like(name != null, Dish::getName, name);
        lqw.orderByDesc(Dish::getUpdateTime);

        //分页查询
        dishService.page(dishPage, lqw);

        //对象拷贝（基于Page对象的除records属性之外的基本属性的拷贝）
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");
        /*
        注意两个分页对象中的records属性不能直接拷贝
        因为正是要对records集合中属性进行处理(records集合正是分页查询结果)
         */
        List<Dish> dishList = dishPage.getRecords();

        List<DishDto> dishDtoList = dishList.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);  //将每个dish对象的普通属性拷贝到一一对应的dishDto对象中（基于dish对象）

            //通过dish获取菜品分类id
            Long categoryId = item.getCategoryId();
            //根据菜品分类id查询分类对象，获取分类名称
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);  //为每个dishDto设置对应categoryName
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(dishDtoList);
        return Result.success(dishDtoPage);
        /*
        不能直接传dishPage，因为dishPage中records集合的每个dish对象即分页中每条菜品记录传的菜品分类字段是categoryId
        而前端要求展示的分页显示的列除了菜品名称、图片、售价、状态等对应dish对象属性的列外还要求展示菜品分类列，且该列要求显示的是菜品分类名称，如川菜、粤菜...
        而经分页查询得到的dishPage中只有菜品分类id即categoryId属性，前端要的是一个字段catgeoryName即菜品分类名称，因此需要引入dto且赋予一属性categoryName
        由dishPage中的categoryId转换处理得到categoryName（因为在category菜品分类表中categoryId与菜品分类名是一一对应的可以直接转换得到）
         */
    }

    /**
     * 根据id查询菜品信息和口味信息
     * 修改菜品时按id后台回显菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishDto> getById(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return Result.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto) {
        log.info("修改菜品：{}",dishDto.toString());
        //菜品表insert操作
        dishService.updateWithFlavor(dishDto);

        //清理所有菜品分类下的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //清理指定菜品分类下的缓存数据
        String key = "dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return Result.success("添加成功");
    }


    /**
     * 单删/批量删除
     * 单删与批量删除无非请求参数个数不同，用数组接收遍历单删即可
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids) {
        //要删两张表：dish、dish_flavor，注意按外键约束需要先删口味表再删菜品表

        dishService.deleteWithFlavor(ids);

        //清理所有菜品分类下的缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

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
            Dish dish = dishService.getById(id);
            dish.setStatus(0);
            boolean updateById = dishService.updateById(dish);
        }

        return Result.success("状态修改成功");
    }


    /**
     * 起售/批量起售
     * @param idArray
     * @return
     */
    @PostMapping("/status/1")
    public Result<String> updateStatus1(@RequestParam("ids") Long[] idArray)  {

        for (Long id : idArray) {
            Dish dish = dishService.getById(id);
            dish.setStatus(1);
            boolean updateById = dishService.updateById(dish);
        }
        return Result.success("状态修改成功");
    }

    /**
     * 根据条件查询对应分类下的菜品
     * 场景一：“添加套餐”功能页点击“添加菜品”功能提供当前指定菜品分类Id下的所有菜品(返回List<Dish>即可)
     * 场景二：移动用户端登录后进入首页需要根据首页左侧列表指定的菜品分类id在页面右侧展示对应菜品数据，且需要显示口味数据（返回List<DishDto>）使得页面能显示“选择规格”按钮
     * 两个场景使用如下同一个功能实现，返回值为List<DishDto>对场景一返回List<Dish>没有影响，只是在dish对象的基础上追加了口味数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public Result<List<DishDto>> list(Dish dish) {

        /*
        1. 查缓存，缓存中有要的数据直接从缓存中取
        2. 缓存中没有数据则查数据库，并将查询的结果(指定分类id下的菜品数据包括口味数据即list<DishDto>)添加到缓存中
           以String类型进行缓存：key为dish_categoryId_status，value为对应categoryId下的list<DishDto>
           注：缓存的形式也可以精确到菜品对象（以hash类型存放，key : key-value形式可以是categoryId_status：dishDto.getId()-dishDto），
              上述只精确到某个菜品分类下的菜品集合（也足够），主要应用于移动端用户在主页切换菜品分类或套餐分类(高并发)能及时从缓存中取到对应分类下的菜品/套餐集合
         */

        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();

        //查缓存中对应分类id是否存在
        if (redisTemplate.hasKey(key)) {
            //存在，返回缓存中的菜品数据
            List<DishDto> dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
            return Result.success(dishDtoList);
        }
        //不存在，查询数据库，并将查询的对应分类下的菜品集合list添加到缓存中，设置缓存时间为60分钟
        List<DishDto> dishDtoList = listQuery(dish);

        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return Result.success(dishDtoList);
    }

    /**
     *从数据库中查询指定分类下的菜品数据及每个菜品对应的口味数据
     * @param dish
     * @return
     */
    public List<DishDto> listQuery(Dish dish) {
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        lqw.eq(Dish::getStatus, 1);
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        //查询
        List<Dish> listDish = dishService.list(lqw);

        //遍历菜品集合，获取List<DishDto>
        List<DishDto> listDishDto = null;

        listDishDto = listDish.stream().map((item) -> {
            //对每个菜品创建对应DishDto对象，拷贝基本属性
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            //获取每个菜品的口味集合，然后设置DishDto对象的list<DishFlavor>
            LambdaQueryWrapper<DishFlavor> lqw1 = new LambdaQueryWrapper<>();
            lqw1.eq(DishFlavor::getDishId, item.getId());

            List<DishFlavor> listDishFlavor = dishFlavorService.list(lqw1);

            dishDto.setFlavors(listDishFlavor);

            return dishDto;
        }).collect(Collectors.toList());

        return listDishDto;
    }

}
