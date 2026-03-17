<template>
  <div class="login-page">
    <div class="login-container">
      
      <!-- 桌面端/移动端顶部的品牌展示区 -->
      <div class="login-brand">
        <div class="brand-content">
          <div class="brand-logo">
            <HeartPulse :size="48" color="#fff" :stroke-width="1.5" />
          </div>
          <h1 class="brand-title">社区卫生服务中心</h1>
          <p class="brand-sub">Community Health Service</p>
          
          <div class="brand-features">
            <div class="feature-item">预约挂号 · 移动排队</div>
            <div class="feature-item">健康档案 · 全程管理</div>
            <div class="feature-item">慢病随访 · 公共卫生</div>
          </div>
        </div>
      </div>

      <!-- 表单区 -->
      <div class="login-form-area">
        <div class="form-wrapper">
          <div class="form-header">
            <h2>欢迎登录</h2>
            <p>请使用手机号或工号安全登录</p>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            class="custom-form"
          >
            <el-form-item prop="account">
              <div class="custom-input-group">
                <span class="input-icon">
                  <UserRound :size="20" />
                </span>
                <input 
                  type="text" 
                  v-model="form.account" 
                  placeholder="手机号 / 工号"
                  class="custom-input"
                  data-testid="input-account"
                  @keyup.enter="focusPassword"
                />
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <div class="custom-input-group">
                <span class="input-icon">
                  <Lock :size="20" />
                </span>
                <input 
                  type="password" 
                  v-model="form.password" 
                  placeholder="登录密码"
                  class="custom-input"
                  data-testid="input-password"
                  ref="passwordInputRef"
                  @keyup.enter="handleLogin"
                />
              </div>
            </el-form-item>

            <button 
              type="button"
              class="custom-submit-btn" 
              :class="{ 'is-loading': loading }"
              data-testid="btn-login"
              @click="handleLogin"
            >
              <span v-if="loading" class="spinner"></span>
              <span v-else>登 录</span>
            </button>
          </el-form>

          <details class="help-section">
            <summary>演示账号 · 帮助</summary>
            <div class="help-content">
              <p>居民端：手机号 13800000001 / 密码 123456</p>
              <p>医护端：工号 DR001 / 密码 123456</p>
              <p>管理端：工号 AD001 / 密码 123456</p>
            </div>
          </details>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { HeartPulse, UserRound, Lock } from 'lucide-vue-next'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref()
const passwordInputRef = ref(null)

const form = reactive({ account: '', password: '' })

// 后台静默校验：11位数字即为居民
const isPhone = computed(() => /^1[3-9]\d{9}$/.test(form.account))

const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function focusPassword() {
  passwordInputRef.value?.focus()
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    let res
    if (isPhone.value) {
      // 静默路由至居民端验证 (防爆破: 前端无UI提示)
      res = await request.post('/auth/resident/login', {
        phone: form.account,
        password: form.password
      })
    } else {
      // 否则为医护/管理验证
      res = await request.post('/auth/admin/login', {
        username: form.account,
        password: form.password
      })
    }
    
    userStore.setLogin(res.data)
    ElMessage.success('登录成功')
    
    const role = res.data?.role
    const domain = res.data?.domain
    
    // 多域跳转
    if (domain === 'resident') {
      router.push('/resident/home')
    } else if (role === 'admin') {
      router.push('/admin/dashboard')
    } else {
      router.push('/medical/workbench')
    }
  } catch (e) {
    // Axios response interceptor handles the error notification
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ────────────────────────────────────────────────────────
   全局页面背景
──────────────────────────────────────────────────────── */
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f4f2;
  background-image: 
    radial-gradient(at 0% 0%, hsla(158,40%,90%,1) 0, transparent 50%), 
    radial-gradient(at 100% 100%, hsla(158,30%,85%,1) 0, transparent 50%);
  padding: 24px;
  font-family: var(--font-sans);
}

