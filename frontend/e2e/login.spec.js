// @ts-check
import { test, expect } from '@playwright/test'

/**
 * 登录流程 E2E 测试
 */
test.describe('登录流程', () => {

  test('打开登录页面', async ({ page }) => {
    await page.goto('/login')
    // 应该看到品牌标题
    await expect(page.locator('.brand-title')).toHaveText('社区卫生服务')
    // 应该看到登录表单
    await expect(page.locator('[data-testid="input-account"]')).toBeVisible()
    await expect(page.locator('[data-testid="input-password"]')).toBeVisible()
    await expect(page.locator('[data-testid="btn-login"]')).toBeVisible()
  })

  test('无效工号登录 — 停留在登录页', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="input-account"]', 'INVALID_USER')
    await page.fill('[data-testid="input-password"]', 'wrongpassword')
    await page.click('[data-testid="btn-login"]')
    await page.waitForTimeout(2000)
    // 登录失败应仍在登录页
    await expect(page).toHaveURL(/.*login/)
  })

  test('无效手机号登录 — 停留在登录页', async ({ page }) => {
    await page.goto('/login')
    await page.fill('[data-testid="input-account"]', '13900000000')
    await page.fill('[data-testid="input-password"]', 'wrongpassword')
    await page.click('[data-testid="btn-login"]')
    await page.waitForTimeout(2000)
    await expect(page).toHaveURL(/.*login/)
  })
})
