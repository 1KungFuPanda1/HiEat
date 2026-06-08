package com.sky.service;

import com.sky.dto.AdminDTO;
import com.sky.entity.Admin;

public interface AdminService {

  /**
   * 管理员登录
   * 
   * @param adminDTO
   * @return
   */
  Admin login(AdminDTO adminDTO);
}