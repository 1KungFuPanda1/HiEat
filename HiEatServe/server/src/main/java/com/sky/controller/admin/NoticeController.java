package com.sky.controller.admin;

import com.sky.dto.NoticeDTO;
import com.sky.entity.Notice;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通知管理
 */
@RestController
@RequestMapping("/admin/notice")
@Api(tags = "通知管理接口")
@Slf4j
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 新增通知
     * @param noticeDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增通知")
    public Result<String> save(@RequestBody NoticeDTO noticeDTO) {
        log.info("新增通知：{}", noticeDTO);
        noticeService.save(noticeDTO);
        return Result.success();
    }

    /**
     * 删除通知
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除通知")
    public Result<String> delete(@PathVariable Long id) {
        log.info("删除通知：{}", id);
        noticeService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改通知
     * @param noticeDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改通知")
    public Result<String> update(@RequestBody NoticeDTO noticeDTO) {
        log.info("修改通知：{}", noticeDTO);
        noticeService.update(noticeDTO);
        return Result.success();
    }

    /**
     * 根据id查询通知
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询通知")
    public Result<Notice> getById(@PathVariable Long id) {
        log.info("根据id查询通知：{}", id);
        Notice notice = noticeService.getById(id);
        return Result.success(notice);
    }

    /**
     * 通知分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("通知分页查询")
    public Result<PageResult> page(int page, int pageSize) {
        log.info("通知分页查询：page={}, pageSize={}", page, pageSize);
        PageResult pageResult = noticeService.pageQuery(page, pageSize);
        return Result.success(pageResult);
    }
} 