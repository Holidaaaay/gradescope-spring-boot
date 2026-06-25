<template>
  <div class="auth-layout">
    <!-- Left Panel: Brand & Illustration -->
    <aside class="brand-panel">
      <div class="brand-content">
        <div class="brand-logo">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="40" height="40" rx="10" fill="white" fill-opacity="0.12" />
            <path d="M12 14H28M12 20H24M12 26H20" stroke="white" stroke-width="2.5" stroke-linecap="round" />
          </svg>
          <span class="logo-text">Gradescope</span>
        </div>

        <h1 class="brand-headline">
          Streamline your
          <br />
          <span class="brand-highlight">teaching workflow</span>
        </h1>

        <p class="brand-desc">
          Assign, submit, grade, and collaborate — all in one modern platform built for educators and students.
        </p>

        <ul class="feature-list stagger-children">
          <li class="feature-item">
            <span class="feature-icon">✓</span>
            <span class="feature-text">Intelligent assignment management</span>
          </li>
          <li class="feature-item">
            <span class="feature-icon">✓</span>
            <span class="feature-text">Multi-version submission tracking</span>
          </li>
          <li class="feature-item">
            <span class="feature-icon">✓</span>
            <span class="feature-text">Transparent rubric-based grading</span>
          </li>
          <li class="feature-item">
            <span class="feature-icon">✓</span>
            <span class="feature-text">Role-based access control</span>
          </li>
        </ul>

        <!-- Decorative circles -->
        <div class="decor-circle decor-circle-1" />
        <div class="decor-circle decor-circle-2" />
        <div class="decor-circle decor-circle-3" />
      </div>
    </aside>

    <!-- Right Panel: Form -->
    <main class="form-panel">
      <div class="form-wrapper animate-fade-in-scale">
        <router-view />
      </div>
    </main>
  </div>
</template>

<style scoped>
.auth-layout {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg);
}

/* ── Left Brand Panel ─────────────────────────────── */
.brand-panel {
  flex: 0 0 44%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: var(--space-16);
  background: linear-gradient(160deg, #3730a3 0%, #4f46e5 50%, #6366f1 100%);
  color: white;
  position: relative;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 1;
  max-width: 440px;
  margin: 0 auto;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-10);
}

.logo-text {
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: white;
}

.brand-headline {
  font-size: 2.75rem;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -0.03em;
  color: white;
  margin-bottom: var(--space-5);
}

.brand-highlight {
  background: linear-gradient(90deg, #c7d2fe, #a5b4fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-desc {
  font-size: 1.0625rem;
  line-height: 1.65;
  color: rgba(255, 255, 255, 0.75);
  margin-bottom: var(--space-10);
  max-width: 360px;
}

.feature-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-size: 0.9375rem;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
}

.feature-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.15);
  font-size: 0.75rem;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

/* Decorative circles */
.decor-circle {
  position: absolute;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.04);
  pointer-events: none;
}

.decor-circle-1 {
  width: 400px;
  height: 400px;
  top: -120px;
  right: -100px;
}

.decor-circle-2 {
  width: 280px;
  height: 280px;
  bottom: 80px;
  right: -60px;
  background: rgba(255, 255, 255, 0.03);
}

.decor-circle-3 {
  width: 160px;
  height: 160px;
  bottom: -40px;
  left: 40px;
  background: rgba(255, 255, 255, 0.05);
}

/* ── Right Form Panel ─────────────────────────────── */
.form-panel {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8);
  background: var(--color-bg-soft);
}

.form-wrapper {
  width: 100%;
  max-width: 420px;
}

/* ── Responsive ───────────────────────────────────── */
@media (max-width: 960px) {
  .brand-panel {
    display: none;
  }

  .form-panel {
    padding: var(--space-6);
  }
}

@media (max-width: 480px) {
  .form-panel {
    padding: var(--space-4);
    align-items: flex-start;
    padding-top: var(--space-10);
  }

  .form-wrapper {
    max-width: 100%;
  }
}
</style>
