package com.drone.training.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drone.training.dto.AiDiagnoseDTO;
import com.drone.training.entity.AiDiagnosis;

/**
 * 智慧农情 AI 诊断 Service
 *
 * @author 罗健 202308852
 */
public interface AiDiagnosisService extends IService<AiDiagnosis> {

    /**
     * 执行 AI 诊断: 启用时调用第三方大模型, 未启用或出错时回退到内置模拟应答。
     * 该方法保证永不抛出 500, 始终返回已保存的诊断记录。
     *
     * @param dto    诊断请求
     * @param userId 当前用户 ID
     * @return 已保存的诊断记录
     */
    AiDiagnosis diagnose(AiDiagnoseDTO dto, Long userId);
}
