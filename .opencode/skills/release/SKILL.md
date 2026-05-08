# Skill: Release

Create a new release following git flow conventions.

## Version Locations

| File | Format |
|------|--------|
| `server/gradle.properties` | `version="X.Y.Z-SNAPSHOT"` → `version="X.Y.Z"` |
| `client/package.json` | `"version": "X.Y.Z-SNAPSHOT"` → `"version": "X.Y.Z"` |

## Tag Format

`X.Y.Z` (no `v` prefix)

## Steps

### 1. Create release branch from develop

```bash
git checkout develop && git pull
git checkout -b release/X.Y.Z
```

### 2. Bump versions to release (remove -SNAPSHOT)

- `server/gradle.properties`: `version="X.Y.Z"`
- `client/package.json`: `"version": "X.Y.Z"`

### 3. Commit and push release branch

```bash
git add -A && git commit -m "chore(release): bump version to X.Y.Z"
git push origin release/X.Y.Z
```

### 4. Tag the release on the release branch

```bash
git tag X.Y.Z
git push origin X.Y.Z
```

### 5. Merge the tag back into master

```bash
git checkout master && git pull
git merge --no-ff X.Y.Z -m "release: X.Y.Z"
git push origin master
```

### 6. Merge the tag back into develop

```bash
git checkout develop
git merge --no-ff X.Y.Z -m "chore: merge release X.Y.Z into develop"
git push origin develop
```

### 7. Delete release branch

```bash
git branch -d release/X.Y.Z
git push origin --delete release/X.Y.Z
```

### 8. Bump versions to next SNAPSHOT on develop

- `server/gradle.properties`: `version="X.Y+1.0-SNAPSHOT"`
- `client/package.json`: `"version": "X.Y+1.0-SNAPSHOT"`

```bash
git add -A && git commit -m "chore: next development cycle X.Y+1.0-SNAPSHOT"
git push origin develop
```

## Notes

- Always confirm release version and next dev version with user before starting
- Never force push
- Tag lives on the release branch, then gets merged to both master and develop
- If conflicts arise during merge, resolve and continue
