package com.chp.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chp.admin.entity.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
