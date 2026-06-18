package com.drone.training.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.training.common.Result;
import com.drone.training.dto.AiDiagnoseDTO;
import com.drone.training.entity.AiDiagnosis;
import com.drone.training.security.SecurityUtils;
import com.drone.training.service.AiDiagnosisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 智慧农情 AI 助手 Controller (VisualGLM 第三方大模型代理)
 *
 * @author 罗健 202308852
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AiDiagnosisController {

    private final AiDiagnosisService aiDiagnosisService;

    /**
     * AI 病虫害诊断。启用第三方大模型时代理调用, 未启用或出错时回退到内置模拟应答, 永不 500。
     */
    @PostMapping("/diagnose")
    public Result<AiDiagnosis> diagnose(@RequestBody AiDiagnoseDTO dto) {
        Long userId = SecurityUtils.getUserId();
        AiDiagnosis diagnosis = aiDiagnosisService.diagnose(dto, userId);
        return Result.success(diagnosis);
    }

    /**
     * 当前用户的 AI 诊断历史 (分页, 最新优先)。
     */
    @GetMapping("/history/my")
    public Result<Page<AiDiagnosis>> myHistory(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtils.getUserId();
        Page<AiDiagnosis> page = aiDiagnosisService.page(
                new Page<>(pageNum, pageSize),
                Wrappers.<AiDiagnosis>lambdaQuery()
                        .eq(AiDiagnosis::getUserId, userId)
                        .orderByDesc(AiDiagnosis::getId));
        return Result.success(page);
    }
}
