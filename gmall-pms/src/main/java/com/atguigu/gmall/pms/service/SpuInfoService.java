package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.atguigu.gmall.pmsinterface.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * spu信息
 *
 * @author dxh
 * @email dxh@atguigu.com
 * @date 2020-05-08 16:12:11
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo querySpu(QueryCondition queryCondition, Long catId);

    void bigSave(SpuInfoVO spuInfoVO);
}

