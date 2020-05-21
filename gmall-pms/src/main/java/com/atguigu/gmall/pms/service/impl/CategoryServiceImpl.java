package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pmsinterface.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
    public List<CategoryEntity> queryCategoryLevelOrParentCid(Map<String, Object> map) {
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        if (map.get("level") != null ) {
            //因为get的是object但是我知道是什么类型就转成什么类型，否则和数据库类型对不上查询失败
            Integer level = Integer.parseInt(map.get("level").toString());
            if ( level != 0){
                //如果上面不转类型的话直接object和integer在数据库比较会出问题的
                //所以要转成和数据库类型一致的
                wrapper.eq("cat_level",level);
            }
        }


        if (map.get("parentCid") != null){
            //object转long或者integer会出现错误所以要先转为string再用parse包装类方法转成想要的类型
            //这里是必须要转的，否则查询判断时object类型和数据库的类型不一样在数据库是无法查询的
            Long parentCid = Long.parseLong(map.get("parentCid").toString());
            wrapper.eq("parent_cid",parentCid);
        }

        return categoryDao.selectList(wrapper);
    }

}