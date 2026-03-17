package com.chp.admin.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chp.admin.entity.Drug;
import com.chp.admin.entity.DrugStock;
import com.chp.admin.entity.Notice;
import com.chp.admin.mapper.DrugMapper;
import com.chp.admin.mapper.DrugStockMapper;
import com.chp.admin.mapper.NoticeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务：库存预警
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockAlertTask {

    private final DrugMapper drugMapper;
    private final DrugStockMapper drugStockMapper;
    private final NoticeMapper noticeMapper;

    /**
     * 每日凌晨1点检查药品库存预警
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkDrugStockAlert() {
        log.info("[定时任务] 开始检查药品库存预警");
        List<Drug> drugs = drugMapper.selectList(
                new LambdaQueryWrapper<Drug>().eq(Drug::getStatus, 1));
        int alertCount = 0;
        for (Drug drug : drugs) {
            // 统计该药品的有效库存总量
            List<DrugStock> stocks = drugStockMapper.selectList(
                    new LambdaQueryWrapper<DrugStock>()
                            .eq(DrugStock::getDrugId, drug.getId())
                            .eq(DrugStock::getStatus, 1));
            int totalQty = stocks.stream().mapToInt(DrugStock::getQuantity).sum();
            if (totalQty < drug.getAlertQty()) {
                alertCount++;
                // 写入公告
                Notice notice = new Notice();
                notice.setTitle("【库存预警】" + drug.getGenericName());
                notice.setContent(String.format("药品 %s(%s) 当前库存 %d，低于预警值 %d，请及时补货。",
                        drug.getGenericName(), drug.getDrugCode(), totalQty, drug.getAlertQty()));
                notice.setNoticeType("system");
                notice.setIsTop(0);
                notice.setStatus(1);
                notice.setPublisherId(0L); // 系统
                noticeMapper.insert(notice);
            }
        }
        log.info("[定时任务] 药品库存预警完成，发现 {} 项预警", alertCount);
    }
}
