package com.natsumes.wezard.controller;

import com.natsumes.wezard.entity.Response;
import com.natsumes.wezard.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {


    @Autowired
    private CategoryService categoryService;

    @GetMapping("/categories")
    public Response selectAll() {
        return categoryService.selectAll();
    }
}
