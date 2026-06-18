package com.drone.training.dto;

import lombok.Data;

/**
 * 飞行作业日志审核 DTO
 *
 * @author 罗健 202308852
 */
@Data
public class FlightLogAuditDTO {

    /** APPROVED / REJECTED */
    private String auditStatus;

    private String auditReason;
}
