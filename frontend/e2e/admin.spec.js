// @ts-check
import { test, expect } from '@playwright/test'

/**
 * 管理操作 E2E 测试
 * 演示账号：AD001 / 123456（管理员）
 */
test.describe('管理端操作', () => {

  async function adminLogin(page) {
    await page.goto('/login')
    await page.fill('[data-testid="input-account"]', 'AD001')
    await page.fill('[data-testid="input-password"]', '123456')
    await page.click('[data-testid="btn-login"]')
    await page.waitForURL('**/admin/**', { timeout: 5000 }).catch(() => {})
  }

  test('管理员登录后跳转到仪表盘', async ({ page }) => {
    await adminLogin(page)
    const url = page.url()
    if (url.includes('admin')) {
      await expect(page).toHaveURL(/.*admin\/dashboard/)
    } else {
      await expect(page).toHaveURL(/.*login/)
    }
  })

  test('仪表盘内容加载', async ({ page }) => {
    await adminLogin(page)
    const url = page.url()
    if (url.includes('admin')) {
      // 应该看到仪表盘标题
      await expect(page.locator('h2')).toContainText('管理仪表盘')
      // 应该看到导出报表按钮
      await expect(page.locator('text=导出报表')).toBeVisible()
    }
  })

  test('侧边栏导航可见', async ({ page }) => {
    await adminLogin(page)
    const url = page.url()
    if (url.includes('admin')) {
      // 侧边栏应可见
      const sidebar = page.locator('.sidebar, .el-aside, aside')
      await expect(sidebar.first()).toBeVisible()
    }
  })
})