/* ────────────────────────────────────────────────────────
   绝对对称、居中的玻璃视效容器 (Desktop: 960x540)
──────────────────────────────────────────────────────── */
.login-container {
  width: 100%;
  max-width: 960px;
  height: 560px;
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 12px 40px -8px rgba(31, 86, 68, 0.10),
              0 0 1px 0 rgba(0,0,0,0.08);
  display: flex;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* ────────────────────────────────────────────────────────
   左半区：品牌视觉 (严格 50% 宽，高度一致)
──────────────────────────────────────────────────────── */
.login-brand {
  width: 50%;
  height: 100%;
  background: linear-gradient(160deg, #3a7d65 0%, #2b5e4a 100%);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #fff;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.brand-logo {
  width: 64px;
  height: 64px;
  margin-bottom: 24px;
}

.brand-logo svg {
  width: 100%;
  height: 100%;
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 2px;
  margin-bottom: 8px;
  /* text-shadow removed for cleaner look */
}

.brand-sub {
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 3px;
  color: rgba(255,255,255,0.7);
  text-transform: uppercase;
  margin-bottom: 48px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  font-size: 15px;
  color: rgba(255,255,255,0.9);
  letter-spacing: 0.5px;
  position: relative;
  padding: 8px 16px;
  border-radius: 999px;
  background: rgba(255,255,255,0.08);
}


/* ────────────────────────────────────────────────────────
   右半区：留白表单 (严格 50% 宽，高度一致)
──────────────────────────────────────────────────────── */
.login-form-area {
  width: 50%;
  height: 100%;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.form-wrapper {
  width: 100%;
  max-width: 360px;
  display: flex;
  flex-direction: column;
}

.form-header {
  margin-bottom: 36px;
  text-align: left;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 800;
  color: #1a221f;
  margin-bottom: 10px;
  letter-spacing: -0.5px;
}

.form-header p {
  font-size: 14px;
  color: #6c7e77;
}

/* ────────────────────────────────────────────────────────
   高端原生表单样式
──────────────────────────────────────────────────────── */
.custom-form {
  display: flex;
  flex-direction: column;
  gap: 8px; /* Element Form 底部已带边距，调小自身 Gap */
}

/* Element Plus Form 覆写 */
.custom-form :deep(.el-form-item) {
  margin-bottom: 24px;
}
.custom-form :deep(.el-form-item__content) {
  line-height: normal;
}
.custom-form :deep(.el-form-item__error) {
  padding-top: 6px;
  font-size: 12px;
  color: var(--danger);
}

.custom-input-group {
  display: flex;
  align-items: center;
  width: 100%;
  height: 52px;
  background: #f7faf9;
  border: 1px solid #eaf0ed;
  border-radius: 14px;
  padding: 0 16px;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.custom-input-group:focus-within {
  background: #ffffff;
  border-color: var(--primary);
  box-shadow: 0 4px 16px rgba(47, 107, 87, 0.12);
  transform: translateY(-1px);
}

.input-icon {
  width: 20px;
  height: 20px;
  color: #9cb3a8;
  margin-right: 12px;
  display: flex;
  align-items: center;
  transition: color 0.3s ease;
}

.custom-input-group:focus-within .input-icon {
  color: var(--primary);
}

.custom-input {
  flex: 1;
  height: 100%;
  border: none;
  background: transparent;
  outline: none;
  font-size: 15px;
  color: #2c3a35;
  font-weight: 500;
  font-family: inherit;
}

.custom-input::placeholder {
  color: #a8bec1;
  font-weight: 400;
}

/* 自定义登录按钮 */
.custom-submit-btn {
  width: 100%;
  height: 52px;
  margin-top: 12px;
  border-radius: 14px;
  background: linear-gradient(135deg, #32755e 0%, #225140 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  border: none;
  outline: none;
  cursor: pointer;
  box-shadow: 0 8px 24px -6px rgba(47, 107, 87, 0.4);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.custom-submit-btn:hover:not(.is-loading) {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px -6px rgba(47, 107, 87, 0.5);
  background: linear-gradient(135deg, #38846a 0%, #285f4b 100%);
}

.custom-submit-btn:active:not(.is-loading) {
  transform: translateY(1px);
  box-shadow: 0 4px 12px -6px rgba(47, 107, 87, 0.4);
}

.custom-submit-btn.is-loading {
  opacity: 0.8;
  cursor: not-allowed;
}

/* Loading 旋转动画 */
.spinner {
  width: 22px;
  height: 22px;
  border: 3px solid rgba(255,255,255,0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 0.8s ease-in-out infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 折叠帮助区 */
.help-section {
  margin-top: 28px;
  text-align: center;
}
.help-section summary {
  font-size: 12px;
  color: var(--muted);
  cursor: pointer;
  user-select: none;
}
.help-section summary:hover { color: var(--primary); }
.help-content {
  margin-top: 8px;
  text-align: left;
  font-size: 12px;
  color: var(--muted);
  line-height: 1.8;
  background: var(--surface-soft);
  border-radius: 8px;
  padding: 10px 14px;
}
.help-content p { margin: 0; }

/* ────────────────────────────────────────────────────────
   移动端：与 PC 端同风格的垂直居中圆角卡片
──────────────────────────────────────────────────────── */
@media screen and (max-width: 800px) {
  .login-page {
    padding: 16px;
    align-items: center;
    justify-content: center;
    background-color: #f0f4f2;
    background-image: 
      radial-gradient(at 0% 0%, hsla(158,40%,90%,1) 0, transparent 50%), 
      radial-gradient(at 100% 100%, hsla(158,30%,85%,1) 0, transparent 50%);
  }

  /* 容器改为纵向堆叠，保留圆角和阴影 */
  .login-container {
    flex-direction: column;
    height: auto;
    width: 100%;
    max-width: 100%;
    border-radius: 24px;
    box-shadow: 0 16px 48px -8px rgba(31, 86, 68, 0.15),
                0 0 1px 0 rgba(0,0,0,0.08);
    background: #ffffff;
    overflow: hidden;
  }

  /* 品牌区变为顶部横幅 */
  .login-brand {
    width: 100%;
    height: auto;
    min-height: 180px;
    background: linear-gradient(135deg, var(--primary-hover) 0%, #174233 100%);
    border-radius: 0; /* 由父容器控制圆角 */
    padding: 32px 24px;
  }

  .brand-content {
    padding: 0;
    justify-content: center;
  }

  .brand-logo {
    width: 48px;
    height: 48px;
    margin-bottom: 16px;
  }
  
  .brand-title {
    font-size: 22px;
  }
  .brand-sub {
    font-size: 11px;
    margin-bottom: 0;
  }
  .brand-features { display: none; }



  /* 表单区：白色底、与品牌区同宽、4 面圆角由容器控制 */
  .login-form-area {
    width: 100%;
    height: auto;
    background: #ffffff;
    border-radius: 0;
    padding: 32px 24px;
    align-items: center;
  }

  .form-wrapper {
    max-width: 100%;
  }

  .form-header {
    margin-bottom: 28px;
  }
  .form-header h2 { font-size: 24px; }

  .custom-input-group {
    height: 52px;
    border-radius: 14px;
  }

  .custom-submit-btn {
    height: 52px;
    border-radius: 14px;
    margin-top: 16px;
    font-size: 16px;
  }
  
  .help-section {
    margin-top: 24px;
  }
}

/* 针对更小屏幕如 iPhone SE */
@media screen and (max-width: 380px) {
  .login-page { padding: 10px; }
  .login-brand { min-height: 150px; padding: 24px 16px; }
  .brand-title { font-size: 20px; }
  .login-form-area { padding: 24px 16px; }
}
</style>
