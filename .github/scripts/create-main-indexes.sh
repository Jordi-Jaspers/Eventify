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
  <a class="back-link" href="../">Back to all reports</a>
  <h1>Build Report - Run $run_id</h1>
  <h2>Available Reports</h2>
  <ul>
    <li><a href="quality/" class="report-link">Quality Reports</a></li>
    <li><a href="tests/" class="report-link">Test Results</a></li>
  </ul>
</div>
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

         h2 {
           color: var(--text-color);
           margin-top: calc(var(--spacing-unit) * 2.5);
           font-size: 1.25rem;
           font-weight: 600;
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

         .build-number {
           margin-left: auto;
           opacity: 0.8;
           font-size: 0.9em;
         }

         @media (max-width: 600px) {
           body {
             padding: var(--spacing-unit);
           }

           .container {
             padding: var(--spacing-unit);
           }

           .builds-grid {
             grid-template-columns: 1fr;
           }
         }
  </style>
</head>
<body>
<div class="container">
  <h1>Build Reports</h1>
  <h2>Latest Build</h2>
  <p class="build-item">
    <a href="$run_id/" class="build-link">Run $run_id</a>
  </p>
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
