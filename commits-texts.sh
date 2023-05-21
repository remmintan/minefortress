#!/bin/bash

# Function to get the commit messages between two tags
get_commit_messages() {
  local tag1=$1
  local tag2=$2

  # Use git log to retrieve commit messages between the two tags
  git log --pretty=format:"%s" "$tag1".."$tag2" | grep -v "Merge branch"
}

# Switch to the master branch
git checkout master

# Get all tags on the master branch
tags=($(git tag --sort=-creatordate | grep -v "server" | grep -v "1.17.1"))

# Create the commits.txt file
touch commits.txt

# Iterate over the tags and retrieve commit messages
for ((i = 0; i < ${#tags[@]} - 1; i++)); do
  tag=${tags[i]}
  next_tag=${tags[i + 1]}

  # Get commit messages between the current tag and the next tag
  commit_messages=$(get_commit_messages  "$next_tag" "$tag")

  # Append the tag and commit messages to the commits.txt file
  echo "TAG: $tag" >> commits.txt
  echo "$commit_messages" >> commits.txt
  echo "====================" >> commits.txt
  echo "" >> commits.txt
done

# Append the last tag's commit messages to the commits.txt file
last_tag=${tags[-1]}
last_commit_messages=$(get_commit_messages "$last_tag" HEAD)
echo "TAG: $last_tag" >> commits.txt
echo "$last_commit_messages" >> commits.txt

echo "Commit messages have been saved to commits.txt."
