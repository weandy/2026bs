package com.chp.resident.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.Article;
import com.chp.admin.mapper.ArticleMapper;
import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/resident/article")
@RequiredArgsConstructor
public class ArticleViewController {

    private final ArticleMapper mapper;

    /** 居民端 — 已发布文章列表 */
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size,
                          @RequestParam(required = false) String category) {
        LambdaQueryWrapper<Article> qw = new LambdaQueryWrapper<Article>()
                .eq(Article::getIsPublished, 1)
                .eq(category != null, Article::getCategory, category)
                .orderByDesc(Article::getPublishedAt);
        return Result.success(mapper.selectPage(new Page<>(page, size), qw));
    }

    /** 居民端 — 文章详情 */
    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        Article a = mapper.selectById(id);
        if (a == null || a.getIsPublished() != 1)
            return Result.error(400, "文章不存在");
        // 阅读量+1
        a.setViewCount(a.getViewCount() + 1);
        mapper.updateById(a);
        return Result.success(a);
    }
}
