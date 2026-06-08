package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.NoticeDTO;
import com.sky.entity.Notice;
import com.sky.mapper.NoticeMapper;
import com.sky.result.PageResult;
import com.sky.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知服务实现
 */
@Service
@Slf4j
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    /**
     * 新增通知
     * @param noticeDTO
     */
    @Override
    public void save(NoticeDTO noticeDTO) {
        Notice notice = new Notice();
        BeanUtils.copyProperties(noticeDTO, notice);
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        notice.setCreateTime(now);
        notice.setUpdateTime(now);
        
        // 如果设置了持续时间，则计算失效时间
        if (noticeDTO.getDuration() != null && noticeDTO.getDuration() > 0) {
            LocalDateTime expireTime = noticeDTO.getEffectiveTime().plusMinutes(noticeDTO.getDuration());
            notice.setExpireTime(expireTime);
        } else {
            // 如果设置了失效时间，则计算持续时间
            LocalDateTime effectiveTime = noticeDTO.getEffectiveTime();
            notice.setDuration((int) Duration.between(effectiveTime, noticeDTO.getExpireTime()).toMinutes());
        }
        
        noticeMapper.insert(notice);
    }

    /**
     * 删除通知
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        noticeMapper.deleteById(id);
    }

    /**
     * 更新通知
     * @param noticeDTO
     */
    @Override
    public void update(NoticeDTO noticeDTO) {
        Notice notice = new Notice();
        BeanUtils.copyProperties(noticeDTO, notice);
        
        // 设置更新时间
        notice.setUpdateTime(LocalDateTime.now());
        
        // 如果设置了持续时间，则计算失效时间
        if (noticeDTO.getDuration() != null && noticeDTO.getDuration() > 0 && noticeDTO.getEffectiveTime() != null) {
            LocalDateTime expireTime = noticeDTO.getEffectiveTime().plusMinutes(noticeDTO.getDuration());
            notice.setExpireTime(expireTime);
        }
        
        noticeMapper.update(notice);
    }

    /**
     * 根据ID查询通知
     * @param id
     * @return
     */
    @Override
    public Notice getById(Long id) {
        return noticeMapper.getById(id);
    }

    /**
     * 分页查询通知
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult pageQuery(int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        Page<Notice> notices = noticeMapper.pageQuery();
        return new PageResult(notices.getTotal(), notices.getResult());
    }

    /**
     * 获取当前有效的通知
     * @return
     */
    @Override
    public List<Notice> getActiveNotices() {
        LocalDateTime now = LocalDateTime.now();
        return noticeMapper.getActiveNotices(now);
    }
} 