package com.natsumes.wezard.service.impl;

import com.natsumes.wezard.pojo.Category;
import com.natsumes.wezard.service.CategoryService;
import com.natsumes.wezard.entity.Response;
import com.natsumes.wezard.entity.vo.CategoryVo;
import com.natsumes.wezard.service.DubboCategoryService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.natsumes.wezard.consts.StefanieConst.ROOT_CATEGORY_PARENT_ID;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Reference
    private DubboCategoryService dubboCategoryService;

    /**
     * 耗时: http（请求微信API） > 磁盘 > 内存
     * mysql 内网 + 磁盘
     */
    @Override
    public Response<List<CategoryVo>> selectAll() {
        List<Category> categories = dubboCategoryService.selectAll();

        //查出parent_id=0
        // lambda + stream
        List<CategoryVo> categoryVos = categories.stream()
                .filter(e -> e.getParentId().equals(ROOT_CATEGORY_PARENT_ID))
                .map(this::category2CategoryVo)
                .sorted(Comparator.comparing(CategoryVo::getSortOrder).reversed())
                .collect(Collectors.toList());


        //查询子目录
        findSubCategory(categoryVos, categories);

        return Response.success(categoryVos);
    }

    @Override
    public void findSubCategoryId(Integer id, Set<Integer> resultSet) {
        List<Category> categories = dubboCategoryService.selectAll();
        findSubCategoryId(id, resultSet, categories);
    }

    private void findSubCategoryId(Integer id, Set<Integer> resultSet, List<Category> categories) {
        categories.forEach(category -> {
            if (category.getParentId().equals(id)) {
                resultSet.add(category.getId());
                findSubCategoryId(category.getId(), resultSet, categories);
            }
        });
    }

    private void findSubCategory(List<CategoryVo> categoryVos, List<Category> categories) {
        for (CategoryVo categoryVo : categoryVos) {
            List<CategoryVo> subCategoryVos = new ArrayList<>();
            for (Category category : categories) {
                //如果查到内容，设置子目录，继续往下查
                if (categoryVo.getId().equals(category.getParentId())) {
                    CategoryVo subCategoryVo = category2CategoryVo(category);
                    subCategoryVos.add(subCategoryVo);
                }

                subCategoryVos.sort(Comparator.comparing(CategoryVo::getSortOrder).reversed());
                categoryVo.setSubCategories(subCategoryVos);

                findSubCategory(subCategoryVos, categories);
            }
        }
    }

    private CategoryVo category2CategoryVo(Category category) {
        CategoryVo categoryVo = new CategoryVo();
        BeanUtils.copyProperties(category, categoryVo);
        return categoryVo;
    }
}
