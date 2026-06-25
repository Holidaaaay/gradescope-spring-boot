<template>
  <div class="auth-layout">
    <!-- 左侧品牌面板 -->
    <aside class="brand-panel">
      <div class="brand-content">
        <div class="brand-logo animate-fade-in-up">
          <svg width="36" height="36" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="40" height="40" rx="10" fill="white" fill-opacity="0.1" />
            <path d="M12 14H28M12 20H24M12 26H20" stroke="white" stroke-width="2.5" stroke-linecap="round" />
          </svg>
          <span class="logo-text">Gradescope</span>
        </div>

        <div class="brand-statement animate-fade-in-up">
          <h1 class="brand-headline">
            更清晰的评分方式。
          </h1>
          <div class="editorial-mark" aria-hidden="true" />
          <p class="brand-tagline">
            作业、提交与成绩，一站式管理。
          </p>
        </div>
      </div>
    </aside>

    <!-- 右侧表单面板 -->
    <main class="form-panel">
      <div class="form-card animate-fade-in-scale">
        <!-- 标志性批改建 -->
        <div class="margin-rule" aria-hidden="true">
          <div class="rule-line" />
          <div class="rule-tick tick-top" />
          <div class="rule-tick tick-middle" />
          <div class="rule-tick tick-bottom" />
        </div>

        <router-view />
      </div>
    </main>
  </div>
</template>

<style scoped>
.auth-layout {
  display: flex;
  min-height: 100vh;
  background: var(--color-desk);
}

/* ── 左侧品牌面板 ─────────────────────────────── */
.brand-panel {
  flex: 0 0 33%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: var(--space-12) var(--space-10);
  background: var(--color-panel);
  color: var(--color-panel-ink);
  position: relative;
  overflow: hidden;
}

.brand-panel::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 120% 20%, rgba(255, 255, 255, 0.06) 0%, transparent 40%),
    radial-gradient(circle at -20% 110%, rgba(255, 255, 255, 0.05) 0%, transparent 35%);
  pointer-events: none;
}

.brand-content {
  position: relative;
  z-index: 1;
  max-width: 360px;
  margin: 0 auto;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-12);
}

.logo-text {
  font-family: var(--font-display);
  font-size: 1.25rem;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--color-panel-ink);
}

.brand-statement {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.brand-headline {
  font-family: var(--font-display);
  font-size: 3rem;
  font-weight: 600;
  line-height: 1.05;
  letter-spacing: -0.03em;
  color: var(--color-panel-ink);
  margin: 0 0 var(--space-5) 0;
}

.editorial-mark {
  width: 40px;
  height: 3px;
  background: var(--color-rule);
  border-radius: var(--radius-full);
  margin-bottom: var(--space-5);
}

.brand-tagline {
  font-family: var(--font-sans);
  font-size: 0.9375rem;
  font-weight: 400;
  line-height: 1.5;
  color: var(--color-panel-muted);
  margin: 0;
  max-width: 280px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ── 右侧表单面板与卡片 ───────────────────────── */
.form-panel {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8);
}

.form-card {
  position: relative;
  width: 100%;
  max-width: 440px;
  background: var(--color-paper);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: var(--space-10) var(--space-8) var(--space-8) 56px;
  overflow: hidden;
}

/* ── 批改建 ──────────────────────────────────── */
.margin-rule {
  position: absolute;
  top: var(--space-10);
  bottom: var(--space-8);
  left: 28px;
  width: 2px;
  pointer-events: none;
}

.rule-line {
  position: absolute;
  inset: 0;
  width: 100%;
  background: var(--color-rule);
  border-radius: var(--radius-full);
  opacity: 0.9;
}

.rule-tick {
  position: absolute;
  left: 50%;
  width: 14px;
  height: 2px;
  background: var(--color-rule);
  border-radius: var(--radius-full);
  transform: translateX(-50%);
  transition: width 0.2s ease, transform 0.2s ease;
}

.tick-top { top: 0; }
.tick-middle {
  top: 50%;
  transform: translateX(-50%) translateY(-50%);
}
.tick-bottom { bottom: 0; }

.form-card:hover .rule-tick {
  width: 18px;
}

/* ── 响应式 ──────────────────────────────────── */
@media (max-width: 1023px) {
  .auth-layout {
    flex-direction: column;
  }

  .brand-panel {
    flex: 0 0 auto;
    padding: var(--space-6) var(--space-8);
    align-items: center;
    text-align: center;
  }

  .brand-content {
    max-width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .brand-logo {
    margin-bottom: var(--space-4);
  }

  .brand-statement {
    align-items: center;
  }

  .brand-headline {
    font-size: 2rem;
    margin-bottom: var(--space-4);
  }

  .editorial-mark {
    margin-bottom: var(--space-4);
  }

  .brand-tagline {
    max-width: 420px;
  }

  .form-panel {
    padding: var(--space-6);
    align-items: flex-start;
  }

  .form-card {
    padding: var(--space-8) var(--space-6) var(--space-6) 48px;
  }

  .margin-rule {
    left: 24px;
    top: var(--space-8);
    bottom: var(--space-6);
  }
}

@media (max-width: 767px) {
  .brand-panel {
    padding: var(--space-5) var(--space-6);
  }

  .brand-headline,
  .editorial-mark,
  .brand-tagline {
    display: none;
  }

  .form-panel {
    padding: var(--space-4);
  }

  .form-card {
    max-width: 100%;
    padding: var(--space-8) var(--space-5) var(--space-6) 40px;
    border-radius: var(--radius-md);
  }

  .margin-rule {
    left: 20px;
    width: 1px;
  }

  .rule-tick {
    width: 10px;
    height: 1px;
  }

  .tick-middle {
    display: none;
  }
}

@media (max-width: 479px) {
  .brand-panel {
    display: none;
  }

  .form-panel {
    min-height: 100vh;
    padding: var(--space-4);
    align-items: center;
  }

  .form-card {
    padding: var(--space-8) var(--space-4) var(--space-6) 32px;
  }

  .margin-rule {
    left: 14px;
  }

  .rule-tick {
    width: 8px;
  }

  .tick-bottom {
    display: none;
  }
}
</style>
