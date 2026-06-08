package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.entity.Notice;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知Mapper
 */
@Mapper
public interface NoticeMapper {

    /**
     * 插入通知
     * @param notice
     */
    @Insert("insert into notice (content, effective_time, expire_time, duration, create_time, update_time) " +
            "values (#{content}, #{effectiveTime}, #{expireTime}, #{duration}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Notice notice);

    /**
     * 根据ID删除通知
     * @param id
     */
    @Delete("delete from notice where id = #{id}")
    void deleteById(Long id);

    /**
     * 更新通知
     * @param notice
     */
    void update(Notice notice);

    /**
     * 根据ID查询通知
     * @param id
     * @return
     */
    @Select("select * from notice where id = #{id}")
    Notice getById(Long id);

    /**
     * 分页查询通知
     * @return
     */
    Page<Notice> pageQuery();

    /**
     * 查询有效的通知
     * @param currentTime 当前时间
     * @return 有效的通知列表
     */
    @Select("select * from notice where effective_time <= #{currentTime} and " +
            "(expire_time is null or expire_time >= #{currentTime}) " +
            "order by effective_time desc")
    List<Notice> getActiveNotices(LocalDateTime currentTime);
} 