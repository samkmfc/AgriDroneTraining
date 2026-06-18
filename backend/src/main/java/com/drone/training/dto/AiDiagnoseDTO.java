package com.drone.training.dto;

import lombok.Data;

/**
 * AI 诊断请求 DTO
 *
 * @author 罗健 202308852
 */
@Data
public class AiDiagnoseDTO {

    private String imageUrl;

    private String cropType;

    private String question;
}
