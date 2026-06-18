package com.drone.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.training.entity.AiDiagnosis;
import org.apache.ibatis.annotations.Mapper;

/**
 * 智慧农情 AI 诊断记录 Mapper
 *
 * @author 罗健 202308852
 */
@Mapper
public interface AiDiagnosisMapper extends BaseMapper<AiDiagnosis> {
}
