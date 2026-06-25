<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElNotification } from 'element-plus'
import { User, Lock, Message, Phone, Document, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: '',
  phone: '',
  userNo: ''
})

const validateConfirmPassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '长度需在 3–50 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码至少 8 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
}

const formRef = ref()
const loading = ref(false)

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  const success = await authStore.register({
    username: form.username,
    password: form.password,
    realName: form.realName || undefined,
    email: form.email || undefined,
    phone: form.phone || undefined,
    userNo: form.userNo || undefined
  })
  loading.value = false

  if (success) {
    ElNotification.success({ title: '账号已创建', message: '请使用新账号登录' })
    router.push('/login')
  }
}
</script>

<template>
  <div class="register-view">
    <div class="form-header">
      <h1 class="form-title">创建账号</h1>
      <p class="form-subtitle">填写以下信息，开始管理你的课程与作业</p>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      class="auth-form"
    >
      <!-- 用户名 -->
      <el-form-item prop="username">
        <template #label>
          <span class="field-label">用户名 <span class="required-mark">*</span></span>
        </template>
        <el-input
          v-model="form.username"
          placeholder="设置一个用户名"
          size="large"
          :prefix-icon="User"
          class="auth-input"
        />
      </el-form-item>

      <!-- 密码 -->
      <el-form-item prop="password">
        <template #label>
          <span class="field-label">密码 <span class="required-mark">*</span></span>
        </template>
        <el-input
          v-model="form.password"
          type="password"
          placeholder="至少 8 位字符"
          size="large"
          :prefix-icon="Lock"
          show-password
          class="auth-input"
        />
      </el-form-item>

      <!-- 确认密码 -->
      <el-form-item prop="confirmPassword">
        <template #label>
          <span class="field-label">确认密码 <span class="required-mark">*</span></span>
        </template>
        <el-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="再次输入密码"
          size="large"
          :prefix-icon="Lock"
          show-password
          class="auth-input"
        />
      </el-form-item>

      <!-- 选填信息分隔线 -->
      <div class="optional-divider">
        <span class="optional-line" />
        <span class="optional-text">选填信息</span>
        <span class="optional-line" />
      </div>

      <!-- 真实姓名 -->
      <el-form-item prop="realName">
        <template #label>
          <span class="field-label">真实姓名</span>
        </template>
        <el-input
          v-model="form.realName"
          placeholder="你的真实姓名"
          size="large"
          :prefix-icon="User"
          class="auth-input"
        />
      </el-form-item>

      <!-- 邮箱 -->
      <el-form-item prop="email">
        <template #label>
          <span class="field-label">邮箱</span>
        </template>
        <el-input
          v-model="form.email"
          placeholder="your@email.com"
          size="large"
          :prefix-icon="Message"
          class="auth-input"
        />
      </el-form-item>

      <!-- 电话 -->
      <el-form-item prop="phone">
        <template #label>
          <span class="field-label">电话</span>
        </template>
        <el-input
          v-model="form.phone"
          placeholder="手机号码"
          size="large"
          :prefix-icon="Phone"
          class="auth-input"
        />
      </el-form-item>

      <!-- 学号/工号 -->
      <el-form-item prop="userNo">
        <template #label>
          <span class="field-label">学号 / 工号</span>
        </template>
        <el-input
          v-model="form.userNo"
          placeholder="学生或教职工编号"
          size="large"
          :prefix-icon="Document"
          class="auth-input"
        />
      </el-form-item>

      <!-- 提交按钮 -->
      <el-button
        type="primary"
        size="large"
        class="submit-btn"
        :loading="loading"
        @click="handleRegister"
      >
        <span class="btn-content">
          创建账号
          <ArrowRight class="btn-icon" />
        </span>
      </el-button>
    </el-form>

    <!-- 页脚 -->
    <div class="form-footer">
      <p class="footer-text">
        已有账号？
        <router-link to="/login" class="footer-link">立即登录</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
/* 头部 */
.form-header {
  margin-bottom: var(--space-8);
  text-align: left;
}

.form-title {
  font-family: var(--font-display);
  font-size: 1.875rem;
  font-weight: 600;
  line-height: 1.2;
  letter-spacing: -0.01em;
  color: var(--color-ink);
  margin: 0 0 var(--space-2) 0;
}

.form-subtitle {
  font-family: var(--font-sans);
  font-size: 0.9375rem;
  color: var(--color-graphite);
  margin: 0;
}

/* 表单 */
.auth-form :deep(.el-form-item) {
  margin-bottom: var(--space-5);
}

.auth-form :deep(.el-form-item__label) {
  padding-bottom: var(--space-2);
  line-height: 1;
}

.field-label {
  font-family: var(--font-sans);
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-ink);
  letter-spacing: 0.02em;
}

.required-mark {
  color: var(--color-rule);
  margin-left: 2px;
}

.auth-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 1px var(--color-border) inset;
  padding: 2px 14px;
  background: var(--color-paper);
  transition: box-shadow var(--transition-fast), border-color var(--transition-fast);
}

.auth-input :deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 1px var(--color-border-focus) inset,
    0 0 0 3px rgba(184, 50, 62, 0.1);
}

.auth-input :deep(.el-input__inner) {
  height: 46px;
  font-family: var(--font-sans);
  font-size: 0.9375rem;
  color: var(--color-ink);
}

.auth-input :deep(.el-input__inner::placeholder) {
  color: var(--color-graphite);
  opacity: 0.7;
}

.auth-input :deep(.el-input__icon) {
  color: var(--color-graphite);
}

/* 选填分隔线 */
.optional-divider {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin: var(--space-6) 0 var(--space-6);
}

.optional-line {
  flex: 1;
  height: 1px;
  background: var(--color-border);
}

.optional-text {
  font-family: var(--font-sans);
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-graphite);
  white-space: nowrap;
  letter-spacing: 0.05em;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: var(--radius-md);
  font-family: var(--font-sans);
  font-size: 0.9375rem;
  font-weight: 600;
  color: white;
  background: var(--color-rule);
  border: none;
  box-shadow: var(--shadow-button);
  transition: all var(--transition-fast);
  margin-top: var(--space-2);
}

.submit-btn:hover {
  background: var(--color-rule-dark);
  box-shadow: 0 6px 18px rgba(146, 42, 51, 0.35);
  transform: translateY(-2px);
}

.submit-btn:active {
  transform: translateY(0);
}

.btn-content {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
}

.btn-icon {
  width: 16px;
  height: 16px;
}

/* 页脚 */
.form-footer {
  text-align: center;
  margin-top: var(--space-8);
}

.footer-text {
  font-family: var(--font-sans);
  font-size: 0.875rem;
  color: var(--color-graphite);
  margin: 0;
}

.footer-link {
  color: var(--color-rule);
  font-weight: 600;
  text-decoration: none;
  margin-left: var(--space-1);
  transition: color var(--transition-fast);
}

.footer-link:hover {
  color: var(--color-rule-dark);
  text-decoration: underline;
  text-underline-offset: 2px;
}
</style>
