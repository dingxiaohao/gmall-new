package com.atguigu.gmall.pms.controller;

import java.util.Arrays;
import java.util.List;


import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pmsinterface.entity.AttrGroupEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gmall.pms.service.AttrGroupService;




/**
 * 属性分组
 *
 * @author dxh
 * @email dxh@atguigu.com
 * @date 2020-05-08 16:12:11
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;


    @GetMapping("/withattrs/cat/{catId}")
    public Resp<List<GroupVo>> queryAttrAndGroupByCatId(@PathVariable Long catId){
        List<GroupVo> list = attrGroupService.queryAttrAndGroupByCatId(catId);
        return Resp.ok(list);
    }


    //GroupVo就是返回给前端的查询结果对象,刚好是前端需要的数据
    @GetMapping("withattr/{gid}")
    public Resp<GroupVo> queryGroupVoByGid(@PathVariable Long gid){
        GroupVo groupVo = attrGroupService.queryGroupVoByGid(gid);
        return Resp.ok(groupVo);
    }


    //接收占位符参数
    @GetMapping("{catelogId}")     //QueryCondition是分页对象,传的分页参数框架就自动保存到这个对象属性中,不需要注解接收对象
    //PathVariable接收路径的占位符参数，根据三级分类id查询规格参数的组
    public Resp<PageVo> queryAttrGroupByCid(QueryCondition queryCondition,@PathVariable Long catelogId){
        PageVo page = attrGroupService.queryAttrGroupByCid(queryCondition,catelogId);
        return Resp.ok(page);
    }

    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:attrgroup:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = attrGroupService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{attrGroupId}")
    @PreAuthorize("hasAuthority('pms:attrgroup:info')")
    public Resp<AttrGroupEntity> info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        return Resp.ok(attrGroup);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:attrgroup:save')")
    public Resp<Object> save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:attrgroup:update')")
    public Resp<Object> update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:attrgroup:delete')")
    public Resp<Object> delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return Resp.ok(null);
    }

}
