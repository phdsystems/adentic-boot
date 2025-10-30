#!/bin/bash

# verify-links.sh - Verify all markdown cross-references
# Usage: ./scripts/verify-links.sh

set -e

echo "=== Verifying Markdown Cross-References ==="
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

ERRORS=0
WARNINGS=0
CHECKED=0

# Function to check if a file exists
check_file() {
    local file="$1"
    local referenced_from="$2"

    if [ -f "$file" ]; then
        echo -e "${GREEN}✓${NC} $file (referenced from $referenced_from)"
        ((CHECKED++))
        return 0
    else
        echo -e "${RED}✗${NC} $file NOT FOUND (referenced from $referenced_from)"
        ((ERRORS++))
        return 1
    fi
}

# Function to extract and verify links from a markdown file
verify_links_in_file() {
    local md_file="$1"
    local base_dir=$(dirname "$md_file")

    if [ ! -f "$md_file" ]; then
        echo -e "${YELLOW}⚠${NC} Skipping $md_file (file not found)"
        ((WARNINGS++))
        return
    fi

    echo ""
    echo "Checking links in: $md_file"
    echo "---"

    # Extract markdown links: [text](path.md) or [text](path.md#anchor)
    # Remove anchors (#section) for file verification
    grep -oP '\[.*?\]\(\K[^)]+(?=\))' "$md_file" 2>/dev/null | grep '\.md' | while read -r link; do
        # Remove anchor if present
        link_without_anchor="${link%%#*}"

        # Skip external URLs
        if [[ "$link_without_anchor" =~ ^https?:// ]]; then
            continue
        fi

        # Resolve relative path
        if [[ "$link_without_anchor" == /* ]]; then
            # Absolute path
            target_file="$link_without_anchor"
        else
            # Relative path
            target_file="$base_dir/$link_without_anchor"
        fi

        # Normalize path (resolve .. and .)
        target_file=$(realpath -m "$target_file" 2>/dev/null || echo "$target_file")

        check_file "$target_file" "$md_file"
    done
}

echo "Starting verification from: $(pwd)"
echo ""

# Verify key documentation files
echo "=== Core Architecture Documents ==="
verify_links_in_file "doc/3-design/framework-architecture-overview.md"
verify_links_in_file "doc/3-design/architecture-modular-split.md"
verify_links_in_file "doc/3-design/design-index.md"
verify_links_in_file "doc/3-design/decisions/0003-domain-specific-annotation-module.md"
verify_links_in_file "ARCHITECTURE_SPLIT_PLAN.md"

# Verify all other design documents
echo ""
echo "=== All Design Documents ==="
find doc/3-design -name "*.md" -type f | while read -r file; do
    verify_links_in_file "$file"
done

# Summary
echo ""
echo "=== Verification Summary ==="
echo -e "Files checked: $CHECKED"
echo -e "${GREEN}Verified links: $CHECKED${NC}"
echo -e "${RED}Errors (broken links): $ERRORS${NC}"
echo -e "${YELLOW}Warnings (skipped files): $WARNINGS${NC}"

if [ $ERRORS -gt 0 ]; then
    echo ""
    echo -e "${RED}✗ Verification FAILED - $ERRORS broken links found${NC}"
    exit 1
else
    echo ""
    echo -e "${GREEN}✓ All cross-references verified successfully!${NC}"
    exit 0
fi
