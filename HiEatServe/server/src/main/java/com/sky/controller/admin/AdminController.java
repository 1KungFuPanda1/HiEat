package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.AdminDTO;
import com.sky.entity.Admin;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.AdminService;
import com.sky.utils.JwtUtil;
import com.sky.vo.AdminLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员相关接口
 */
@RestController
@RequestMapping("/admin")
@Api(tags = "管理员相关接口")
@Slf4j
public class AdminController {

  @Autowired
  private AdminService adminService;

  @Autowired
  private JwtProperties jwtProperties;

  /**
   * 管理员登录
   * 
   * @param adminDTO
   * @return
   */
  @PostMapping("/login")
  @ApiOperation("管理员登录")
  public Result<AdminLoginVO> login(@RequestBody AdminDTO adminDTO) {
    log.info("管理员登录：{}", adminDTO);

    Admin admin = adminService.login(adminDTO);

    // 登录成功后，生成jwt令牌
    Map<String, Object> claims = new HashMap<>();
    claims.put(JwtClaimsConstant.ADMIN_ID, admin.getId());
    String token = JwtUtil.createJWT(
        jwtProperties.getAdminSecretKey(),
        jwtProperties.getAdminTtl(),
        claims);

    AdminLoginVO adminLoginVO = AdminLoginVO.builder()
        .id(admin.getId())
        .username(admin.getUsername())
        .name(admin.getName())
        .token(token)
        .build();

    return Result.success(adminLoginVO);
  }

  /**
   * 退出
   * 
   * @return
   */
  @PostMapping("/logout")
  @ApiOperation("管理员退出")
  public Result<String> logout() {
    return Result.success();
  }
}