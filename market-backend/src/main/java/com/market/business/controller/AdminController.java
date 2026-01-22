package com.market.business.controller;

import com.market.business.global.PageVO;
import com.market.business.global.Result;
import com.market.business.query.AdminApproveQuery;
import com.market.business.query.MarketApproveQuery;
import com.market.business.service.AdminService;
import com.market.business.vo.AdminStatisticsVO;
import com.market.business.vo.MarketVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 管理员控制器
 *
 * @author echo
 * @date 2026/01/22
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@Tag(name = "管理员接口", description = "管理员审核市场相关接口")
public class AdminController {

    @Resource
    private AdminService adminService;

    /**
     * 分页查询审核市场列表
     */
    @Operation(summary = "分页查询审核市场列表", description = "根据状态查询待审核市场列表，返回列表数据和各状态统计")
    @PostMapping("/approveList")
    public Result<PageVO<MarketVO>> getApproveList(@Validated AdminApproveQuery query) {
        PageVO<MarketVO> result = adminService.getApproveList(query);
        return Result.success(result);
    }

    /**
     * 审批市场
     */
    @Operation(summary = "审批市场", description = "审批市场状态变更：待审核(0)→拒绝(1)/初审通过(2)，初审通过(2)→拒绝(1)/终审通过(3)")
    @GetMapping("/approve")
    public Result<Boolean> approveMarket(@Validated MarketApproveQuery query) {
        Boolean result = adminService.approveMarket(query);
        return Result.<Boolean>success().data(result);
    }

    /**
     * 获取管理员统计数据
     */
    @Operation(summary = "获取管理员统计数据", description = "获取总市场数、活跃市场数、待审核数、总流动性等统计信息")
    @GetMapping("/sum")
    public Result<AdminStatisticsVO> getStatistics() {
        AdminStatisticsVO stats = adminService.getStatistics();
        return Result.success(stats);
    }
}
