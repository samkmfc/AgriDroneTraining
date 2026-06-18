package com.drone.training.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.drone.training.common.BusinessException;
import com.drone.training.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传 Controller (本地存储)。
 * 注意: GET /files/** 已由 WebMvcConfig 静态资源映射, 此处仅提供上传接口。
 *
 * @author 罗健 202308852
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    @Value("${drone.upload.path}")
    private String uploadPath;

    @Value("${drone.upload.url-prefix}")
    private String urlPrefix;

    /**
     * 上传文件。保存为 uuid + 原扩展名, 返回访问 url 与原始文件名。
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        String extension = StrUtil.isNotBlank(originalName) ? FileUtil.extName(originalName) : "";
        String fileName = IdUtil.fastSimpleUUID() + (StrUtil.isBlank(extension) ? "" : "." + extension);

        File dir = new File(uploadPath);
        if (!dir.exists()) {
            FileUtil.mkdir(dir);
        }
        File dest = new File(dir, fileName);
        try {
            file.transferTo(dest.getAbsoluteFile());
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException("文件上传失败");
        }

        Map<String, String> result = new HashMap<>();
        result.put("url", urlPrefix + fileName);
        result.put("name", originalName);
        log.info("用户上传文件成功: {} -> {}", originalName, fileName);
        return Result.success(result);
    }
}
