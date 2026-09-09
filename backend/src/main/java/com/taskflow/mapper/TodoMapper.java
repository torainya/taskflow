package com.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskflow.domain.Todo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface TodoMapper extends BaseMapper<Todo> {

    /**
     * 按状态聚合统计（自定义 SQL 示例：注解式 SQL，无需 XML）
     * 返回形如 [{status: TODO, cnt: 3}, ...]
     */
    @Select("select status, count(*) as cnt from tf_todo where user_id = #{userId} group by status")
    List<Map<String, Object>> countByStatus(@Param("userId") Long userId);
}
