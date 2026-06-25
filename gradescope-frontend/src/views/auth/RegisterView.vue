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
    callback(new Error('Passwords do not match'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: 'Please enter a username', trigger: 'blur' },
    { min: 3, max: 50, message: 'Must be 3–50 characters', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter a password', trigger: 'blur' },
    { min: 8, message: 'Must be at least 8 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm your password', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: 'Please enter a valid email', trigger: 'blur' }
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
    ElNotification.success({ title: 'Account created', message: 'Please sign in with your new account' })
    router.push('/login')
  }
}
</script>

<template>
  <div class="register-view">
    <div class="form-header">
      <h1 class="form-title">Create your account</h1>
      <p class="form-subtitle">Fill in the details below to get started</p>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      class="auth-form"
    >
      <!-- Username -->
      <el-form-item prop="username">
        <template #label>
          <span class="field-label">Username <span class="required-mark">*</span></span>
        </template>
        <el-input
          v-model="form.username"
          placeholder="Choose a username"
          size="large"
          :prefix-icon="User"
          class="auth-input"
        />
      </el-form-item>

      <!-- Password -->
      <el-form-item prop="password">
        <template #label>
          <span class="field-label">Password <span class="required-mark">*</span></span>
        </template>
        <el-input
          v-model="form.password"
          type="password"
          placeholder="At least 8 characters"
          size="large"
          :prefix-icon="Lock"
          show-password
          class="auth-input"
        />
      </el-form-item>

      <!-- Confirm Password -->
      <el-form-item prop="confirmPassword">
        <template #label>
          <span class="field-label">Confirm Password <span class="required-mark">*</span></span>
        </template>
        <el-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="Re-enter your password"
          size="large"
          :prefix-icon="Lock"
          show-password
          class="auth-input"
        />
      </el-form-item>

      <!-- Optional Fields Divider -->
      <div class="optional-divider">
        <span class="optional-line" />
        <span class="optional-text">Optional Information</span>
        <span class="optional-line" />
      </div>

      <!-- Real Name -->
      <el-form-item prop="realName">
        <template #label>
          <span class="field-label">Real Name</span>
        </template>
        <el-input
          v-model="form.realName"
          placeholder="Your full name"
          size="large"
          :prefix-icon="User"
          class="auth-input"
        />
      </el-form-item>

      <!-- Email -->
      <el-form-item prop="email">
        <template #label>
          <span class="field-label">Email</span>
        </template>
        <el-input
          v-model="form.email"
          placeholder="your@email.com"
          size="large"
          :prefix-icon="Message"
          class="auth-input"
        />
      </el-form-item>

      <!-- Phone -->
      <el-form-item prop="phone">
        <template #label>
          <span class="field-label">Phone</span>
        </template>
        <el-input
          v-model="form.phone"
          placeholder="Phone number"
          size="large"
          :prefix-icon="Phone"
          class="auth-input"
        />
      </el-form-item>

      <!-- User No -->
      <el-form-item prop="userNo">
        <template #label>
          <span class="field-label">User No</span>
        </template>
        <el-input
          v-model="form.userNo"
          placeholder="Student / Staff ID"
          size="large"
          :prefix-icon="Document"
          class="auth-input"
        />
      </el-form-item>

      <!-- Submit -->
      <el-button
        type="primary"
        size="large"
        class="submit-btn"
        :loading="loading"
        @click="handleRegister"
      >
        <span class="btn-content">
          Create Account
          <ArrowRight class="btn-icon" />
        </span>
      </el-button>
    </el-form>

    <!-- Footer -->
    <div class="form-footer">
      <p class="footer-text">
        Already have an account?
        <router-link to="/login" class="footer-link">Sign in</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.register-view {
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

.required-mark {
  color: var(--color-primary-500);
  margin-left: 2px;
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

/* Optional Divider */
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
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  white-space: nowrap;
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
  margin-top: var(--space-2);
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

/* Footer */
.form-footer {
  text-align: center;
  margin-top: var(--space-6);
  padding-top: var(--space-6);
  border-top: 1px solid var(--color-border);
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
