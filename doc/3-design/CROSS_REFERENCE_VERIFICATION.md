# Cross-Reference Verification Guide

**Date:** 2025-10-25
**Purpose:** How to verify markdown cross-references are correct

---

## Manual Verification (Quick Method)

### Step 1: Extract All Links from a File

```bash
# Example: Check framework-architecture-overview.md
grep -n "\.md)" doc/3-design/framework-architecture-overview.md
```

**Output shows:**
- Line number where link appears
- Full markdown link syntax

### Step 2: Verify Each Referenced File Exists

For each link found, verify the file exists:

```bash
# Example links from framework-architecture-overview.md:
ls -la doc/3-design/decisions/0003-domain-specific-annotation-module.md  ✅
ls -la doc/3-design/architecture-modular-split.md                         ✅
ls -la ARCHITECTURE_SPLIT_PLAN.md                                         ✅
ls -la doc/4-development/coding-standards.md                              ✅
ls -la doc/CONTRIBUTING.md                                                ✅
```

**If file exists:** ✅ Link is valid
**If file not found:** ❌ Broken link

---

## Understanding Relative Paths

### From `doc/3-design/` Files

**Same directory:**

```markdown
[architecture-modular-split.md](architecture-modular-split.md)
```

File must be in: `doc/3-design/architecture-modular-split.md` ✅

**Subdirectory:**

```markdown
[ADR-0003](decisions/0003-domain-specific-annotation-module.md)
```

File must be in: `doc/3-design/decisions/0003-domain-specific-annotation-module.md` ✅

**Parent directory (2 levels up):**

```markdown
[ARCHITECTURE_SPLIT_PLAN.md](../../ARCHITECTURE_SPLIT_PLAN.md)
```

- From: `doc/3-design/framework-architecture-overview.md`
- Up 1: `doc/`
- Up 2: ` (root)`
- Target: `ARCHITECTURE_SPLIT_PLAN.md`
- File must be in: `/ARCHITECTURE_SPLIT_PLAN.md` ✅

**Parent's sibling directory:**

```markdown
[coding-standards.md](../4-development/coding-standards.md)
```

- From: `doc/3-design/framework-architecture-overview.md`
- Up 1: `doc/`
- Down: `4-development/`
- File must be in: `doc/4-development/coding-standards.md` ✅

---

## Automated Verification Script

### Usage

```bash
# Make script executable (one time)
chmod +x scripts/verify-links.sh

# Run verification
./scripts/verify-links.sh
```

### What It Does

1. **Scans all markdown files** in key directories
2. **Extracts all `.md` links** using regex
3. **Resolves relative paths** (handles `..` and `.`)
4. **Verifies each file exists** using `ls` or `realpath`
5. **Reports results:**
   - ✅ Green checkmark for valid links
   - ❌ Red X for broken links
   - Summary with error count

### Example Output

```
=== Verifying Markdown Cross-References ===

Checking links in: doc/3-design/framework-architecture-overview.md
---
✓ doc/3-design/decisions/0003-domain-specific-annotation-module.md
✓ doc/3-design/architecture-modular-split.md
✓ ARCHITECTURE_SPLIT_PLAN.md
✓ doc/4-development/coding-standards.md
✓ doc/CONTRIBUTING.md

=== Verification Summary ===
Files checked: 47
Verified links: 47
Errors (broken links): 0
Warnings (skipped files): 0

✓ All cross-references verified successfully!
```

---

## Manual Checklist for Key Documents

### framework-architecture-overview.md

- [x] decisions/0003-domain-specific-annotation-module.md
- [x] architecture-modular-split.md
- [x] ../../ARCHITECTURE_SPLIT_PLAN.md
- [x] ../4-development/coding-standards.md
- [x] ../CONTRIBUTING.md

### architecture-modular-split.md

- [x] decisions/0003-domain-specific-annotation-module.md
- [x] framework-architecture-overview.md

### design-index.md

