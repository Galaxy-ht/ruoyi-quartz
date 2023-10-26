package com.haotao.quartz.controller;

import com.haotao.quartz.annotation.Log;
import com.haotao.quartz.constant.Constants;
import com.haotao.quartz.core.domain.AjaxResult;
import com.haotao.quartz.core.page.TableDataInfo;
import com.haotao.quartz.enums.BusinessType;
import com.haotao.quartz.exception.job.TaskException;
import com.haotao.quartz.utils.StringUtils;
import com.haotao.quartz.utils.poi.ExcelUtil;
import com.haotao.quartz.domain.SysJob;
import com.haotao.quartz.service.ISysJobService;
import com.haotao.quartz.utils.CronUtils;
import com.haotao.quartz.utils.ScheduleUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 调度任务信息操作处理
 * 
 * @author haotao
 */
@Controller
@RequestMapping("/monitor/job")
@Api(tags = "SysJobController", description = "定时任务Api")
public class SysJobController extends BaseController
{
    private String prefix = "monitor/job";

    @Autowired
    private ISysJobService jobService;

    @GetMapping()
    public String job()
    {
        return prefix + "/job";
    }

    @ApiOperation("定时任务list方法")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jobId", value = "任务序号", required = false, paramType = "query", dataType = "Long"),
            @ApiImplicitParam(name = "jobName", value = "任务名称", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "jobGroup", value = "任务组名", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "invokeTarget", value = "调用目标字符串", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "cronExpression", value = "cron执行表达式", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "misfirePolicy", value = "cron计划策略 （0默认 1立即触发 2触发一次执行 3不触发）", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "concurrent", value = "是否并发执行（0允许 1禁止）", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "status", value = "任务状态（0正常 1暂停）", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "searchValue", value = "搜索值", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createBy", value = "创建者", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createTime", value = "创建时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "updateBy", value = "更新者", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "updateTime", value = "更新时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "remark", value = "备注", required = false, paramType = "query", dataType = "String")
    })
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(SysJob job)
    {
        startPage();
        List<SysJob> list = jobService.selectJobList(job);
        return getDataTable(list);
    }

    @ApiOperation("定时任务导出Excel方法")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jobId", value = "任务序号", required = true, paramType = "query", dataType = "Long"),
            @ApiImplicitParam(name = "jobName", value = "任务名称", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "jobGroup", value = "任务组名", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "invokeTarget", value = "调用目标字符串", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "cronExpression", value = "cron执行表达式", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "misfirePolicy", value = "cron计划策略 （0默认 1立即触发 2触发一次执行 3不触发）", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "concurrent", value = "是否并发执行（0允许 1禁止）", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "status", value = "任务状态（0正常 1暂停）", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "searchValue", value = "搜索值", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createBy", value = "创建者", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createTime", value = "创建时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "updateBy", value = "更新者", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "updateTime", value = "更新时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "remark", value = "备注", required = false, paramType = "query", dataType = "String")
    })
    @Log(title = "定时任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(SysJob job)
    {
        List<SysJob> list = jobService.selectJobList(job);
        ExcelUtil<SysJob> util = new ExcelUtil<SysJob>(SysJob.class);
        return util.exportExcel(list, "定时任务");
    }

    @ApiOperation("定时任务删除方法")
    @Log(title = "定时任务", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) throws SchedulerException
    {
        jobService.deleteJobByIds(ids);
        return success();
    }

    @ApiOperation("定时任务查看详细信息方法")
    @GetMapping("/detail/{jobId}")
    public String detail(@PathVariable("jobId") Long jobId, ModelMap mmap)
    {
        mmap.put("name", "job");
        mmap.put("job", jobService.selectJobById(jobId));
        return prefix + "/detail";
    }

    /**
     * 任务调度状态修改
     */
    @ApiOperation("定时任务修改状态方法")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jobId", value = "任务序号", required = true, paramType = "query", dataType = "Long"),
            @ApiImplicitParam(name = "jobName", value = "任务名称", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "jobGroup", value = "任务组名", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "invokeTarget", value = "调用目标字符串", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "cronExpression", value = "cron执行表达式", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "misfirePolicy", value = "cron计划策略 （0默认 1立即触发 2触发一次执行 3不触发）", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "concurrent", value = "是否并发执行（0允许 1禁止）", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "status", value = "任务状态（0正常 1暂停）", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "searchValue", value = "搜索值", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createBy", value = "创建者", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createTime", value = "创建时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "updateBy", value = "更新者", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "updateTime", value = "更新时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "remark", value = "备注", required = false, paramType = "query", dataType = "String")
    })
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PostMapping("/changeStatus")
    @ResponseBody
    public AjaxResult changeStatus(SysJob job) throws SchedulerException
    {
        SysJob newJob = jobService.selectJobById(job.getJobId());
        newJob.setStatus(job.getStatus());
        return toAjax(jobService.changeStatus(newJob));
    }

    /**
     * 任务调度立即执行一次
     */
    @ApiOperation("任务调度立即执行一次")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jobId", value = "任务序号", required = true, paramType = "query", dataType = "Long"),
            @ApiImplicitParam(name = "jobName", value = "任务名称", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "jobGroup", value = "任务组名", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "invokeTarget", value = "调用目标字符串", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "cronExpression", value = "cron执行表达式", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "misfirePolicy", value = "cron计划策略 （0默认 1立即触发 2触发一次执行 3不触发）", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "concurrent", value = "是否并发执行（0允许 1禁止）", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "status", value = "任务状态（0正常 1暂停）", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "searchValue", value = "搜索值", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createBy", value = "创建者", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createTime", value = "创建时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "updateBy", value = "更新者", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "updateTime", value = "更新时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "remark", value = "备注", required = false, paramType = "query", dataType = "String")
    })
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PostMapping("/run")
    @ResponseBody
    public AjaxResult run(SysJob job) throws SchedulerException
    {
        boolean result = jobService.run(job);
        return result ? success() : error("任务不存在或已过期！");
    }

    /**
     * 新增调度
     */
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存调度
     */
    @ApiOperation("保存新增调度任务")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jobId", value = "任务序号", required = true, paramType = "query", dataType = "Long"),
            @ApiImplicitParam(name = "jobName", value = "任务名称", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "jobGroup", value = "任务组名", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "invokeTarget", value = "调用目标字符串", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "cronExpression", value = "cron执行表达式", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "misfirePolicy", value = "cron计划策略 （0默认 1立即触发 2触发一次执行 3不触发）", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "concurrent", value = "是否并发执行（0允许 1禁止）", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "status", value = "任务状态（0正常 1暂停）", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "searchValue", value = "搜索值", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createBy", value = "创建者", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createTime", value = "创建时间", required = true, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "updateBy", value = "更新者", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "updateTime", value = "更新时间", required = true, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "remark", value = "备注", required = true, paramType = "query", dataType = "String")
    })
    @Log(title = "定时任务", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated SysJob job) throws SchedulerException, TaskException
    {
        if (!CronUtils.isValid(job.getCronExpression()))
        {
            return error("新增任务'" + job.getJobName() + "'失败，Cron表达式不正确");
        }
        else if (StringUtils.containsIgnoreCase(job.getInvokeTarget(), Constants.LOOKUP_RMI))
        {
            return error("新增任务'" + job.getJobName() + "'失败，目标字符串不允许'rmi'调用");
        }
        else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[] { Constants.LOOKUP_LDAP, Constants.LOOKUP_LDAPS }))
        {
            return error("新增任务'" + job.getJobName() + "'失败，目标字符串不允许'ldap(s)'调用");
        }
        else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[] { Constants.HTTP, Constants.HTTPS }))
        {
            return error("新增任务'" + job.getJobName() + "'失败，目标字符串不允许'http(s)'调用");
        }
        else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.JOB_ERROR_STR))
        {
            return error("新增任务'" + job.getJobName() + "'失败，目标字符串存在违规");
        }
        else if (!ScheduleUtils.whiteList(job.getInvokeTarget()))
        {
            return error("新增任务'" + job.getJobName() + "'失败，目标字符串不在白名单内");
        }
        job.setCreateBy("user");
        return toAjax(jobService.insertJob(job));
    }

    /**
     * 修改调度
     */
    @GetMapping("/edit/{jobId}")
    public String edit(@PathVariable("jobId") Long jobId, ModelMap mmap)
    {
        mmap.put("job", jobService.selectJobById(jobId));
        return prefix + "/edit";
    }

    /**
     * 修改保存调度
     */
    @ApiOperation("保存修改调度任务")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jobId", value = "任务序号", required = true, paramType = "query", dataType = "Long"),
            @ApiImplicitParam(name = "jobName", value = "任务名称", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "jobGroup", value = "任务组名", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "invokeTarget", value = "调用目标字符串", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "cronExpression", value = "cron执行表达式", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "misfirePolicy", value = "cron计划策略 （0默认 1立即触发 2触发一次执行 3不触发）", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "concurrent", value = "是否并发执行（0允许 1禁止）", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "status", value = "任务状态（0正常 1暂停）", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "searchValue", value = "搜索值", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createBy", value = "创建者", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createTime", value = "创建时间", required = true, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "updateBy", value = "更新者", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "updateTime", value = "更新时间", required = true, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "remark", value = "备注", required = true, paramType = "query", dataType = "String")
    })
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated SysJob job) throws SchedulerException, TaskException
    {
        if (!CronUtils.isValid(job.getCronExpression()))
        {
            return error("修改任务'" + job.getJobName() + "'失败，Cron表达式不正确");
        }
        else if (StringUtils.containsIgnoreCase(job.getInvokeTarget(), Constants.LOOKUP_RMI))
        {
            return error("修改任务'" + job.getJobName() + "'失败，目标字符串不允许'rmi'调用");
        }
        else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[] { Constants.LOOKUP_LDAP, Constants.LOOKUP_LDAPS }))
        {
            return error("修改任务'" + job.getJobName() + "'失败，目标字符串不允许'ldap'调用");
        }
        else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[] { Constants.HTTP, Constants.HTTPS }))
        {
            return error("修改任务'" + job.getJobName() + "'失败，目标字符串不允许'http(s)'调用");
        }
        else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.JOB_ERROR_STR))
        {
            return error("修改任务'" + job.getJobName() + "'失败，目标字符串存在违规");
        }
        else if (!ScheduleUtils.whiteList(job.getInvokeTarget()))
        {
            return error("修改任务'" + job.getJobName() + "'失败，目标字符串不在白名单内");
        }
        return toAjax(jobService.updateJob(job));
    }

    /**
     * 校验cron表达式是否有效
     */
    @ApiOperation("校验cron表达式是否有效")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jobId", value = "任务序号", required = false, paramType = "query", dataType = "Long"),
            @ApiImplicitParam(name = "jobName", value = "任务名称", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "jobGroup", value = "任务组名", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "invokeTarget", value = "调用目标字符串", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "cronExpression", value = "cron执行表达式", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "misfirePolicy", value = "cron计划策略 （0默认 1立即触发 2触发一次执行 3不触发）", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "concurrent", value = "是否并发执行（0允许 1禁止）", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "status", value = "任务状态（0正常 1暂停）", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "searchValue", value = "搜索值", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createBy", value = "创建者", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "createTime", value = "创建时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "updateBy", value = "更新者", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "updateTime", value = "更新时间", required = false, paramType = "query", dataType = "date-time"),
            @ApiImplicitParam(name = "remark", value = "备注", required = false, paramType = "query", dataType = "String")
    })
    @PostMapping("/checkCronExpressionIsValid")
    @ResponseBody
    public boolean checkCronExpressionIsValid(SysJob job)
    {
        return jobService.checkCronExpressionIsValid(job.getCronExpression());
    }

    /**
     * Cron表达式在线生成
     */
    @ApiOperation("Cron表达式在线生成")
    @GetMapping("/cron")
    public String cron()
    {
        return prefix + "/cron";
    }

    /**
     * 查询cron表达式近5次的执行时间
     */
    @ApiOperation("查询cron表达式近5次的执行时间")
    @GetMapping("/queryCronExpression")
    @ResponseBody
    public AjaxResult queryCronExpression(@RequestParam(value = "cronExpression", required = false) String cronExpression)
    {
        if (jobService.checkCronExpressionIsValid(cronExpression))
        {
            List<String> dateList = CronUtils.getRecentTriggerTime(cronExpression);
            return success(dateList);
        }
        else
        {
            return error("表达式无效");
        }
    }
}
