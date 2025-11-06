# Commit Guidelines for Claude Code

## Instructions for commits and push

Los commit siempre deben estar en inglés.

When making commits, follow these guidelines:

### 1. Files to commit
- **ONLY commit source code files** (.kt, .xml, .gradle, .md, etc.)
- **DO NOT commit** binary files like:
  - `app/release/app-release.apk`
  - `app/release/baselineProfiles/*.dm`
  - Other build artifacts

### 2. Commit message format
Use conventional commits format:

```
<type>: <short description>

<optional longer description explaining the change>
```

**Types:**
- `feat:` - New feature
- `fix:` - Bug fix
- `refactor:` - Code refactoring
- `docs:` - Documentation changes
- `style:` - Code style/formatting
- `test:` - Adding tests
- `chore:` - Maintenance tasks

### 3. Commit process
1. Run `git status` to review changes
2. Run `git diff` to see detailed changes
3. Add only source files: `git add <source-files>`
4. Create commit with proper message format
5. Push: `git push`

### 4. Example
```bash
git add app/src/main/java/com/example/qvapayappandroid/presentation/ui/home/components/MyOfferCard.kt
git commit -m "$(cat <<'EOF'
fix: prevent long names from compressing UI components

Added text overflow handling to ensure long text truncates gracefully without affecting layout.
EOF
)"
git push
```