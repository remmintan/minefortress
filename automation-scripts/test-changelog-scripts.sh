#!/bin/bash

# ==============================================================================
# MineFortress Changelog Generation Test Script
#
# This script simulates the changelog generation process from the GitHub workflow.
# It retrieves commit messages between the two most recent tags and sends them
# to the OpenAI API to generate a changelog.
#
# REQUIREMENTS:
# 1. An OpenAI API key must be set as an environment variable.
#    Run this in your terminal before executing the script:
#    export OPENAI_API_KEY="your_secret_key_here"
#
# 2. `jq` must be installed to parse the JSON response from the API.
#    (On macOS: `brew install jq`, on Debian/Ubuntu: `sudo apt-get install jq`)
#
# 3. The scripts `prepare-commits-texts.sh` and `generate-changelog.sh`
#    must be in the `automation-scripts` directory.
#
# USAGE:
# 1. Make the script executable: `chmod +x test-changelog-scripts.sh`
# 2. Run it: `./test-changelog-scripts.sh`
# 3. (Optional) Specify a different model: `./test-changelog-scripts.sh gpt-3.5-turbo`
# ==============================================================================

# --- Configuration ---
# Use the first command-line argument as the model name, or default to "gpt-4o".
MODEL_NAME="${1:-gpt-4o-mini}"
API_KEY_VAR_NAME="OPENAI_API_KEY"
SCRIPTS_DIR="./automation-scripts"

# --- Helper Functions ---
print_step() {
  echo ""
  echo "--- $1 ---"
}

print_error() {
  echo "[ERROR] $1" >&2
  exit 1
}

# --- Pre-flight Checks ---
print_step "Performing Pre-flight Checks"

# Check for OpenAI API key
if [[ -z "${!API_KEY_VAR_NAME}" ]]; then
  print_error "OPENAI_API_KEY environment variable is not set. Please run: export OPENAI_API_KEY=\"your_key_here\""
fi
echo "✅ OpenAI API key found."

# Check for jq
if ! command -v jq &> /dev/null; then
  print_error "'jq' is not installed. Please install it to parse the API response. (e.g., 'sudo apt-get install jq' or 'brew install jq')"
fi
echo "✅ jq is installed."

# Check for required scripts
if [ ! -x "${SCRIPTS_DIR}/prepare-commits-texts.sh" ]; then
    print_error "Script not found or not executable: ${SCRIPTS_DIR}/prepare-commits-texts.sh"
fi
if [ ! -x "${SCRIPTS_DIR}/generate-changelog.sh" ]; then
    print_error "Script not found or not executable: ${SCRIPTS_DIR}/generate-changelog.sh"
fi
echo "✅ Required scripts are present and executable."

# --- Main Logic ---

# Step 1: Get commit messages
print_step "Step 1: Retrieving commit messages"

COMMITS_LIST=$("${SCRIPTS_DIR}/prepare-commits-texts.sh")
if [ $? -ne 0 ]; then
    print_error "Failed to retrieve commit messages. Make sure you have at least two tags in your repository."
fi

echo "Found the following commits to summarize:"
echo -e "$COMMITS_LIST"


# Step 2: Generate changelog using the AI script
print_step "Step 2: Sending commits to OpenAI API (Model: $MODEL_NAME)"

# Note: The quotes around "$COMMITS_LIST" are crucial to preserve newlines.
API_RESPONSE=$("${SCRIPTS_DIR}/generate-changelog.sh" "${!API_KEY_VAR_NAME}" "$COMMITS_LIST" "$MODEL_NAME")
if [ $? -ne 0 ]; then
    print_error "The 'generate-changelog.sh' script failed to execute."
fi

# Step 3: Parse and display the result
print_step "Step 3: Parsing API Response"

# Check if the response contains an error object
if echo "$API_RESPONSE" | jq -e '.error' > /dev/null; then
  echo "API returned an error:"
  echo "$API_RESPONSE" | jq '.'
  print_error "OpenAI API request failed."
fi

# Extract the content from the JSON response
CHANGELOG_CONTENT=$(echo "$API_RESPONSE" | jq -r '.choices[0].message.content')

if [ -z "$CHANGELOG_CONTENT" ] || [ "$CHANGELOG_CONTENT" == "null" ]; then
    echo "Could not extract changelog content from the API response. Full response:"
    echo "$API_RESPONSE" | jq '.'
    print_error "Failed to parse a valid changelog from the API response."
fi

# Final Output
print_step "Generated Changelog"
echo -e "$CHANGELOG_CONTENT"
echo ""
echo "--- Test complete. ---"