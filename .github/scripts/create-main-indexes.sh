#!/bin/bash

# .github/scripts/create-main-indexes.sh
create_main_indexes() {
  local run_id=$1
  local reports_dir="reports"

  # Create build-specific index
  cat > "$reports_dir/$run_id/index.html" << EOF
<!DOCTYPE html>
<html>
<head>
  <title>Build Report - Run $run_id</title>
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
    .report-link {
      display: inline-block;
      padding: 0.7em 1.2em;
      background-color: #3498db;
      color: white;
      text-decoration: none;
      border-radius: 4px;
      transition: background-color 0.2s;
    }
    .report-link:hover {
      background-color: #2980b9;
    }
  </style>
</head>
<body>
  <a class="back-link" href="../">Back to all reports</a>
  <h1>Build Report - Run $run_id</h1>
  <h2>Available Reports</h2>
  <ul>
    <li><a href="quality/" class="report-link">Quality Reports</a></li>
    <li><a href="tests/" class="report-link">Test Results</a></li>
  </ul>
</body>
</html>
EOF

  # Create main index
  cat > "$reports_dir/index.html" << EOF
<!DOCTYPE html>
<html>
<head>
  <title>Build Reports</title>
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
    h1 {
      color: #2c3e50;
      border-bottom: 2px solid #eee;
      padding-bottom: 0.5em;
    }
    h2 {
      color: #34495e;
      margin-top: 2em;
    }
    .build-link {
      display: inline-block;
      padding: 0.7em 1.2em;
      background-color: #3498db;
      color: white;
      text-decoration: none;
      border-radius: 4px;
      transition: background-color 0.2s;
    }
    .build-link:hover {
      background-color: #2980b9;
    }
  </style>
</head>
<body>
  <h1>Build Reports</h1>
  <h2>Latest Build</h2>
  <p><a href="$run_id/" class="build-link">Run $run_id</a></p>
</body>
</html>
EOF
}

# Execute if run directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <run_id>"
    exit 1
  fi
  create_main_indexes "$1"
fi
