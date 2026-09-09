package com.taskflow.dto.todo;

import java.util.List;
import java.util.Map;

/**
 * 各状态数量统计（首页/列表头部展示用）。
 */
public record TodoStats(long todo, long inProgress, long done, long total) {

    public static TodoStats from(List<Map<String, Object>> rows) {
        long todo = 0, inProgress = 0, done = 0;
        for (Map<String, Object> row : rows) {
            String status = null;
            Number count = null;
            for (Map.Entry<String, Object> e : row.entrySet()) {
                String key = e.getKey() == null ? "" : e.getKey().toLowerCase();
                if ("status".equals(key)) {
                    status = e.getValue() == null ? null : e.getValue().toString();
                } else if ("cnt".equals(key) && e.getValue() instanceof Number n) {
                    count = n;
                }
            }
            if (status == null || count == null) {
                continue;
            }
            long c = count.longValue();
            switch (status) {
                case "TODO" -> todo = c;
                case "IN_PROGRESS" -> inProgress = c;
                case "DONE" -> done = c;
                default -> { }
            }
        }
        return new TodoStats(todo, inProgress, done, todo + inProgress + done);
    }
}
