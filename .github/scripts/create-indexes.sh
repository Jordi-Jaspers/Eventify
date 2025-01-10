#!/bin/bash

# .github/scripts/create-indexes.sh
create_index() {
  local dir=$1
  local title=$2
  local index_file="$dir/index.html"

  # Get list of subdirectories and files
  local items=($(find "$dir" -maxdepth 1 -mindepth 1 -type d -printf "%f\n" | sort))
  local files=($(find "$dir" -maxdepth 1 -mindepth 1 -type f -name "*.html" ! -name "index.html" -printf "%f\n" | sort))

  # Create index file
  cat > "$index_file" << EOF
<!DOCTYPE html>
<html>
<head>
  <title>$title</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 2em; }
    .back-link { margin-bottom: 2em; }
  </style>
</head>
<body>
  <div class="back-link"><a href="../">← Back to parent directory</a></div>
  <h1>$title</h1>
EOF

  # Add directories
  if [ ${#items[@]} -gt 0 ]; then
    echo "<h2>Directories:</h2><ul>" >> "$index_file"
    for item in "${items[@]}"; do
      echo "<li><a href=\"$item/\">$item</a></li>" >> "$index_file"
    done
    echo "</ul>" >> "$index_file"
  fi

  # Add files
  if [ ${#files[@]} -gt 0 ]; then
    echo "<h2>Reports:</h2><ul>" >> "$index_file"
    for file in "${files[@]}"; do
      echo "<li><a href=\"$file\">$file</a></li>" >> "$index_file"
    done
    echo "</ul>" >> "$index_file"
  fi

  # Close HTML
  echo "</body></html>" >> "$index_file"
}

# Create all necessary indexes
create_all_indexes() {
  local run_id=$1
  local reports_dir="reports/$run_id"

  # Create indexes for quality report directories
  if [ -d "$reports_dir/quality" ]; then
    create_index "$reports_dir/quality" "Quality Reports"

    for dir in $reports_dir/quality/*/; do
      if [ -d "$dir" ]; then
        create_index "$dir" "$(basename "$dir") Report"
      fi
    done
  fi

  # Create index for test reports
  if [ -d "$reports_dir/tests" ]; then
    create_index "$reports_dir/tests" "Test Reports"
  fi
}

# Execute if run directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <run_id>"
    exit 1
  fi
  create_all_indexes "$1"
fi
