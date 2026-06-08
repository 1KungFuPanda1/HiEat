package com.sky.service;

import com.sky.dto.NoticeDTO;
import com.sky.entity.Notice;
import com.sky.result.PageResult;

import java.util.List;

/**
 * 通知服务
 */
public interface NoticeService {

    /**
     * 新增通知
     * @param noticeDTO
     */
    void save(NoticeDTO noticeDTO);

    /**
     * 删除通知
     * @param id
     */
    void deleteById(Long id);

    /**
     * 更新通知
     * @param noticeDTO
     */
    void update(NoticeDTO noticeDTO);

    /**
     * 根据ID查询通知
     * @param id
     * @return
     */
    Notice getById(Long id);

    /**
     * 分页查询通知
     * @param page
     * @param pageSize
     * @return
     */
    PageResult pageQuery(int page, int pageSize);

    /**
     * 获取当前有效的通知
     * @return
     */
    List<Notice> getActiveNotices();
} 