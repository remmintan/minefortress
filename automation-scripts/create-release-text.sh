#!/bin/bash
data="{
           \"prompt\": \"$2\",
           \"model\": \"$3\",
           \"max_tokens\": 750,
           \"temperature\": 0.1,
           \"frequency_penalty\": 0.5,
           \"stop\": \"attached\"
       }"
curl --location 'https://api.openai.com/v1/completions' \
--header "Authorization: Bearer $1" \
--header 'Content-Type: application/json' \
--data "$data"