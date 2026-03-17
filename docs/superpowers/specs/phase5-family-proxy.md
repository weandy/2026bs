# Phase 5：创新点③ 家庭代管分级权限

## 现状

| 组件 | 状态 |
|------|------|
| `FamilyMember` 实体 | ✅ ownerId, linkedResidentId, relation, status |
| `FamilyMemberController` | ✅ CRUD at `/resident/family` |

## 缺口

1. 权限范围字段（permissionScope）：控制代管人能查看哪些模块
2. 代查看 API：以被代管人身份查询健康档案/就诊记录/指标
3. 前端代管视图：家庭成员页面增加"查看档案"按钮

## 权限范围定义

| scope 值 | 说明 | 敏感级别 |
|----------|------|---------|
| `basic` | 基本信息（姓名、年龄） | 低 |
| `appointment` | 预约记录 | 低 |
| `health_record` | 健康档案（慢病标签、过敏史） | 中 |
| `visit_record` | 就诊记录（诊断、处方） | 高 |
| `vital` | 健康指标 | 中 |

默认关系：
- 父母 → `basic,appointment,health_record,vital`
- 子女 → `basic,appointment,health_record,visit_record,vital`  
- 配偶 → `basic,appointment,health_record,visit_record,vital`

## 改动清单

### 后端
1. `FamilyMember.java` — 增加 `permissionScope` (String, CSV)
2. `FamilyMemberController.java` — 新增 `GET /resident/family/{id}/proxy-view` 代查看接口
3. DB 迁移

### 前端
4. `FamilyPage.vue` — 代管成员列表增加"查看档案"按钮
5. `FamilyProxyView.vue` — [NEW] 代查看页面，根据 permissionScope 过滤显示内容
