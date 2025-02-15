#!/bin/bash

# .github/scripts/create-run-index.sh
create_run_index() {
  local path=$1  # Receives "branch/run_id"
  local branch_name=$(dirname "$path")
  local run_id=$(basename "$path")

  # Create run-specific index
  cat > "$branch_name/$run_id/index.html" << EOF
<!DOCTYPE html>
<html>
<head>
  <title>Build Report - $branch_name/$run_id</title>
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

    h2 {
      color: var(--text-color);
      margin-top: calc(var(--spacing-unit) * 2.5);
      font-size: 1.25rem;
      font-weight: 600;
    }

    ul {
      list-style: none;
      padding: 0;
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: var(--spacing-unit);
      margin-top: calc(var(--spacing-unit) * 1.5);
    }

    li {
      margin: 0;
    }

    .report-link {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 1em 1.5em;
      background-color: var(--primary-color);
      color: white;
      text-decoration: none;
      border-radius: 6px;
      transition: all 0.2s ease;
      font-weight: 500;
      height: 100%;
      text-align: center;
    }

    .report-link:hover {
      background-color: var(--primary-dark);
      transform: translateY(-2px);
      box-shadow: 0 4px 6px rgba(52, 152, 219, 0.2);
    }

    @media (max-width: 600px) {
      body {
        padding: var(--spacing-unit);
      }

      .container {
        padding: var(--spacing-unit);
      }

      ul {
        grid-template-columns: 1fr;
      }
    }
  </style>
</head>
<body>
<div class="container">
  <a class="back-link" href="../">Back to branch reports</a>
  <h1>Build Report - $branch_name/$run_id</h1>
  <div class="report-grid">
    <h2>Available Reports</h2>
    <ul>
      <li><a href="quality/" class="report-link">Quality Report</a></li>
      <li><a href="tests/" class="report-link">Test Report</a></li>
    </ul>
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
  create_run_index "$1"
fi
