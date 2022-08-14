package com.bruce.reggie.dto;

import com.bruce.reggie.entity.Setmeal;
import com.bruce.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
