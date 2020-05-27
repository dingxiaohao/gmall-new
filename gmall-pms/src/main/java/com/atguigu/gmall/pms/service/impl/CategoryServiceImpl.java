package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.vo.CategoryVO;
import com.atguigu.gmall.pmsinterface.entity.CategoryEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.CategoryDao;
import com.atguigu.gmall.pms.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryDao categoryDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategoryLevelOrParentCid(Integer level, Long pid) {
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        if (level != null && level != 0) {
            wrapper.eq("cat_level",level);
        }


        if (pid != null){
            wrapper.eq("parent_cid",pid);
        }

        return categoryDao.selectList(wrapper);
    }

    @Override
    public List<CategoryVO> querySubCategory(Long pid) {
        //根据鼠标指上的一级分类id查询对应的二级分类集合,一级分类下的二级分类
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", pid));

        //通过stream流把二级分类集合转为vo对象集合
        //二级分类和三级分类的集合
        List<CategoryVO> categoryVOList = categoryEntityList.stream().map(categoryEntity -> {
            CategoryVO categoryVO = new CategoryVO();
            BeanUtils.copyProperties(categoryEntity, categoryVO);
            return categoryVO;
        }).collect(Collectors.toList());

        //遍历二级分类集合,获取id再根据这个id查询父id相等的三级分类
        //查询二级分类下的三级分类,和一级分类下的二级分类一样的
        //categoryVO这个就是循环的每个元素,每次循环元素都不一样获取的值也不一样
        //每次查询出来的数据也不一样,设置的数据也不一样,每个对象都设置一下
        categoryVOList.forEach(categoryVO -> {
            //通过二级分类的pid等于三级分类,获取到三级分类集合
            List<CategoryEntity> subs = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", categoryVO.getCatId()));
            //再把三级分类集合组装到vo对象中进行返回
            //vo对象里有sub子属性,就是分类下的子分类
            categoryVO.setSubs(subs);
        });

        //设置完成后返回数据给前端
        return categoryVOList;
    }

}