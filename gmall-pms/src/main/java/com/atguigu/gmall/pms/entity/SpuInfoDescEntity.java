package com.atguigu.gmall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * spu信息介绍
 * 
 * @author dxh
 * @email dxh@atguigu.com
 * @date 2020-05-08 16:12:11
 */
@ApiModel
@Data
@TableName("pms_spu_info_desc")
public class SpuInfoDescEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 商品id
	 */
	//设置该主键策略不遵循全局设置,不是自增了是自己设置该主键
	@TableId(type = IdType.INPUT)
	@ApiModelProperty(name = "spuId",value = "商品id")
	private Long spuId;
	/**
	 * 商品介绍
	 */
	@ApiModelProperty(name = "decript",value = "商品介绍")
	private String decript;

}
