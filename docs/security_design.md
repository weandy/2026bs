# 安全设计文档

---

## 一、JWT 认证

| 配置 | 值 |
|------|-----|
| Access Token | 2h |
| Refresh Token | 7d |
| 签名算法 | HMAC-SHA256 |
| 密钥管理 | `application.yml` 的 `jwt.secret`，生产环境通过环境变量 `JWT_SECRET` 注入 |

**Token Payload**：`sub`(用户ID), `name`, `role`, `deptCode`, `domain`(resident/admin)

**关键**：每次请求都查用户 `status` 字段（Caffeine 缓存 5min），管理员禁用账号后立即生效。

## 二、RBAC 权限

三级模型：角色 → 模块 → 操作(view/create/edit/delete/export)

```java
@RequirePermission(module = "prescription", op = "create")
```

### 权限矩阵

| 模块 | 居民 | 医生 | 护士 | 管理员 |
|------|------|------|------|--------|
| 预约挂号 | VCDE | V | V | V |
| 接诊工作台 | - | VCE | V | V |
| 电子处方 | - | VC | V | V |
| 药房发药 | - | - | VCE | V |
| 慢病随访 | - | VCE | VCE | V |
| 用户权限 | - | - | - | VCEDX |
| 药品库存 | - | - | - | VCEX |

## 三、密码策略

- 6-20 位，包含字母+数字
- BCrypt 加密
- 初始密码：手机号后 6 位
- 登录失败 5 次锁定 30 分钟（Caffeine 实现）

```java
Cache<String, Integer> loginFailCache = Caffeine.newBuilder()
    .expireAfterWrite(30, TimeUnit.MINUTES).maximumSize(10000).build();
```

## 四、数据脱敏

| 字段 | 规则 | 场景 |
|------|------|------|
| 手机号 | `138****8000` | 所有对外接口 |
| 身份证 | `110101********5011` | 医护端只读 |
| 姓名 | `张*明` | 候诊公屏 |

实现位置：VO 层 `MaskUtils` 工具类。

## 五、防越权

| 场景 | 方式 |
|------|------|
| 居民查他人档案 | `resident_id = JWT.residentId` |
| 居民取消他人预约 | SQL 加 `AND resident_id = ?` |
| 居民接口不接受 residentId 参数 | Controller 从 SecurityContext 取 |
| 医生查他人处方 | 校验 visit.staff_id |

## 六、审计日志触发清单

`LOGIN` / `LOGOUT` / `RECORD_VIEW` / `RECORD_CREATE` / `RECORD_EDIT` /
`HEALTH_EXPORT` / `PRESC_CREATE` / `PRESC_REVOKE` / `DRUG_DISPENSE` /
`DRUG_INBOUND` / `DRUG_ADJUST` / `VACCINE_RECORD` / `USER_CREATE` /
`USER_STATUS` / `USER_RESET_PWD` / `PERM_CHANGE` / `SCHEDULE_STOP`

audit_log 表**只追加**，任何角色不可删改。
