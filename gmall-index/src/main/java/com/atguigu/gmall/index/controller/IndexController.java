package com.atguigu.gmall.index.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pmsinterface.entity.CategoryEntity;
import com.atguigu.gmall.pmsinterface.entity.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/index")
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping("/cates")
    public Resp<List<CategoryEntity>> queryLevel1(){
        List<CategoryEntity> categoryEntityList = indexService.queryLevel1();
        return Resp.ok(categoryEntityList);
    }
    //再根据一级分类id查底下的二级分类和二级分类下的三级分类
    @GetMapping("/cates/{pid}")         //传的是鼠标指上的一级分类id
    public Resp<List<CategoryVO>> querySubByPid(@PathVariable("pid") Long pid){
        List<CategoryVO> categoryVOS = indexService.querySubByPid(pid);
        return Resp.ok(categoryVOS);
    }
}
