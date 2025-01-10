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
      :root {
        --primary-color: #3498db;
        --primary-dark: #2980b9;
        --text-color: #2c3e50;
        --background-hover: #f7f9fb;
        --spacing-unit: 1rem;
        --link-color: #2980b9;
      }

      body {
        font-family: system-ui, -apple-system, sans-serif;
        margin: 0;
        padding: calc(var(--spacing-unit) * 3);
        line-height: 1.6;
        color: var(--text-color);
        max-width: 900px;
        margin: 0 auto;
        background-color: #fafafa;
      }

      .container {
        background-color: white;
        padding: calc(var(--spacing-unit) * 2);
        border-radius: 8px;
        box-shadow: 0 2px 4px rgba(0,0,0,0.05);
      }

      .back-link {
        margin-bottom: calc(var(--spacing-unit) * 2);
        display: inline-flex;
        align-items: center;
        padding: 0.75em 1.25em;
        color: var(--text-color);
        text-decoration: none;
        border-radius: 6px;
        transition: all 0.2s ease;
        font-weight: 500;
        background-color: white;
        box-shadow: 0 1px 2px rgba(0,0,0,0.05);
      }

      .back-link:hover {
        background-color: var(--background-hover);
        transform: translateY(-1px);
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      }

      .back-link:before {
        content: '←';
        margin-right: 0.75em;
        font-size: 1.1em;
      }

      h1 {
        color: var(--text-color);
        border-bottom: 2px solid #eef2f7;
        padding-bottom: 0.75em;
        margin-top: 0;
        font-size: 1.75rem;
      }

      h2 {
        color: var(--text-color);
        margin-top: calc(var(--spacing-unit) * 2.5);
        font-size: 1.25rem;
        font-weight: 600;
      }

      .report-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: calc(var(--spacing-unit) * 1.5);
        margin-top: calc(var(--spacing-unit) * 1.5);
      }

      .report-item {
        margin: 0;
        background-color: var(--background-hover);
        border-radius: 6px;
        transition: all 0.2s ease;
      }

      .report-item:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
      }

      .report-link {
        display: flex;
        align-items: center;
        padding: 1em 1.25em;
        color: var(--link-color);
        text-decoration: none;
        font-weight: 500;
        width: 100%;
        height: 100%;
        box-sizing: border-box;
        position: relative;
      }

      .report-link:after {
        content: '→';
        margin-left: auto;
        opacity: 0;
        transition: all 0.2s ease;
      }

      .report-item:hover .report-link:after {
        opacity: 1;
        transform: translateX(4px);
      }

      @media (max-width: 600px) {
        body {
          padding: var(--spacing-unit);
        }

        .container {
          padding: var(--spacing-unit);
        }

        .report-grid {
          grid-template-columns: 1fr;
        }
      }
    </style>
</head>
<body>
  <div class="container">
      <a class="back-link" href="../">Back to parent directory</a
      <h1>$title</h1>
EOF

  # Add directories
  if [ ${#items[@]} -gt 0 ]; then
    echo "<h2>Quality Check Reports</h2>" >> "$index_file"
    echo "<div class=\"report-grid\">" >> "$index_file"
    for item in "${items[@]}"; do
      echo "<div class=\"report-item\">" >> "$index_file"
      echo "<a class=\"report-link\" href=\"$item/\">$item</a>" >> "$index_file"
      echo "</div>" >> "$index_file"
    done
    echo "</div>" >> "$index_file"
  fi

  echo "</div>" >> "$index_file"
  echo "</body>" >> "$index_file"
  echo "</html>" >> "$index_file"
}

# Create all necessary indexes
create_all_indexes() {
  local run_id=$1
  local reports_dir="reports/$run_id"

  # Create reports directory if it doesn't exist
  mkdir -p "$reports_dir"

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
