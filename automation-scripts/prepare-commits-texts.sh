#!/bin/bash

# Get the latest tag, which triggered the workflow
tag=$(git describe --tags --abbrev=0 HEAD)

# Get the tag before the latest one to define the commit range
previous_tag=$(git describe --tags --abbrev=0 "${tag}^")

# Get commit subjects between the two tags, formatted as a markdown list.
# The output is a clean, multi-line string.
commits_list=$(git log --pretty=format:"- %s" "${previous_tag}".."${tag}" | grep -v "Merge branch")

# Output the raw list of commits
echo "$commits_list"