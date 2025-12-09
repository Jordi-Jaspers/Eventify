<!--
  AppBackground Component

  Enterprise-grade animated background with grid pattern and gradient orbs.
  Provides consistent background across all pages with glassmorphism effects.

  Props:
  - None (uses slot for content)

  Usage:
  <AppBackground>
    <YourContent />
  </AppBackground>
-->
<script lang="ts">
</script>

<!-- Enterprise Background with Grid and Gradient Orbs -->
<div class="animated-grid-bg min-h-screen">
    <!-- Gradient Orbs -->
    <div class="gradient-orb gradient-orb-1"></div>
    <div class="gradient-orb gradient-orb-2"></div>

    <!-- Content Container with proper z-index -->
    <div class="relative z-10">
        <slot />
    </div>
</div>
