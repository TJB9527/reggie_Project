package com.bruce.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bruce.reggie.common.Result;
import com.bruce.reggie.entity.Category;

public interface CategoryService extends IService<Category> {

    Result<String> remove(Long id);
}
