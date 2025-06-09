#!/bin/bash
#
# Script to generate a changelog from commit messages using OpenAI's Chat Completions API.
#
# Usage: ./generate-changelog.sh "API_KEY" "COMMIT_MESSAGES" "MODEL_NAME"

# Check for required arguments
if [ "$#" -ne 3 ]; then
    echo "Usage: $0 API_KEY COMMIT_MESSAGES MODEL_NAME"
    exit 1
fi

API_KEY="$1"
COMMIT_MESSAGES="$2"
MODEL_NAME="$3"

# The system prompt gives the AI its instructions and persona.
# This is key to getting good, consistent results.
SYSTEM_PROMPT="You are a release notes author for a Minecraft mod named MineFortress. Your task is to transform a list of git commit messages into a user-friendly, well-formatted changelog in Markdown. Group related changes under logical headings like '✨ Features', '🐛 Bug Fixes', and '🔧 Quality of life'. Rewrite the commit messages to be clear and concise for players, try to make them short and straightforward. Omit trivial and technical changes. Omit mod name in the changelog. The output should be only the Markdown content, without any introductory or concluding text."

# Construct the JSON payload using jq for safety and proper escaping of multi-line strings.
json_payload=$(jq -n \
  --arg model "$MODEL_NAME" \
  --arg system_prompt "$SYSTEM_PROMPT" \
  --arg user_content "Please generate a changelog for the following commits:\n\n$COMMIT_MESSAGES" \
  '{
    "model": $model,
    "messages": [
      {"role": "system", "content": $system_prompt},
      {"role": "user", "content": $user_content}
    ],
    "temperature": 0.2,
    "max_tokens": 1024
  }')

# Make the API call using curl
curl --location 'https://api.openai.com/v1/chat/completions' \
     --header "Authorization: Bearer $API_KEY" \
     --header 'Content-Type: application/json' \
     --data-raw "$json_payload"