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
  username: [{ required: true, message: 'Please enter your username', trigger: 'blur' }],
  password: [{ required: true, message: 'Please enter your password', trigger: 'blur' }]
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
    ElNotification.success({ title: 'Welcome back!', message: 'You have signed in successfully' })
    router.push('/dashboard')
  }
}
</script>

<template>
  <div class="login-view">
    <div class="form-header">
      <h1 class="form-title">Welcome back</h1>
      <p class="form-subtitle">Enter your credentials to access your account</p>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      class="auth-form"
      @keyup.enter="handleLogin"
    >
      <!-- Username -->
      <el-form-item prop="username">
        <template #label>
          <span class="field-label">Username</span>
        </template>
        <el-input
          v-model="form.username"
          placeholder="Enter your username"
          size="large"
          :prefix-icon="User"
          class="auth-input"
        />
      </el-form-item>

      <!-- Password -->
      <el-form-item prop="password">
        <template #label>
          <span class="field-label">Password</span>
        </template>
        <el-input
          v-model="form.password"
          type="password"
          placeholder="Enter your password"
          size="large"
          :prefix-icon="Lock"
          show-password
          class="auth-input"
        />
      </el-form-item>

      <!-- Actions row -->
      <div class="actions-row">
        <el-checkbox v-model="form.remember" class="remember-check">Remember me</el-checkbox>
        <a href="#" class="forgot-link" @click.prevent>Forgot password?</a>
      </div>

      <!-- Submit -->
      <el-button
        type="primary"
        size="large"
        class="submit-btn"
        :loading="loading"
        @click="handleLogin"
      >
        <span class="btn-content">
          Sign In
          <ArrowRight class="btn-icon" />
        </span>
      </el-button>
    </el-form>

    <!-- Divider -->
    <div class="divider">
      <span class="divider-line" />
      <span class="divider-text">or</span>
      <span class="divider-line" />
    </div>

    <!-- Footer -->
    <div class="form-footer">
      <p class="footer-text">
        Don't have an account?
        <router-link to="/register" class="footer-link">Create an account</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.login-view {
  width: 100%;
}

/* Header */
.form-header {
  margin-bottom: var(--space-8);
  text-align: left;
}

.form-title {
  font-size: 1.75rem;
  font-weight: 700;
  line-height: 1.25;
  letter-spacing: -0.02em;
  color: var(--color-text);
  margin: 0 0 var(--space-2) 0;
}

.form-subtitle {
  font-size: 0.9375rem;
  color: var(--color-text-muted);
  margin: 0;
}

/* Form */
.auth-form :deep(.el-form-item) {
  margin-bottom: var(--space-5);
}

.auth-form :deep(.el-form-item__label) {
  padding-bottom: var(--space-2);
  line-height: 1;
}

.field-label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-text);
  letter-spacing: 0.01em;
}

.auth-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 1px var(--color-border) inset;
  padding: 2px 14px;
  transition: box-shadow var(--transition-fast), border-color var(--transition-fast);
}

.auth-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--color-primary-200) inset, 0 0 0 1px var(--color-primary-500) inset;
}

.auth-input :deep(.el-input__inner) {
  height: 44px;
  font-size: 0.9375rem;
  color: var(--color-text);
}

.auth-input :deep(.el-input__inner::placeholder) {
  color: var(--color-text-muted);
}

.auth-input :deep(.el-input__icon) {
  color: var(--color-text-muted);
}

/* Actions */
.actions-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-6);
}

.remember-check :deep(.el-checkbox__label) {
  font-size: 0.8125rem;
  color: var(--color-text-soft);
}

.remember-check :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: var(--color-primary-600);
}

.remember-check :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: var(--color-primary-600);
  border-color: var(--color-primary-600);
}

.forgot-link {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--color-primary-600);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.forgot-link:hover {
  color: var(--color-primary-700);
  text-decoration: underline;
  text-underline-offset: 2px;
}

/* Submit Button */
.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: var(--radius-md);
  font-size: 0.9375rem;
  font-weight: 600;
  background: linear-gradient(135deg, var(--color-primary-600), var(--color-primary-500));
  border: none;
  box-shadow: 0 4px 14px 0 rgba(79, 70, 229, 0.35);
  transition: all var(--transition-fast);
}

.submit-btn:hover {
  background: linear-gradient(135deg, var(--color-primary-700), var(--color-primary-600));
  box-shadow: 0 6px 20px 0 rgba(79, 70, 229, 0.45);
  transform: translateY(-1px);
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

/* Divider */
.divider {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin: var(--space-6) 0;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: var(--color-border);
}

.divider-text {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* Footer */
.form-footer {
  text-align: center;
}

.footer-text {
  font-size: 0.875rem;
  color: var(--color-text-soft);
  margin: 0;
}

.footer-link {
  color: var(--color-primary-600);
  font-weight: 600;
  text-decoration: none;
  margin-left: var(--space-1);
  transition: color var(--transition-fast);
}

.footer-link:hover {
  color: var(--color-primary-700);
  text-decoration: underline;
  text-underline-offset: 2px;
}
</style>
