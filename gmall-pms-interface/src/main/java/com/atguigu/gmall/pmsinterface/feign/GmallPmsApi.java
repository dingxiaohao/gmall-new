package com.atguigu.gmall.pmsinterface.feign;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pmsinterface.entity.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallPmsApi {
    @PostMapping("pms/spuinfo/page")
    public Resp<List<SpuInfoEntity>> querySpuByPage(@RequestBody QueryCondition queryCondition);

    @GetMapping("pms/skuinfo/{spuId}")
    public Resp<List<SkuInfoEntity>> querySkuBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/brand/info/{brandId}")
    public Resp<BrandEntity> queryBrandById(@PathVariable("brandId") Long brandId);

    @GetMapping("pms/category/info/{catId}")
    public Resp<CategoryEntity> queryCategoryById(@PathVariable("catId") Long catId);

    @GetMapping("pms/productattrvalue/{spuId}")
    public Resp<List<ProductAttrValueEntity>> querySearchAttrsBySpuId(@PathVariable("spuId") Long spuId);
}
