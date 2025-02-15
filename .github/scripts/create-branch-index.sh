#!/bin/bash

create_branch_index() {
  local path=$1  # Receives "branch/run_id"
  local branch_name=$(dirname "$path")

  # Find all run directories for this branch
  local runs=$(find "$branch_name" -mindepth 1 -maxdepth 1 -type d -exec basename {} \; | sort -r)

  # Create run links HTML
  local run_links=""
  for run in $runs; do
    run_links+="    <p class=\"build-item\">
      <a href=\"$run/\" class=\"build-link\">Run $run</a>
    </p>
"
  done

  # Create branch index with all runs
  cat > "$branch_name/index.html" << EOF
<!DOCTYPE html>
<html>
<head>
  <title>Build Reports - $branch_name branch</title>
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

    .builds-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: var(--spacing-unit);
      margin-top: calc(var(--spacing-unit) * 1.5);
    }

    .build-item {
      margin: 0;
    }

    .build-link {
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
    }

    .build-link:before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      height: 100%;
      width: 4px;
      background-color: rgba(255, 255, 255, 0.2);
    }

    .build-link:hover {
      background-color: var(--primary-dark);
      transform: translateY(-2px);
      box-shadow: 0 4px 6px rgba(52, 152, 219, 0.2);
    }
  </style>
</head>
<body>
<div class="container">
  <a class="back-link" href="../">Back to all branches</a>
  <h1>Build Reports - $branch_name branch</h1>
  <div class="builds-grid">
${run_links}
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
  create_branch_index "$1"
fi
