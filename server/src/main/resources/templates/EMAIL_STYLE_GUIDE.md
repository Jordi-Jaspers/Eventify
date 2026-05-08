# Eventify Email Template Style Guide

## Brand Colors

| Color | Hex | Usage |
|-------|-----|-------|
| Primary Blue | `#0ea5e9` | Header gradient start, CTA buttons |
| Accent Purple | `#a855f7` | Header gradient end |
| Background | `#f1f5f9` | Email body background |
| Card | `#ffffff` | Content card background |
| Text Dark | `#1e293b` | Headings |
| Text Body | `#475569` | Body text |
| Text Muted | `#64748b` | Secondary text, disclaimers |
| Text Footer | `#94a3b8` | Footer text |
| Divider | `#e2e8f0` | Horizontal rules |

## Typography

- **Font Family**: Ubuntu, Helvetica, Arial, sans-serif
- **Headings**: 24px, weight 600, color `#1e293b`
- **Body**: 14px, weight 400, line-height 1.6, color `#475569`
- **Footer**: 11-12px, color `#94a3b8`

## Layout Structure

```
┌─────────────────────────────────────┐
│  Gradient Header (blue → purple)    │
│         "Eventify" (28px white)     │
├─────────────────────────────────────┤
│           20px spacer               │
├─────────────────────────────────────┤
│  ┌─────────────────────────────┐    │
│  │  White Card (12px radius)   │    │
│  │                             │    │
│  │  Title (24px, centered)     │    │
│  │                             │    │
│  │  Body text (14px)           │    │
│  │                             │    │
│  │  [  CTA Button  ]           │    │
│  │                             │    │
│  │  Disclaimer (13px muted)    │    │
│  │  ─────────────────────      │    │
│  │  Signature                  │    │
│  └─────────────────────────────┘    │
├─────────────────────────────────────┤
│  Footer (centered, muted)           │
│  © 2026 Eventify. All rights...     │
│  Tagline                            │
└─────────────────────────────────────┘
```

## Components

### Header
```xml
<mj-section css-class="gradient-header" background-color="#0ea5e9" padding="24px 0">
  <mj-column>
    <mj-text align="center" color="#ffffff" font-size="28px" font-weight="600">
      Eventify
    </mj-text>
  </mj-column>
</mj-section>
```

Note: The gradient is defined in `<mj-style>`:
```css
.gradient-header { background: linear-gradient(135deg, #0ea5e9 0%, #a855f7 100%); }
```

### Content Card
```xml
<mj-section background-color="#f1f5f9" padding="0 20px">
  <mj-column background-color="#ffffff" border-radius="12px" padding="32px 24px">
    <!-- Content here -->
  </mj-column>
</mj-section>
```

### CTA Button
```xml
<mj-button background-color="#0ea5e9" color="#ffffff" border-radius="8px" 
           padding="16px 0" inner-padding="14px 32px">
  <a th:href="${applicationUrl} + '/path?token=' + ${token}">Button Text</a>
</mj-button>
```

### Divider
```xml
<mj-divider border-color="#e2e8f0" border-width="1px" padding="24px 0 16px 0" />
```

### Footer
```xml
<mj-section background-color="#f1f5f9" padding="24px 0">
  <mj-column>
    <mj-text align="center" color="#94a3b8" font-size="12px">
      © 2026 Eventify. All rights reserved.
    </mj-text>
    <mj-text align="center" color="#94a3b8" font-size="11px" padding-top="8px">
      Real-time monitoring and event tracking
    </mj-text>
  </mj-column>
</mj-section>
```

## Thymeleaf Variables

| Variable | Description |
|----------|-------------|
| `${applicationUrl}` | Base URL (e.g., `https://eventify.app`) |
| `${token}` | Verification or reset token |
| `${emailAddress}` | User's email (optional) |

## Compiling Templates

### Option 1: Online (mjml.io)
1. Go to https://mjml.io/try-it-live
2. Paste `.mjml` content
3. Copy generated HTML to `.html` file

### Option 2: CLI
```bash
cd server/src/main/resources/templates
npx mjml template-name.mjml -o template-name.html
```

Note: MJML will warn about `th:href` being illegal - this is expected. Thymeleaf processes it at runtime.

## Creating New Templates

1. Copy an existing `.mjml` file as a starting point
2. Update the `<mj-title>` tag
3. Modify content within the card section
4. Keep header, footer, and overall structure consistent
5. Compile to HTML
6. Test with Thymeleaf variable substitution

## Design Principles

- **Clean & Professional**: Minimal design, generous whitespace
- **Mobile-First**: MJML handles responsive automatically
- **Accessible**: High contrast text, clear hierarchy
- **Trustworthy**: Consistent branding builds user confidence
- **Action-Oriented**: Single clear CTA per email