- [x] framework-architecture-overview.md
- [x] architecture-modular-split.md
- [x] architecture.md
- [x] ade-architecture-tiers.md
- [x] api-design.md
- [x] component-specifications.md
- [x] data-model.md
- [x] dataflow.md
- [x] workflow.md
- [x] error-handling-strategy.md
- [x] decisions/0003-domain-specific-annotation-module.md
- [x] decisions/001-code-formatting-tool-selection.md
- [x] decisions/002-checkstyle-configuration-update.md
- [x] generic-refactoring-plan.md
- [x] implementation-test-strategy.md

### ADR-0003

- [x] All references are internal (markdown anchors, no file links)

### ARCHITECTURE_SPLIT_PLAN.md

- [x] doc/3-design/framework-architecture-overview.md
- [x] doc/3-design/architecture-modular-split.md
- [x] doc/3-design/decisions/0003-domain-specific-annotation-module.md
- [x] doc/3-design/design-index.md

---

## Quick Verification Commands

### Find all markdown links in a file

```bash
grep -o '\[.*\]([^)]*\.md[^)]*)' doc/3-design/framework-architecture-overview.md
```

### Find all referenced files

```bash
grep -oP '\[.*?\]\(\K[^)]+(?=\))' doc/3-design/framework-architecture-overview.md | grep '\.md'
```

### Check if a specific file exists

```bash
test -f doc/3-design/decisions/0003-domain-specific-annotation-module.md && echo "✅ Exists" || echo "❌ Not found"
```

### Verify all decision files exist

```bash
for file in doc/3-design/decisions/*.md; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file NOT FOUND"
    fi
done
```

---

## Common Link Issues

### Issue 1: Relative Path Incorrect

**Problem:**

```markdown
[ADR-0003](0003-domain-specific-annotation-module.md)
```

**From:** `doc/3-design/framework-architecture-overview.md`
**Looking for:** `doc/3-design/0003-domain-specific-annotation-module.md`
**Actual location:** `doc/3-design/decisions/0003-domain-specific-annotation-module.md`

**Fix:**

```markdown
[ADR-0003](decisions/0003-domain-specific-annotation-module.md)
```

### Issue 2: File Renamed or Moved

**Problem:** Link points to old filename

**Fix:** Update link to new filename/location

### Issue 3: Anchor Link to Non-Existent Section

**Problem:**

```markdown
[Section 7](decisions/0003-domain-specific-annotation-module.md#section-7)
```

**Section actually named:** `#7-bean-scopes-and-lifecycle-management`

**Fix:**

```markdown
[Section 7](decisions/0003-domain-specific-annotation-module.md#7-bean-scopes-and-lifecycle-management)
```

**Note:** GitHub markdown anchors:
- Convert to lowercase
- Replace spaces with hyphens
- Remove special characters except hyphens
- `## 7. Bean Scopes` → `#7-bean-scopes`

---

## Testing Links in GitHub

1. **Push to GitHub**
2. **Navigate to markdown file** in GitHub UI
3. **Click each link** to verify it works
4. **Check anchor links** navigate to correct section

**Tip:** GitHub's markdown renderer handles relative paths correctly, so if it works locally with proper paths, it will work on GitHub.

---

## CI/CD Integration

Add to `.github/workflows/verify-docs.yml`:

```yaml
name: Verify Documentation Links

on:
  pull_request:
    paths:
      - 'doc/**/*.md'
      - 'ARCHITECTURE_SPLIT_PLAN.md'

jobs:
  verify-links:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Verify Markdown Links
        run: |
          chmod +x scripts/verify-links.sh
          ./scripts/verify-links.sh

      - name: Report Results
        if: failure()
        run: echo "❌ Broken links found. Please fix before merging."
```

**Benefit:** Automatically catches broken links in PRs

---

## Summary

**Manual verification process:**
1. Extract links: `grep -n "\.md)" <file>`
2. Verify existence: `ls -la <referenced-file>`
3. Check relative paths resolve correctly

**Automated verification:**
1. Run: `./scripts/verify-links.sh`
2. Fix any reported broken links
3. Re-run until all pass

**Current status (2025-10-25):** ✅ All cross-references verified and working

---

*Last Updated: 2025-10-25*
*Status: All Links Verified*
