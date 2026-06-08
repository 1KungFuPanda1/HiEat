package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {


    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}",file);
        return getStringResult(file);
    }

    @PostMapping("/upload/register")
    @ApiOperation("注册文件上传")
    public Result<String> registerUpload(MultipartFile file){
        log.info("注册文件上传：{}",file);
        return getStringResult(file);
    }

    private Result<String> getStringResult(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return Result.error("无效文件名");
            }

            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = UUID.randomUUID().toString() + extension;

            // 创建目录（自动创建父目录）
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs(); // 跨平台安全
            }
            //把前端上传的图片，从内存 / 临时缓存 原样写入到自己硬盘的指定位置。
            File destFile = new File(dir, objectName);
            file.transferTo(destFile);

            String fileUrl = baseUrl + "/files/" + objectName;
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }
}
