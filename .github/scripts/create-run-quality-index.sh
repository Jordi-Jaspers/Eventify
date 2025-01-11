#!/bin/bash

create_quality_index() {
  local path=$1  # Receives "branch/run_id"
  local quality_path="$path/quality"

  # Check if quality directory exists
  if [ ! -d "$quality_path" ]; then
    echo "Quality directory not found at: $quality_path"
    return 1
  fi

  # Find all report directories in the quality folder
  local reports=$(find "$quality_path" -mindepth 1 -maxdepth 1 -type d -exec basename {} \; | sort)

  # Create report links HTML and rename HTML files
  local report_links=""
  for report in $reports; do
    local report_dir="$quality_path/$report"

    # Find first HTML file and rename it to index.html
    local first_html=$(find "$report_dir" -maxdepth 1 -name "*.html" | head -n 1)
    if [ ! -z "$first_html" ] && [ ! -f "$report_dir/index.html" ]; then
      mv "$first_html" "$report_dir/index.html"
    fi

    report_links+="    <p class=\"report-item\">
      <a href=\"$report/\" class=\"report-link\">$report</a>
    </p>
"
  done

  # Create quality index with all report types
  cat > "$quality_path/index.html" << EOF
<!DOCTYPE html>
<html>
<head>
  <title>Quality Reports</title>
  <style>
    :root {
      --primary-color: #3498db;
      --primary-dark: #2980b9;
      --text-color: #2c3e50;
      --background-hover: #f7f9fb;
      --spacing-unit: 1rem;
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

    .reports-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: var(--spacing-unit);
      margin-top: calc(var(--spacing-unit) * 1.5);
    }

    .report-item {
      margin: 0;
    }

    .report-link {
      display: flex;
      align-items: center;
      padding: 1em 1.5em;
      background-color: var(--primary-color);
      color: white;
      text-decoration: none;
      border-radius: 6px;
      transition: all 0.2s ease;
      font-weight: 500;
      position: relative;
      overflow: hidden;
      text-transform: capitalize;
    }

    .report-link:before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      height: 100%;
      width: 4px;
      background-color: rgba(255, 255, 255, 0.2);
    }

    .report-link:hover {
      background-color: var(--primary-dark);
      transform: translateY(-2px);
      box-shadow: 0 4px 6px rgba(52, 152, 219, 0.2);
    }
  </style>
</head>
<body>
<div class="container">
  <a class="back-link" href="../">Back to run summary</a>
  <h1>Quality Reports</h1>
  <div class="reports-grid">
${report_links}
  </div>
</div>
</body>
</html>
EOF
}

# Execute if run directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <branch/run_id>"
    exit 1
  fi
  create_quality_index "$1"
fi
