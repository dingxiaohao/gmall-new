package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
import com.atguigu.gmall.sms.dao.SkuLadderDao;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import dto.SaleDTO;
import org.springframework.beans.BeanUtils;
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

import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private SkuLadderDao skuLadderDao;

    @Autowired
    private SkuFullReductionDao skuFullReductionDao;


    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageVo(page);
    }

    @Transactional
    @Override
    public void saveSaleInfo(SaleDTO saleDTO) {
        // 3.1. 积分优惠skuBoundsEntity
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(saleDTO,skuBoundsEntity);
        List<Integer> work = saleDTO.getWork();
        //如果这个集合不为空加了!取反,这个方法默认是空的话是true,不为空false
        if (!CollectionUtils.isEmpty(work)){
            skuBoundsEntity.setWork(work.get(0) * 8 + work.get(1) * 4 + work.get(2) * 2 + work.get(3));
        }
        this.save(skuBoundsEntity);

        // 3.2. 满减优惠
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        //拷贝一个对象的属性到另一个对象,前提是属性名和属性类型一致才可以拷贝
        //拷贝同意的属性类型和属性名进行拷贝,左边的对象属性拷贝到右边的对象属性上
        //如果有多余的属性也不会拷贝属性值的,因为属性名和属性类型没有对上是不会拷贝的
        //不管是左边还是右边的有多余的属性也都不会拷贝赋值的
        //只有属性名和属性类型完全一样的才可以拷贝赋值
        //多余的是不会拷贝赋值的,不管是左边还是右边多余都不会拷贝赋值的,因为属性名和属性类型对不上
        //只有属性名和属性类型完全一样的属性才可以通过BeanUtils拷贝对象的属性值到另一个对象
        BeanUtils.copyProperties(saleDTO,skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(saleDTO.getFullAddOther());
        skuFullReductionDao.insert(skuFullReductionEntity);

        // 3.3. 数量折扣
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(saleDTO,skuLadderEntity);
        //这个对象里还有一个折后价属性,这个属性是要根据实时计算的
        skuLadderDao.insert(skuLadderEntity);
    }

}