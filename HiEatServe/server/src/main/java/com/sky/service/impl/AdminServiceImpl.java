package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.AdminDTO;
import com.sky.entity.Admin;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.AdminMapper;
import com.sky.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class AdminServiceImpl implements AdminService {

  @Autowired
  private AdminMapper adminMapper;

  /**
   * 管理员登录
   * 
   * @param adminDTO
   * @return
   */
  public Admin login(AdminDTO adminDTO) {
    String username = adminDTO.getUsername();
    String password = adminDTO.getPassword();

    // 1、根据用户名查询管理员
    Admin admin = adminMapper.getByUsername(username);

    // 2、处理各种异常情况（用户名不存在、密码错误、账号被锁定）
    if (admin == null) {
      throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
    }

    // 密码比对
    password = DigestUtils.md5DigestAsHex(password.getBytes());
    if (!password.equals(admin.getPassword())) {
      throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
    }

    // 判断账号是否被锁定
    if (admin.getStatus() == StatusConstant.DISABLE) {
      throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
    }

    // 3、返回实体对象
    return admin;
  }
}