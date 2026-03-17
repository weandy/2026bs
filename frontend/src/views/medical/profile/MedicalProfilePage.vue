<template>
  <div class="profile-page">
    <el-tabs v-model="activeTab">
      <!-- Tab 1: 我的简介 -->
      <el-tab-pane label="我的简介" name="bio">
        <el-card shadow="never" class="profile-card">
          <div class="profile-header">
            <div class="avatar-area">
              <div class="avatar-circle">{{ nameInitial }}</div>
              <div>
                <h3>{{ userStore.userInfo?.name }}</h3>
                <el-tag size="small" type="success">{{ profile.title || '医护人员' }}</el-tag>
              </div>
            </div>
          </div>
          <el-form :model="profile" label-width="90px" class="profile-form">
            <el-form-item label="职称">
              <el-select v-model="profile.title" placeholder="请选择职称" style="width:100%">
                <el-option label="主任医师" value="主任医师" />
                <el-option label="副主任医师" value="副主任医师" />
                <el-option label="主治医师" value="主治医师" />
                <el-option label="住院医师" value="住院医师" />
                <el-option label="主任护师" value="主任护师" />
                <el-option label="副主任护师" value="副主任护师" />
                <el-option label="主管护师" value="主管护师" />
                <el-option label="护师" value="护师" />
                <el-option label="护士" value="护士" />
              </el-select>
            </el-form-item>
            <el-form-item label="从医年限">
              <el-input-number v-model="profile.yearsExp" :min="0" :max="60" style="width:100%" />
            </el-form-item>
            <el-form-item label="擅长领域">
              <el-input v-model="profile.specialty" placeholder="例如：高血压、糖尿病、儿童保健" maxlength="200" show-word-limit />
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input v-model="profile.bio" type="textarea" :rows="5" placeholder="介绍自己的专业背景、诊疗特色等" maxlength="1000" show-word-limit />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingBio" @click="saveBio">保存简介</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- Tab 2: 账号安全 -->
      <el-tab-pane label="账号安全" name="account">
        <el-card shadow="never" style="max-width:500px">
          <h4 style="margin: 0 0 20px;">修改密码</h4>
          <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="90px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="changingPwd" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>

          <el-divider />

          <h4 style="margin: 0 0 20px;">修改手机号</h4>
          <el-form :model="phoneForm" label-width="90px">
            <el-form-item label="当前手机">
              <span class="current-phone">{{ userStore.userInfo?.phone || '未绑定' }}</span>
            </el-form-item>
            <el-form-item label="新手机号">
              <el-input v-model="phoneForm.newPhone" placeholder="请输入新手机号" maxlength="11" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="changingPhone" @click="changePhone">更新手机号</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const activeTab = ref('bio')
const savingBio = ref(false)
const changingPwd = ref(false)
const changingPhone = ref(false)
const pwdFormRef = ref()

const nameInitial = computed(() => (userStore.userInfo?.name || '医')[0])

const profile = reactive({ specialty: '', bio: '', title: '', yearsExp: 0 })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const phoneForm = reactive({ newPhone: '' })

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, cb) => {
        if (value !== pwdForm.newPassword) cb(new Error('两次密码不一致'))
        else cb()
      }, trigger: 'blur'
    }
  ]
}

async function loadProfile() {
  try {
    const res = await request.get('/medical/profile')
    Object.assign(profile, res.data || {})
  } catch {}
}

async function saveBio() {
  savingBio.value = true
  try {
    await request.put('/medical/profile', { ...profile })
    ElMessage.success('简介已保存')
  } catch {} finally { savingBio.value = false }
}

async function changePassword() {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return
  changingPwd.value = true
  try {
    await request.put('/medical/profile/password', { oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  } catch {} finally { changingPwd.value = false }
}

async function changePhone() {
  if (!/^1[3-9]\d{9}$/.test(phoneForm.newPhone)) { ElMessage.error('请输入合法手机号'); return }
  changingPhone.value = true
  try {
    await request.put('/medical/profile/phone', { phone: phoneForm.newPhone })
    ElMessage.success('手机号已更新')
    phoneForm.newPhone = ''
  } catch {} finally { changingPhone.value = false }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-page { padding: 20px; max-width: 720px; }
.profile-card { margin-bottom: 20px; }
.profile-header { display: flex; align-items: center; margin-bottom: 24px; }
.avatar-area { display: flex; align-items: center; gap: 16px; }
.avatar-circle {
  width: 64px; height: 64px; border-radius: 50%;
  background: var(--primary); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; font-weight: 700;
}
.profile-form { max-width: 560px; }
.current-phone { color: var(--text); font-weight: 500; }
</style>
