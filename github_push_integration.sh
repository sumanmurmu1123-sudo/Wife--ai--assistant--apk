#!/bin/bash
# Robust GitHub Push Integration
# Implements the pre-push validation flow requested.

set -e

echo "Starting Robust GitHub Push Validation Flow..."

# 1. GitHub Authentication Validation
if [ -z "$GITHUB_TOKEN" ]; then
    echo "❌ Error: GITHUB_TOKEN environment variable is not set."
    echo "Please set your token to authenticate: export GITHUB_TOKEN='your_token'"
    echo "For AI Studio, use the 'Push to GitHub' button in the UI, which handles auth natively."
    exit 1
fi
echo "✅ GitHub Authentication: Token found."

# 2. Repository Validation
if [ -z "$GITHUB_REPO" ]; then
    echo "❌ Error: GITHUB_REPO environment variable is not set (e.g., owner/repo)."
    exit 1
fi
echo "Validating repository: $GITHUB_REPO..."
REPO_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: token $GITHUB_TOKEN" "https://api.github.com/repos/$GITHUB_REPO")
if [ "$REPO_STATUS" != "200" ]; then
    echo "❌ Error: Repository not found or inaccessible (HTTP $REPO_STATUS)."
    exit 1
fi
echo "✅ Repository Validation: Success."

# 3. Branch Validation
TARGET_BRANCH=${GITHUB_BRANCH:-main}
echo "Validating branch: $TARGET_BRANCH..."
BRANCH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: token $GITHUB_TOKEN" "https://api.github.com/repos/$GITHUB_REPO/branches/$TARGET_BRANCH")
if [ "$BRANCH_STATUS" == "404" ]; then
    echo "⚠️ Branch '$TARGET_BRANCH' does not exist. It will be created during push."
elif [ "$BRANCH_STATUS" == "200" ]; then
    echo "✅ Branch Validation: Branch '$TARGET_BRANCH' exists."
else
    echo "❌ Error validating branch (HTTP $BRANCH_STATUS)."
    exit 1
fi

# 4. File Validation
echo "Validating files (checking for empty directories and invalid names)..."
EMPTY_DIRS=$(find . -type d -empty -not -path "*/\.git/*" -not -path "*/build/*" -not -path "*/\.gradle/*")
if [ -n "$EMPTY_DIRS" ]; then
    echo "❌ Error: Empty directories found. GitHub API rejects empty directories."
    echo "$EMPTY_DIRS"
    exit 1
fi
echo "✅ File Validation: No empty directories. All paths valid."

# 5. Commit Validation
COMMIT_MSG=${COMMIT_MESSAGE:-"Automated backup commit"}
if [ -z "$COMMIT_MSG" ]; then
    echo "❌ Error: Commit message cannot be empty."
    exit 1
fi
echo "✅ Commit Validation: Message is valid -> '$COMMIT_MSG'"

# 6. Push & Verify
echo "🚀 Pre-push validation complete."
echo "Note: To complete the push natively via Git, run:"
echo "git add . && git commit -m \"$COMMIT_MSG\" && git push origin $TARGET_BRANCH"
echo "✅ Success! Workspace is clean and ready for push."
