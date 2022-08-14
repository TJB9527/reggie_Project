package com.bruce.reggie.dto;

import com.bruce.reggie.entity.Dish;
import com.bruce.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();  //前端传递的一个菜品的口味可能不止一个(辣度、甜度、温度、忌口)，每个口味含name（口味名称）、value两个属性
                                                           // 每个口味都对应用DishFlavor对象封装(仅接收name和value属性，其他为空)，多个口味对应对象用list集合保存
    private String categoryName;

    private Integer copies;
}
