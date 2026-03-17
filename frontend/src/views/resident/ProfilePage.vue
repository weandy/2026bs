<template>
  <div class="profile-page">
    <div class="profile-grid">

      <!-- 左栏：个人资料 -->
      <div class="profile-col profile-col-left">
        <el-card shadow="never" class="profile-card">
          <template #header><span>个人信息</span></template>
          <el-descriptions :column="descColumn" border v-if="profile" v-loading="loading">
            <el-descriptions-item label="姓名">{{ profile.name }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ profile.phone }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ profile.gender === 1 ? '男' : '女' }}</el-descriptions-item>
            <el-descriptions-item label="出生日期">{{ profile.birthDate || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="身份证">{{ profile.idCard || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="血型">{{ profile.bloodType || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="地址" :span="descColumn">{{ profile.address || '未设置' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never" class="profile-card">
          <template #header>
            <div class="card-header-row">
              <span>紧急联系人</span>
              <el-button type="primary" size="small" @click="showContactDlg = true">{{ contactForm.name ? '编辑' : '添加' }}</el-button>
            </div>
          </template>
          <el-descriptions :column="descColumn" border v-if="contactForm.name">
            <el-descriptions-item label="姓名">{{ contactForm.name }}</el-descriptions-item>
            <el-descriptions-item label="关系">{{ contactForm.relation }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ contactForm.phone }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="未设置紧急联系人" :image-size="48" />
        </el-card>
      </div>

      <!-- 右栏：系统设置 -->
      <div class="profile-col profile-col-right">
        <el-card shadow="never" class="profile-card">
          <template #header><span>系统设置</span></template>
          <div class="settings-actions">
            <el-button type="primary" @click="showPwdDialog = true">修改密码</el-button>
            <el-button @click="showPhoneDialog = true">修改手机号</el-button>
            <el-button type="danger" @click="handleLogout">退出登录</el-button>
          </div>
        </el-card>
      </div>

    </div>

    <!-- 修改密码 -->
    <el-dialog v-model="showPwdDialog" title="修改密码" width="400px">
      <el-form :model="pwdForm" label-width="80px" ref="pwdFormRef" :rules="pwdRules">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPwdDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="changePassword">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 修改手机号 -->
    <el-dialog v-model="showPhoneDialog" title="修改手机号" width="400px">
      <el-form :model="phoneForm" label-width="90px" ref="phoneFormRef" :rules="phoneRules">
        <el-form-item label="登录密码" prop="password">
          <el-input v-model="phoneForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="新手机号" prop="newPhone">
          <el-input v-model="phoneForm.newPhone" maxlength="11" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPhoneDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="changePhone">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 紧急联系人 -->
    <el-dialog v-model="showContactDlg" title="紧急联系人" width="400px">
      <el-form label-width="80px">
        <el-form-item label="姓名"><el-input v-model="contactForm.name" /></el-form-item>
        <el-form-item label="关系">
          <el-select v-model="contactForm.relation" placeholder="选择关系">
            <el-option label="配偶" value="配偶" />
            <el-option label="父母" value="父母" />
            <el-option label="子女" value="子女" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话"><el-input v-model="contactForm.phone" maxlength="11" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showContactDlg = false">取消</el-button>
        <el-button type="primary" @click="saveContact">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request, { forceLogout } from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

const windowWidth = ref(window.innerWidth)
const onResize = () => { windowWidth.value = window.innerWidth }
const descColumn = computed(() => windowWidth.value <= 768 ? 1 : 2)
const profile = ref(null)
const loading = ref(false)
const saving = ref(false)
const showPwdDialog = ref(false)
const showPhoneDialog = ref(false)
const showContactDlg = ref(false)
const pwdFormRef = ref()
const phoneFormRef = ref()

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const phoneForm = reactive({ password: '', newPhone: '' })
const contactForm = reactive({ name: '', relation: '', phone: '' })

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度 6-20 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (r, v, cb) => v !== pwdForm.newPassword ? cb(new Error('两次密码不一致')) : cb(), trigger: 'blur' }
  ]
}

const phoneRules = {
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }],
  newPhone: [
    { required: true, message: '请输入新手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const loadProfile = async () => {
  loading.value = true
  try {
    const { data } = await request.get('/resident/profile')
    profile.value = data
  } finally { loading.value = false }
}

const changePassword = async () => {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await request.put('/resident/profile/password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    showPwdDialog.value = false
    forceLogout()
  } finally { saving.value = false }
}

const changePhone = async () => {
  const valid = await phoneFormRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await request.put('/resident/profile/phone', phoneForm)
    ElMessage.success('手机号修改成功')
    showPhoneDialog.value = false
    loadProfile()
  } finally { saving.value = false }
}

const handleLogout = () => {
  forceLogout()
}



onMounted(() => {
  window.addEventListener('resize', onResize)
  loadProfile()
  // 加载紧急联系人
  request.get('/resident/profile/emergency-contact').then(res => {
    if (res.data) Object.assign(contactForm, res.data)
  }).catch(() => {})
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
})

const saveContact = async () => {
  try {
    await request.put('/resident/profile/emergency-contact', contactForm)
    ElMessage.success('保存成功')
    showContactDlg.value = false
  } catch (e) {
    ElMessage.error('保存失败')
  }
}
</script>

<style scoped>
.profile-page {
  padding: 16px;
  max-width: 600px;
  margin: 0 auto;
}

.profile-grid {
  display: block;
}

.profile-card {
  margin-bottom: 16px;
  border-radius: 14px;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.settings-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .profile-page { padding: 12px; }

  .settings-actions {
    flex-direction: column;
  }

  .settings-actions .el-button {
    width: 100%;
    margin-left: 0 !important;
  }
}

/* ══ PC 桌面端双栏布局 (≥ 768px) ══ */
@media (min-width: 768px) {
  .profile-page {
    max-width: 1000px;
    padding: 24px 32px;
  }

  .profile-grid {
    display: grid;
    grid-template-columns: 3fr 2fr;
    gap: 20px;
    align-items: start;
  }

  .settings-actions {
    flex-direction: column;
  }

  .settings-actions .el-button {
    width: 100%;
    margin-left: 0 !important;
  }
}
</style>
