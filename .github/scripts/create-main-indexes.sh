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
    body { font-family: Arial, sans-serif; margin: 2em; }
    .back-link { margin-bottom: 2em; }
  </style>
</head>
<body>
  <div class="back-link"><a href="../">← Back to all reports</a></div>
  <h1>Build Report - Run $run_id</h1>
  <h2>Reports:</h2>
  <ul>
    <li><a href="quality/">Quality Reports</a></li>
    <li><a href="tests/">Test Reports</a></li>
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
    body { font-family: Arial, sans-serif; margin: 2em; }
  </style>
</head>
<body>
  <h1>Build Reports</h1>
  <h2>Latest Build:</h2>
  <p><a href="$run_id/">Run $run_id</a></p>
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
