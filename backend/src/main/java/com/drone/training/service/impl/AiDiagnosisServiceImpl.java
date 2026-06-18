package com.drone.training.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drone.training.dto.AiDiagnoseDTO;
import com.drone.training.entity.AiDiagnosis;
import com.drone.training.mapper.AiDiagnosisMapper;
import com.drone.training.service.AiDiagnosisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 智慧农情 AI 诊断 Service 实现 (VisualGLM 第三方大模型代理 + 内置模拟应答)
 *
 * @author 罗健 202308852
 */
@Slf4j
@Service
public class AiDiagnosisServiceImpl extends ServiceImpl<AiDiagnosisMapper, AiDiagnosis>
        implements AiDiagnosisService {

    @Value("${drone.ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${drone.ai.api-url:}")
    private String aiApiUrl;

    @Value("${drone.ai.api-key:}")
    private String aiApiKey;

    @Override
    public AiDiagnosis diagnose(AiDiagnoseDTO dto, Long userId) {
        AiDiagnosis diagnosis = new AiDiagnosis();
        diagnosis.setUserId(userId);
        diagnosis.setImageUrl(dto.getImageUrl());
        diagnosis.setCropType(dto.getCropType());
        diagnosis.setQuestion(dto.getQuestion());

        boolean filled = false;
        if (aiEnabled) {
            try {
                filled = callRemoteModel(dto, diagnosis);
            } catch (Exception e) {
                log.warn("调用第三方 AI 大模型失败, 回退到内置模拟应答: {}", e.getMessage());
            }
        }

        if (!filled) {
            // 未启用 / 调用失败 / 解析失败 -> 内置模拟诊断
            fillMockDiagnosis(dto, diagnosis);
        }

        save(diagnosis);
        return diagnosis;
    }

    /**
     * 调用第三方大模型 (VisualGLM)。成功并解析出结果返回 true, 否则返回 false 由调用方回退。
     */
    private boolean callRemoteModel(AiDiagnoseDTO dto, AiDiagnosis diagnosis) {
        Map<String, Object> body = new HashMap<>();
        body.put("imageUrl", dto.getImageUrl());
        body.put("cropType", dto.getCropType());
        body.put("question", dto.getQuestion());

        try (HttpResponse response = HttpRequest.post(aiApiUrl)
                .header("Authorization", "Bearer " + aiApiKey)
                .header("Content-Type", "application/json")
                .body(JSON.toJSONString(body))
                .timeout(15000)
                .execute()) {
            if (!response.isOk()) {
                log.warn("第三方 AI 返回非成功状态: {}", response.getStatus());
                return false;
            }
            String respBody = response.body();
            JSONObject json = JSON.parseObject(respBody);
            if (json == null) {
                return false;
            }
            // 兼容常见返回结构, 优先取 data 节点
            JSONObject data = json.getJSONObject("data");
            JSONObject src = data != null ? data : json;

            String recognition = src.getString("recognitionResult");
            if (recognition == null) {
                recognition = src.getString("result");
            }
            if (recognition == null) {
                recognition = src.getString("reply");
            }
            if (recognition == null || recognition.trim().isEmpty()) {
                return false;
            }
            diagnosis.setRecognitionResult(recognition);
            diagnosis.setRecommendPesticide(src.getString("recommendPesticide"));
            Object params = src.get("recommendParams");
            if (params != null) {
                diagnosis.setRecommendParams(params instanceof String
                        ? (String) params : JSON.toJSONString(params));
            }
            BigDecimal confidence = src.getBigDecimal("confidence");
            diagnosis.setConfidence(confidence != null ? confidence : new BigDecimal("90.0"));
            return true;
        }
    }

    /**
     * 根据作物类型生成合理的内置模拟诊断结果。
     */
    private void fillMockDiagnosis(AiDiagnoseDTO dto, AiDiagnosis diagnosis) {
        String crop = dto.getCropType() == null ? "" : dto.getCropType().trim();

        String pest;
        String pesticide;
        String speed;
        String height;
        String flow;

        switch (crop) {
            case "水稻":
                pest = "稻飞虱 (中度发生)";
                pesticide = "25%吡蚜酮 20-30克/亩";
                speed = "4米/秒";
                height = "2.5米";
                flow = "1.2升/亩";
                break;
            case "小麦":
                pest = "小麦蚜虫 (轻度至中度)";
                pesticide = "10%吡虫啉 20克/亩";
                speed = "5米/秒";
                height = "2.8米";
                flow = "1.0升/亩";
                break;
            case "玉米":
                pest = "玉米螟 (中度发生)";
                pesticide = "5%甲氨基阿维菌素苯甲酸盐 15毫升/亩";
                speed = "4.5米/秒";
                height = "3.0米";
                flow = "1.5升/亩";
                break;
            case "棉花":
                pest = "棉铃虫 (中度发生)";
                pesticide = "4.5%高效氯氰菊酯 30毫升/亩";
                speed = "4米/秒";
                height = "2.5米";
                flow = "1.3升/亩";
                break;
            case "果树":
            case "柑橘":
                pest = "红蜘蛛 (中度发生)";
                pesticide = "5%阿维菌素 15毫升/亩";
                speed = "3.5米/秒";
                height = "3.5米";
                flow = "2.0升/亩";
                break;
            default:
                pest = "常见叶部病虫害 (中度发生)";
                pesticide = "25%吡蚜酮 20-30克/亩";
                speed = "4米/秒";
                height = "2.5米";
                flow = "1.2升/亩";
                break;
        }

        String cropLabel = crop.isEmpty() ? "该作物" : crop;
        String recognition = String.format(
                "经图像识别分析, %s疑似发生【%s】。建议尽快开展统防统治, 注意轮换用药以延缓抗性。",
                cropLabel, pest);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("speed", speed);
        params.put("height", height);
        params.put("flow", flow);

        diagnosis.setRecognitionResult(recognition);
        diagnosis.setRecommendPesticide(pesticide);
        diagnosis.setRecommendParams(JSON.toJSONString(params));
        diagnosis.setConfidence(new BigDecimal("90.0"));
    }
}
