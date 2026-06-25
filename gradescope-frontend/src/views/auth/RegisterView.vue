<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElNotification } from 'element-plus'

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
    { required: true, message: 'Please enter username', trigger: 'blur' },
    { min: 3, max: 50, message: 'Length must be 3 to 50', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter password', trigger: 'blur' },
    { min: 8, message: 'At least 8 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm password', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: 'Invalid email format', trigger: 'blur' }
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
    ElNotification.success({ title: 'Success', message: 'Registration successful, please sign in' })
    router.push('/login')
  }
}
</script>

<template>
  <div>
    <h2 style="text-align: center; margin-bottom: 24px; color: #1f2937;">Sign Up</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="Username *" prop="username">
        <el-input v-model="form.username" placeholder="3-50 characters" size="large" />
      </el-form-item>

      <el-form-item label="Password *" prop="password">
        <el-input v-model="form.password" type="password" placeholder="At least 8 characters" size="large" show-password />
      </el-form-item>

      <el-form-item label="Confirm Password *" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" placeholder="Re-enter password" size="large" show-password />
      </el-form-item>

      <el-form-item label="Real Name" prop="realName">
        <el-input v-model="form.realName" placeholder="Optional" size="large" />
      </el-form-item>

      <el-form-item label="Email" prop="email">
        <el-input v-model="form.email" placeholder="Optional" size="large" />
      </el-form-item>

      <el-form-item label="Phone" prop="phone">
        <el-input v-model="form.phone" placeholder="Optional" size="large" />
      </el-form-item>

      <el-form-item label="User No" prop="userNo">
        <el-input v-model="form.userNo" placeholder="Student/Staff ID (optional)" size="large" />
      </el-form-item>

      <el-button
        type="primary"
        size="large"
        style="width: 100%; margin-top: 8px;"
        :loading="loading"
        @click="handleRegister"
      >
        Sign Up
      </el-button>
    </el-form>

    <div style="text-align: center; margin-top: 16px;">
      <span style="color: #6b7280;">Already have an account? </span>
      <router-link to="/login" style="color: #6366f1; text-decoration: none; font-weight: 500;">
        Sign In
      </router-link>
    </div>
  </div>
</template>
