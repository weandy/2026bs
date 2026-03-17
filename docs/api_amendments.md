# API 设计修正与补充

> 基文档：根目录 `api_design.md`（75 个接口）| 本文档记录修正和补充，以本文档为准

---

## 一、全局修正

### 1.1 HTTP 状态码（已修正）
HTTP 状态码与 code 保持一致。401/403/404 使用对应 HTTP 码，业务错误(10001-10006)使用 HTTP 200。

### 1.2 密码复杂度
新密码 6-20 位，必须同时包含字母和数字。初始密码（手机号后6位）不受限。

## 二、修正的接口

### 2.1 修改手机号增加密码验证
`PUT /resident/profile/phone` 请求体新增 `password` 字段（当前登录密码）。

### 2.2 候诊公屏脱敏统一
`GET /public/screen/{deptCode}` 姓名统一为 `"022张*明"` 格式（序号+脱敏名）。
- 两字：`张*`；三字+：`张*明`

### 2.3 接种预约列表增加分页
`GET /medical/vaccine/appointments` 新增 `page`/`pageSize` 参数。

### 2.4 随访趋势数据增加上限
`GET /medical/follow-up/trend/{planId}` 新增 `limit` 参数（默认 50，最大 200）。

## 三、新增接口

### 3.1 药品消耗统计查询
```
GET /admin/report/drug
[管理员]
参数：startDate, endDate, groupBy(day/month), drugId(可选)
响应：totalInbound, totalOutbound, topConsumed[], trend[]
```

## 四、变更汇总

修正后接口总数：**76 个**。
