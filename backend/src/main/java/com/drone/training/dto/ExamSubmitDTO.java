package com.drone.training.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试提交请求
 *
 * @author 罗健 202308852
 */
@Data
public class ExamSubmitDTO implements Serializable {

    private List<AnswerItem> answers;

    /** 用时(秒) */
    private Integer duration;

    private LocalDateTime startTime;

    @Data
    public static class AnswerItem implements Serializable {
        private Long questionId;
        private String answer;
    }
}
