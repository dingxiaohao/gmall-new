package com.atguigu.gmall.index.service;

import com.atguigu.gmall.pmsinterface.entity.CategoryEntity;
import com.atguigu.gmall.pmsinterface.entity.vo.CategoryVO;

import java.util.List;

public interface IndexService {
    List<CategoryEntity> queryLevel1();

    List<CategoryVO> querySubByPid(Long pid);
}
