package com.sky.controller.user;

import com.sky.entity.Notice;
import com.sky.result.Result;
import com.sky.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 通知(用户端)
 */
@RestController("userNoticeController")
@RequestMapping("/user/notice")
@Api(tags = "通知接口")
@Slf4j
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 获取当前有效的通知
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("获取当前有效的通知")
    public Result<List<Notice>> list() {
        log.info("获取当前有效的通知");
        List<Notice> list = noticeService.getActiveNotices();
        return Result.success(list);
    }
} 