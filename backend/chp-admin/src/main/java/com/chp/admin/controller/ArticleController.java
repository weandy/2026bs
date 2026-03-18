package com.chp.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.Article;
import com.chp.admin.mapper.ArticleMapper;
import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/article")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {

    private final ArticleMapper mapper;
    private final JdbcTemplate jdbcTemplate;

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
        if (publish == 1) {
            a.setPublishedAt(LocalDateTime.now());
            mapper.updateById(a);
            // 发布时推送消息给目标受众
            broadcastNotice(a);
        } else {
            mapper.updateById(a);
        }
        return Result.success();
    }

    /** 向目标受众批量写入 message 通知（跨库 JDBC，异常不影响发布主流程）*/
    private void broadcastNotice(Article a) {
        try {
            String title  = a.getTitle();
            String content = a.getSummary() != null ? a.getSummary() : (a.getContent() != null ? a.getContent().substring(0, Math.min(120, a.getContent().length())) : "");
            String audience = a.getAudience() != null ? a.getAudience() : "all";
            LocalDateTime now = LocalDateTime.now();

            // 向居民写入（仅 all / resident）
            if ("all".equals(audience) || "resident".equals(audience)) {
                String sql = "INSERT INTO chp_resident.message (resident_id, type, title, content, is_read, created_at) " +
                             "SELECT id, 'NOTICE', ?, ?, 0, ? FROM chp_resident.resident WHERE is_deleted = 0";
                int cnt = jdbcTemplate.update(sql, title, content, now);
                log.info("公告 [{}] 推送居民消息 {} 条", a.getId(), cnt);
            }
        } catch (Exception e) {
            log.warn("公告消息推送失败: articleId={}", a.getId(), e);
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        mapper.deleteById(id);
        return Result.success();
    }
}
