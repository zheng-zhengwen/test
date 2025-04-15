package com.wen.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wen.oj.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据库操作
 *
 * @author <a href="https://github.com/zheng-zhengwen">程序员阿文</a>
 * @from <a href="https://wen.icu">在线编程系统</a>
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




