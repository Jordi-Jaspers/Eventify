#!/bin/bash

create_main_index() {
  local path=$1  # Receives "branch/run_id"

  # Find all branches (directories that contain runs)
  local branches=$(find . -mindepth 1 -maxdepth 1 -type d -not -name ".*" -exec basename {} \;)

  # Create branch links HTML
  local branch_links=""
  for branch in $branches; do
    branch_links+="    <p class=\"branch-item\">
      <a href=\"$branch/\" class=\"branch-link\">$branch</a>
    </p>
"
  done

  # Create or update main index with all branches
  cat > "index.html" << EOF
<!DOCTYPE html>
<html>
<head>
  <title>Build Reports - All Branches</title>
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

    h1 {
      color: var(--text-color);
      border-bottom: 2px solid #eef2f7;
      padding-bottom: 0.75em;
      margin-top: 0;
      font-size: 1.75rem;
    }

    .branches-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: var(--spacing-unit);
      margin-top: calc(var(--spacing-unit) * 1.5);
    }

    .branch-item {
      margin: 0;
    }

    .branch-link {
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

    .branch-link:before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      height: 100%;
      width: 4px;
      background-color: rgba(255, 255, 255, 0.2);
    }

    .branch-link:hover {
      background-color: var(--primary-dark);
      transform: translateY(-2px);
      box-shadow: 0 4px 6px rgba(52, 152, 219, 0.2);
    }
  </style>
</head>
<body>
<div class="container">
  <h1>Build Reports - All Branches</h1>
  <div class="branches-grid">
    <p class="branch-item">
      <a href="$branch_name/" class="branch-link">$branch_name</a>
    </p>
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
  create_main_index "$1"
fi
