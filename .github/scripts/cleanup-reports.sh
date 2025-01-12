#!/bin/bash

cleanup_reports() {
  cd reports || exit 1

  # Get list of active branches from the repository
  active_branches=$(git ls-remote --heads origin | awk '{print $2}' | sed 's/refs\/heads\///')
  echo "Active branches: $active_branches"

  # Process each directory in reports
  find . -mindepth 1 -maxdepth 1 -type d | while read branch_dir; do
    branch_name=$(basename "$branch_dir")

    if echo "$active_branches" | grep -q "^${branch_name}$"; then
      echo "Processing active branch: $branch_name"
      # For active branches, remove all run directories but keep branch structure
      find "$branch_dir" -mindepth 1 -maxdepth 1 -type d -exec rm -rf {} +
      # Recreate empty branch index
      cat > "$branch_dir/index.html" << EOF
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
  <a class="back-link" href="../">Back to all branches</a>
  <h1>Build Reports - $branch_name branch</h1>
  <div class="builds-grid">
  </div>
</div>
</body>
</html>
EOF
    else
      echo "Removing inactive branch directory: $branch_name"
      # For inactive branches (including pull requests), remove the entire directory
      rm -rf "$branch_dir"
    fi
  done

  # Recreate main index with remaining branches
  cat > index.html << EOF
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
    $(for branch in $(find . -mindepth 1 -maxdepth 1 -type d); do
      branch_name=$(basename "$branch")
      echo "    <p class=\"branch-item\"><a href=\"$branch_name/\" class=\"branch-link\">$branch_name</a></p>"
    done)
  </div>
</div>
</body>
</html>
EOF
}

# Execute if run directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  cleanup_reports
fi
