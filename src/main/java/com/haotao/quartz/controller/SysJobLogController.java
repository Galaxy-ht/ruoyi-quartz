package com.haotao.quartz.controller;

import com.haotao.quartz.annotation.Log;
import com.haotao.quartz.core.domain.AjaxResult;
import com.haotao.quartz.core.page.TableDataInfo;
import com.haotao.quartz.domain.SysJob;
import com.haotao.quartz.domain.SysJobLog;
import com.haotao.quartz.enums.BusinessType;
import com.haotao.quartz.service.ISysJobLogService;
import com.haotao.quartz.service.ISysJobService;
import com.haotao.quartz.utils.StringUtils;
import com.haotao.quartz.utils.poi.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 调度日志操作处理
 *
 * @author haotao
 */
@Api(tags = "SysJobLogController", description = "定时任务执行日志Api")
@Controller
@RequestMapping("/monitor/jobLog")
public class SysJobLogController extends BaseController {
    private String prefix = "monitor/job";

    @Autowired
    private ISysJobService jobService;

    @Autowired
    private ISysJobLogService jobLogService;

    @GetMapping()
    public String jobLog(@RequestParam(value = "jobId", required = false) Long jobId, ModelMap mmap) {
        if (StringUtils.isNotNull(jobId)) {
            SysJob job = jobService.selectJobById(jobId);
            mmap.put("job", job);
        }
        return prefix + "/jobLog";
    }

    @ApiOperation("调度日志list方法")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jobLogId", value = "日志序号", required = false, paramType = "query", dataType = "Long"),
            @ApiImplicitParam(name = "jobName", value = "任务名称", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "jobGroup", value = "任务组名", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "invokeTarget", value = "调用目标字符串", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "jobMessage", value = "日志信息", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "status", value = "执行状态（0正常 1失败）", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "exceptionInfo", value = "异常信息", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "searchValue", value = "搜索值", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createBy", value = "创建者", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createTime", value = "创建时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "updateBy", value = "更新者", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "updateTime", value = "更新时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "remark", value = "备注", required = false, paramType = "query", dataType = "String")
    })
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(SysJobLog jobLog) {
        startPage();
        List<SysJobLog> list = jobLogService.selectJobLogList(jobLog);
        return getDataTable(list);
    }

    @ApiOperation("调度日志导出Excel方法")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jobLogId", value = "日志序号", required = true, paramType = "query", dataType = "Long"),
            @ApiImplicitParam(name = "jobName", value = "任务名称", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "jobGroup", value = "任务组名", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "invokeTarget", value = "调用目标字符串", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "jobMessage", value = "日志信息", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "status", value = "执行状态（0正常 1失败）", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "exceptionInfo", value = "异常信息", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "searchValue", value = "搜索值", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createBy", value = "创建者", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createTime", value = "创建时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "updateBy", value = "更新者", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "updateTime", value = "更新时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "remark", value = "备注", required = false, paramType = "query", dataType = "String")
    })
    @Log(title = "调度日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(SysJobLog jobLog) {
        List<SysJobLog> list = jobLogService.selectJobLogList(jobLog);
        ExcelUtil<SysJobLog> util = new ExcelUtil<SysJobLog>(SysJobLog.class);
        return util.exportExcel(list, "调度日志");
    }

    @ApiOperation("删除调度日志方法")
    @Log(title = "调度日志", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(jobLogService.deleteJobLogByIds(ids));
    }

    @ApiOperation("查看调度日志详情方法")
    @GetMapping("/detail/{jobLogId}")
    public String detail(@PathVariable("jobLogId") Long jobLogId, ModelMap mmap) {
        mmap.put("name", "jobLog");
        mmap.put("jobLog", jobLogService.selectJobLogById(jobLogId));
        return prefix + "/detail";
    }

    @ApiOperation("清空调度日志详情方法")
    @Log(title = "调度日志", businessType = BusinessType.CLEAN)
    @PostMapping("/clean")
    @ResponseBody
    public AjaxResult clean() {
        jobLogService.cleanJobLog();
        return success();
    }
}
