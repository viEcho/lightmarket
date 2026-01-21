package com.market.business.controller;

import cn.hutool.core.lang.Assert;
import com.market.business.enums.AiEnum;
import com.market.business.enums.TagEnum;
import com.market.business.global.PageVO;
import com.market.business.global.Result;
import com.market.business.query.MarketAddQuery;
import com.market.business.query.MarketPageQuery;
import com.market.business.service.MarketService;
import com.market.business.vo.MarketVO;
import com.market.business.vo.Option;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 市场控制器
 */
@RestController
@RequestMapping("/market")
public class MarketController {

    @Resource
    private MarketService marketService;

    /**
     * 获取配置枚举
     */
    @Operation(summary = "获取配置枚举", description = "前端调用枚举，有多个传多个值")
    @GetMapping("/options")
    public Result<Map<String, List<Option<Integer>>>> getOptions(@RequestParam("types") String types) {
        Assert.notEmpty(types);
        Map<String, List<Option<Integer>>> optionsMap = new HashMap<>();
        for (String tag : types.split(",")) {
            switch (tag){
                case "tag":
                    optionsMap.put(tag, Option.fromEnum(TagEnum.class));
                    break;
                case "ai":
                    optionsMap.put(tag, Option.fromEnum(AiEnum.class));
                    break;
                default:
                    break;
            }
        }
        return Result.success(optionsMap);
    }

    /**
     * 分页查询市场列表
     */
    @Operation(summary = "分页查询市场列表", description = "获取首页市场列表，默认返回6条数据")
    @PostMapping("/findList")
    public Result<PageVO<MarketVO>> findList(@RequestBody MarketPageQuery query) {
        PageVO<MarketVO> page = marketService.findPage(query);
        return Result.success(page);
    }

    /**
     * 分页查询用户参与的市场列表
     */
    @Operation(summary = "分页查询用户参与的市场列表", description = "获取用户参与的市场列表，按参与时间倒序")
    @PostMapping("/findMyList")
    public Result<PageVO<MarketVO>> findMyList(@RequestBody MarketPageQuery query) {
        PageVO<MarketVO> page = marketService.findMyList(query);
        return Result.success(page);
    }

    /**
     * 创建市场
     */
    @Operation(summary = "创建市场", description = "创建新的预测市场，提交后进入待审核状态")
    @PostMapping("/add")
    public Result<String> add(@Validated MarketAddQuery query,
                              @RequestParam("userId") Long userId) {
        String marketId = marketService.add(query, userId);
        return Result.success(marketId);
    }

}
