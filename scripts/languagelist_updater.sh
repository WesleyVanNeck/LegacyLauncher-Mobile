#!/bin/bash

# Get the directory of the script
THISDIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)

# Set the path to the language list file
LANGFILE="$THISDIR/../app_pojavlauncher/src/main/assets/language_list.txt"

# Remove the file if it already exists
rm -f "$LANGFILE"

# Get the directories of the values-* folders
LANGDIRS=$(find "$THISDIR/../app_pojavlauncher/src/main/res" -mindepth 1 -maxdepth 1 -type d -name 'values-*' -exec realpath {} +)

# Write the names of the directories to the language list file
echo "$LANGDIRS" | xargs -n 1 basename | sort > "$LANGFILE"
