package cn.lemonit.lemserver.controller;

import cn.lemonit.lemserver.domian.Result;
import cn.lemonit.lemserver.domian.Tag;
import cn.lemonit.lemserver.service.AppService;
import cn.lemonit.lemserver.service.PublishService;
import cn.lemonit.lemserver.service.TagService;
import cn.lemonit.lemserver.utils.ErrorMsg;
import cn.lemonit.lemserver.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/tag")
public class TagController {
    @Autowired
    private TagService tagService;

    @Autowired
    private PublishService publishService;

    public Date getDate (){
        return new Timestamp(new Date().getTime());
    };

    //创建tag
    @PostMapping("")
    public Result createTag (@RequestBody Tag tag){
        //创建tag
        tag.setCreateTime(getDate());
        tag.setTagKey(UUID.randomUUID().toString());

        //检查app下有没有同名tag
        tagService.selectByNameInApp(tag);
        //新建
        tagService.insertSelective(tag);
        return ResultUtil.success(tagService.selectByPrimaryKey(tag.getTagKey()));
    }

    //修改tag
    @PutMapping("")
    public Result updateTag (@RequestBody Tag tag){
        Tag record = tagService.selectByPrimaryKey(tag.getTagKey());
        record.setTagName(tag.getTagName());
        record.setTagDescription(tag.getTagDescription());
        //检查app下有没有同名tag
        tagService.selectByNameInApp(record);
        tagService.updateByPrimaryKeySelective(record);
        return ResultUtil.success(tagService.selectByPrimaryKey(tag.getTagKey()));
    }

    //查询tag
    @GetMapping("")
    public Result selectByPrimaryKey (@RequestParam String tagKey){
        return ResultUtil.success(tagService.selectByPrimaryKey(tagKey));
    }

    //查询tag列表
    @GetMapping("/list")
    public Result selecttag (@RequestParam(defaultValue="1") Integer page, @RequestParam(defaultValue = "10") Integer limit){
        return  ResultUtil.success(tagService.listByPageinfo((page-1)*limit,limit));
    }

    @DeleteMapping("")
    public Result deleteByPrimaryKey(@RequestBody HashMap tag){
        String tagKey = tag.get("tagKey").toString();

        //是否有tag和version绑定
        if(publishService.selectByTagkey(tagKey)!=null){
            return ResultUtil.error(ErrorMsg.version_has_related_tag.toString());
        };

        //删除
        tagService.deleteByPrimaryKey(tagKey);

        return ResultUtil.success(null);
    }
}
