#!/bin/bash

# .github/scripts/create-indexes.sh
create_index() {
  local dir=$1
  local title=$2
  local index_file="$dir/index.html"

  # Get list of subdirectories and files
  local items=($(find "$dir" -maxdepth 1 -mindepth 1 -type d -printf "%f\n" | sort))
  local files=($(find "$dir" -maxdepth 1 -mindepth 1 -type f -name "*.html" ! -name "index.html" -printf "%f\n" | sort))

  # Create index file with improved styling
  cat > "$index_file" << EOF
<!DOCTYPE html>
<html>
<head>
  <title>$title</title>
  <style>
    body {
      font-family: system-ui, -apple-system, sans-serif;
      margin: 0;
      padding: 2rem;
      line-height: 1.6;
      color: #333;
      max-width: 1200px;
      margin: 0 auto;
    }
    .back-link {
      margin-bottom: 2em;
      display: inline-block;
      padding: 0.5em 1em;
      color: #666;
      text-decoration: none;
      border-radius: 4px;
      transition: background-color 0.2s;
    }
    .back-link:hover {
      background-color: #f0f0f0;
    }
    .back-link:before {
      content: '←';
      margin-right: 0.5em;
    }
    h1 {
      color: #2c3e50;
      border-bottom: 2px solid #eee;
      padding-bottom: 0.5em;
    }
    h2 {
      color: #34495e;
      margin-top: 2em;
    }
    ul {
      list-style: none;
      padding: 0;
    }
    li {
      margin: 0.5em 0;
    }
    li a {
      display: inline-block;
      padding: 0.5em;
      color: #2980b9;
      text-decoration: none;
      border-radius: 4px;
      transition: background-color 0.2s;
    }
    li a:hover {
      background-color: #f7f9fb;
    }
  </style>
</head>
<body>
  <a class="back-link" href="../">Back to parent directory</a>
  <h1>$title</h1>
EOF

  # Add directories
  if [ ${#items[@]} -gt 0 ]; then
    echo "<h2>Quality Check Reports</h2><ul>" >> "$index_file"
    for item in "${items[@]}"; do
      echo "<li><a href=\"$item/\">$item</a></li>" >> "$index_file"
    done
    echo "</ul>" >> "$index_file"
  fi

  # Add files
  if [ ${#files[@]} -gt 0 ]; then
    echo "<h2>Reports</h2><ul>" >> "$index_file"
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

  # Create indexes for quality report directories only
  if [ -d "$reports_dir/quality" ]; then
    create_index "$reports_dir/quality" "Quality Reports"

    for dir in $reports_dir/quality/*/; do
      if [ -d "$dir" ]; then
        create_index "$dir" "$(basename "$dir") Report"
      fi
    done
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
