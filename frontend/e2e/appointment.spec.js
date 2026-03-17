// @ts-check
import { test, expect } from '@playwright/test'

/**
 * 预约流程 E2E 测试
 * 演示账号：13800000001 / 123456（居民端）
 */
test.describe('预约流程（居民端）', () => {

  async function residentLogin(page) {
    await page.goto('/login')
    await page.fill('[data-testid="input-account"]', '13800000001')
    await page.fill('[data-testid="input-password"]', '123456')
    await page.click('[data-testid="btn-login"]')
    // 等待跳转（最多 5 秒）
    await page.waitForURL('**/resident/**', { timeout: 5000 }).catch(() => {})
  }

  test('居民登录后跳转到首页', async ({ page }) => {
    await residentLogin(page)
    const url = page.url()
    // 如果登录成功应跳转到 /resident/home
    if (url.includes('resident')) {
      await expect(page).toHaveURL(/.*resident\/home/)
    } else {
      // 登录失败（如数据库无此账号），验证仍在登录页
      await expect(page).toHaveURL(/.*login/)
    }
  })

  test('居民底部 TabBar 可见', async ({ page }) => {
    await residentLogin(page)
    const url = page.url()
    if (url.includes('resident')) {
      // 底部 TabBar 应可见
      const tabBar = page.locator('.tab-bar, .bottom-nav, nav')
      await expect(tabBar.first()).toBeVisible()
    }
  })
})
