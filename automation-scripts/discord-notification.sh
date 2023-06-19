#!/bin/bash

WEBHOOK_URL="$1"  # Replace with your webhook URL

build_number="$2"  # Replace with the parameter for build number
commit_messages=$3  # Replace with the parameter for commit messages
role_id="$4"  # Replace with the parameter for role ID

message_body="New dev build available!
Build number: ${build_number}

What's new:
${commit_messages}

https://builds.minefortress.org/

<@${role_id}>"

escaped_message_body=${message_body//$'\n'/\\n}

json_payload=$(
  cat <<EOF
  {
    "content": "${escaped_message_body}"
  }
EOF
)

echo "Sending message to Discord..."
curl -H "Content-Type: application/json" \
     -d "$json_payload" "$WEBHOOK_URL"
echo "Message sent!"
