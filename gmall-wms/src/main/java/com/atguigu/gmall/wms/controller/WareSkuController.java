package com.atguigu.gmall.wms.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;




/**
 * 商品库存
 *
 * @author dxh
 * @email dxh@atguigu.com
 * @date 2020-05-08 18:12:33
 */
@Api(tags = "商品库存 管理")
@RestController
@RequestMapping("wms/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    //根据skuid查询具体商品的库存
    @GetMapping("{skuId}")
    public Resp<List<WareSkuEntity>> queryListBySkuId(@PathVariable Long skuId){
        //QueryWrapper里泛型填的就是实体类类型,一个实体类对应一个表,就是代表这个条件针对哪个表了
        List<WareSkuEntity> list = wareSkuService.list(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId));
        return Resp.ok(list);
    }

    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('wms:waresku:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = wareSkuService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{id}")
    @PreAuthorize("hasAuthority('wms:waresku:info')")
    public Resp<WareSkuEntity> info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return Resp.ok(wareSku);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('wms:waresku:save')")
    public Resp<Object> save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('wms:waresku:update')")
    public Resp<Object> update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('wms:waresku:delete')")
    public Resp<Object> delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return Resp.ok(null);
    }

}
