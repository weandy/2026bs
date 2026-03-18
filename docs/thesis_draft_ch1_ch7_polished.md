1. **/admin/dept 被错误开放到匿名访问，存在未登录写入风险。**
   在 SecurityConfig.java 里，"/admin/dept" 被放进了 permitAll() 白名单；而 DeptController.java 正好把 POST /admin/dept 用作新增科室接口。
   这意味着当前配置下，**匿名用户理论上就可以直接调用新增科室接口**。这不是普通体验问题，而是权限配置漏洞。

2. **部分后台控制器路径写成了 /api/admin/...，和全局 context-path=/api 叠加后会形成双前缀风险。**
   application.yml 已经把服务上下文设成了 /api，但 DictController.java 和 VaccineStockController.java 又把控制器映射写成了 /api/admin/dict、/api/admin/vaccine-stock。
   按标准 Spring 路径拼接，这两个接口的真实地址会变成 /api/api/admin/...。前端在 request.js 里又统一用了 baseURL: '/api'，现在能不能工作，很大程度上依赖外层代理或偶然兼容。**这属于接口设计不一致，后面非常容易出 404 或环境差异问题。**

3. **仓库里存在硬编码数据库口令和默认 JWT 密钥，安全风险很高。**
   问题位置包括：

   - application-dev.yml
   - application.yml
   - development_guide.md
   - docker-compose.yml
   - seed_data.py

4. 系统里面不应该有任何英文名称的医生

   
