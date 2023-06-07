#!/bin/bash
tag=$(git describe --tags --abbrev=0 HEAD)
previous_tag=$(git describe --tags --abbrev=0 "${tag}^")
commits_texts=$(git log --reverse --format=%s "${previous_tag}".."${tag}" | awk '{printf "%s\\n", $0}' )
prepared_commits_texts="COMMITS:TAG: ${tag}\\n${commits_texts}\\nRELEASE_NOTES:"

echo "${prepared_commits_texts}" | tr -d '"'