package com.chp.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.Article;
import com.chp.admin.mapper.ArticleMapper;
import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleMapper mapper;

    /** 管理端 — 文章列表（分页） */
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size,
                          @RequestParam(required = false) String category) {
        LambdaQueryWrapper<Article> qw = new LambdaQueryWrapper<Article>()
                .eq(category != null, Article::getCategory, category)
                .orderByDesc(Article::getCreatedAt);
        return Result.success(mapper.selectPage(new Page<>(page, size), qw));
    }

    /** 管理端 — 创建文章 */
    @PostMapping
    public Result<?> create(@RequestBody Article article) {
        mapper.insert(article);
        return Result.success(article);
    }

    /** 管理端 — 更新文章 */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Article article) {
        article.setId(id);
        mapper.updateById(article);
        return Result.success();
    }

    /** 管理端 — 发布/下架 */
    @PutMapping("/{id}/publish")
    public Result<?> togglePublish(@PathVariable Long id,
                                   @RequestParam Integer publish) {
        Article a = mapper.selectById(id);
        if (a == null) return Result.error(400, "文章不存在");
        a.setIsPublished(publish);
        if (publish == 1) a.setPublishedAt(LocalDateTime.now());
        mapper.updateById(a);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        mapper.deleteById(id);
        return Result.success();
    }
}
