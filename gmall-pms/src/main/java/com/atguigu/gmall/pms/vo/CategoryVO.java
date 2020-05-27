package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pmsinterface.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

@Data
public class CategoryVO extends CategoryEntity {
    private List<CategoryEntity> subs;
}
