---
name: reflect
description: Analyze the current session and propose improvements to skills/agents. Run after using a skill to capture learnings. Use when user says "reflect", "improve skill", "learn from this", or at end of sessions.
compatibility: opencode
---

# Reflect Skill

Analyze the current conversation and propose improvements to skills/agents based on what worked, what didn't, and edge cases discovered.

## Trigger

Run `/reflect` or `/reflect [skill-name]` after a session.

## Workflow

### Step 1: Identify the Target

If skill/agent name not provided, ask:

```
Which skill/agent should I analyze this session for?
- Skills: (list from `ls .opencode/skills`)
- Primary Agents: (list from `ls .opencode/agent`)
- Subagents: (list from `ls .opencode/agent/subagents`)

Which of the Skills/Agents would you like me to analyze?
- <Options from the `ls` command of the relevant directories>
```

### Step 2: Analyze the Conversation

Scan for these signals:

**Corrections** (HIGH confidence):
- User said "no", "not like that", "I meant..."
- User explicitly corrected output
- User asked for changes immediately after generation
- User said "always do X" or "never do Y"

**Successes** (MEDIUM confidence):
- User said "perfect", "great", "yes", "exactly"
- User accepted output without modification
- User built on top of the output

**Edge Cases** (MEDIUM confidence):
- Questions the skill didn't anticipate
- Scenarios requiring workarounds
- Errors that required debugging

### Step 3: Propose Changes

```
┌─ Skill Reflection: [skill-name] ─────────────────────┐
│                                                       │
│  Signals: X corrections, Y successes, Z edge cases   │
│                                                       │
│  Proposed changes:                                    │
│                                                       │
│  🔴 [HIGH] + Add: "[specific constraint]"            │
│  🟡 [MED]  + Add: "[specific preference]"            │
│  🔵 [LOW]  ~ Note: "[observation]"                   │
│                                                       │
│  Commit: "[skill]: [summary of changes]"             │
│                                                       │
└───────────────────────────────────────────────────────┘

Apply these changes? [Y/n] or describe tweaks
```

### Step 4: If Approved

1. Read the skill/agent file
2. Apply changes with Edit tool
3. Show the diff
4. Git commit (if repo initialized):
   ```bash
   git add [file-path]
   git commit -m "[skill]: [change summary]"
   ```
5. Confirm: "Skill updated"

### Step 5: If Declined

Ask: "Save these observations for later review?"

If yes, append to `.opencode/reflect/OBSERVATIONS.md`

## File Paths

- skills → `.opencode/skills/[skill-name]/SKILL.md`
- primary agents → `.opencode/agent/[agent-name].md`
- subagents → `.opencode/agent/subagents/[agent-name].md`


## What Makes a Good Addition

✅ **Specific and actionable:**
- "Always use @Transactional on service methods calling multiple repositories"
- "Use Optional.orElseThrow() with ResourceNotFoundException, never .get()"
- "Validate DTOs in validators, not in service methods"

❌ **Too vague (don't add):**
- "Write better code"
- "Follow best practices"
- "Handle errors properly"

## Example

After a backend session where user corrected: "No, always use explicit @Column annotations"

```
┌─ Skill Reflection: springboot-standards ─────────────┐
│                                                       │
│  Signals: 1 correction, 2 successes                  │
│                                                       │
│  Proposed changes:                                    │
│                                                       │
│  🔴 [HIGH] + Entity Pattern:                         │
│            "Always use explicit @Column annotations  │
│             even when column name matches field"     │
│                                                       │
│  🟡 [MED]  + Validation:                             │
│            "Check for existing email before save     │
│             in registration flow"                    │
│                                                       │
│  Commit: "springboot-standards: explicit @Column,    │
│           email uniqueness check"                    │
│                                                       │
└───────────────────────────────────────────────────────┘

Apply these changes? [Y/n] or describe tweaks
```
