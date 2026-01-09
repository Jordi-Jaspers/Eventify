---
description: Analyze this session and propose improvements to a skill or agent
---

Analyze this session and propose skill improvements for: $ARGUMENTS

## Instructions

1. **Identify target**: Use `$ARGUMENTS` if provided, otherwise ask user to pick from:
    - primary agents (`ls .opencode/agent`)
    - subagents (`ls .opencode/agent/subagents`)
    - skills (`ls .opencode/skills`)
   
And then list available options for selection.

2. **Scan conversation** for:
    - 🔴 **Corrections**: User said "no", "not like that", "always/never do X"
    - 🟡 **Successes**: User said "perfect", "great", accepted without changes
    - 🔵 **Edge cases**: Workarounds needed, questions not covered

3. **Propose changes**:

```
┌─ Skill Reflection: [skill-name] ─────────────────────┐
│                                                       │
│  Signals: X corrections, Y successes, Z edge cases   │
│                                                       │
│  Proposed changes:                                    │
│                                                       │
│  🔴 [HIGH] + [category]: "[specific change]"         │
│  🟡 [MED]  + [category]: "[specific change]"         │
│  🔵 [LOW]  ~ Note: "[observation]"                   │
│                                                       │
│  Commit: "[skill]: [summary]"                        │
│                                                       │
└───────────────────────────────────────────────────────┘

Apply these changes? [Y/n] or describe tweaks
```

4. **If approved**: Edit skill file, show diff, git commit
5. **If declined**: Optionally save to `.opencode/reflect/OBSERVATIONS.md`

## File Paths

- skills → `.opencode/skills/[skill-name]/SKILL.md`
- primary agents → `.opencode/agent/[agent-name].md`
- subagents → `.opencode/agent/subagents/[agent-name].md`

## Rules

- Be specific - no vague improvements
- Always show changes before applying
- Never modify without explicit approval
