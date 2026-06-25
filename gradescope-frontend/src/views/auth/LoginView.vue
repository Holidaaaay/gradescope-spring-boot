<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElNotification } from 'element-plus'
import { User, Lock, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  username: '',
  password: '',
  remember: false
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const formRef = ref()
const loading = ref(false)

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  const success = await authStore.login(form.username, form.password)
  loading.value = false

  if (success) {
    ElNotification.success({ title: '欢迎回来', message: '登录成功' })
    router.push('/dashboard')
  }
}
</script>

<template>
  <div class="login-view">
    <div class="form-header">
      <h1 class="form-title">欢迎回来</h1>
      <p class="form-subtitle">请输入账号密码以继续使用</p>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      class="auth-form"
      @keyup.enter="handleLogin"
    >
      <!-- 用户名 -->
      <el-form-item prop="username">
        <template #label>
          <span class="field-label">用户名</span>
        </template>
        <el-input
          v-model="form.username"
          placeholder="请输入用户名"
          size="large"
          :prefix-icon="User"
          class="auth-input"
        />
      </el-form-item>

      <!-- 密码 -->
      <el-form-item prop="password">
        <template #label>
          <span class="field-label">密码</span>
        </template>
        <el-input
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
          size="large"
          :prefix-icon="Lock"
          show-password
          class="auth-input"
        />
      </el-form-item>

      <!-- 操作行 -->
      <div class="actions-row">
        <el-checkbox v-model="form.remember" class="remember-check">记住我</el-checkbox>
        <a href="#" class="forgot-link" @click.prevent>忘记密码？</a>
      </div>

      <!-- 提交按钮 -->
      <el-button
        type="primary"
        size="large"
        class="submit-btn"
        :loading="loading"
        @click="handleLogin"
      >
        <span class="btn-content">
          登录
          <ArrowRight class="btn-icon" />
        </span>
      </el-button>
    </el-form>

    <!-- 页脚 -->
    <div class="form-footer">
      <p class="footer-text">
        还没有账号？
        <router-link to="/register" class="footer-link">立即注册</router-link>
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

/* 操作行 */
.actions-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-6);
}

.remember-check :deep(.el-checkbox__label) {
  font-family: var(--font-sans);
  font-size: 0.8125rem;
  color: var(--color-graphite);
}

.remember-check :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: var(--color-rule);
}

.remember-check :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: var(--color-rule);
  border-color: var(--color-rule);
}

.forgot-link {
  font-family: var(--font-sans);
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--color-rule);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.forgot-link:hover {
  color: var(--color-rule-dark);
  text-decoration: underline;
  text-underline-offset: 2px;
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
