#!/bin/bash

# Output file
output_file="ai-input.txt"

# Clear output file if it exists
> "$output_file"

# Function to check if a file should be included
should_include_file() {
    local file="$1"

    # Skip if not a regular file
    if [[ ! -f "$file" ]]; then
        return 1
    fi

    # Get lowercase extension
    local ext="${file##*.}"
    ext=$(echo "$ext" | tr '[:upper:]' '[:lower:]')

    # Skip binary and specified file types
    case "$ext" in
        # Image files
        jpg|jpeg|png|gif|bmp|tiff|webp|svg|ico|psd)
            return 1
            ;;
        # Class files
        class)
            return 1
            ;;
        # NBT files
        nbt)
            return 1
            ;;
        # Other binary formats to skip
        exe|dll|so|dylib|obj|bin|zip|jar|tar|gz|rar|7z|pdf|doc|docx|xls|xlsx|ppt|pptx)
            return 1
            ;;
    esac

    # Additional check using file command to detect binary files
    if ! file -b "$file" | grep -q "text"; then
        echo "Skipping non-text file detected by 'file' command: $file"
        return 1
    fi

    return 0
}

# Function to safely append content to the output file with UTF-8 encoding
append_to_output() {
    local file="$1"
    local header="$2"

    # Write the header
    echo "$header" | iconv -t UTF-8//IGNORE >> "$output_file"

    # Try to convert file content to UTF-8 and append to output file
    if ! iconv -f UTF-8 -t UTF-8 < "$file" > /dev/null 2>&1; then
        # If the file is not UTF-8, try to detect encoding and convert
        echo "Converting non-UTF-8 file to UTF-8: $file"
        iconv -f $(file -b --mime-encoding "$file") -t UTF-8//IGNORE < "$file" >> "$output_file" 2>/dev/null || {
            # If conversion fails, try a more aggressive approach
            echo "Failed to detect encoding, using UTF-8//IGNORE for $file"
            cat "$file" | iconv -t UTF-8//IGNORE >> "$output_file"
        }
    else
        # File is already UTF-8
        cat "$file" | iconv -t UTF-8//IGNORE >> "$output_file"
    fi

    # Add newlines
#    echo -e "\n\n" >> "$output_file"
}

# Function to process a directory
process_directory() {
    local dir="$1"

    # Skip if directory doesn't exist
    if [[ ! -d "$dir" ]]; then
        echo "Directory $dir does not exist, skipping."
        return
    fi

    echo "Processing directory: $dir"

    # Find all files in the directory and its subdirectories
    find "$dir" -type f | while read -r file; do
        if should_include_file "$file"; then
            echo "Adding file: $file"
            append_to_output "$file" "==== FILE: $file ===="
        fi
    done
}

# Process specified directories
process_directory "src"
process_directory "automation-scripts"
process_directory ".github"

# Process individual files
individual_files=("build.gradle" "gradle.properties" "settings.gradle")

for file in "${individual_files[@]}"; do
    if [[ -f "$file" ]] && should_include_file "$file"; then
        echo "Adding file: $file"
        append_to_output "$file" "==== FILE: $file ===="
    else
        echo "File $file does not exist or is not a text file, skipping."
    fi
done

# Final conversion to ensure the file is UTF-8
temp_file=$(mktemp)
iconv -t UTF-8//IGNORE < "$output_file" > "$temp_file" && mv "$temp_file" "$output_file"

echo "All text files have been collected into $output_file with UTF-8 encoding"