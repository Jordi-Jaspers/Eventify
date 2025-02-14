#!/bin/bash
cd client
bun install --save-text-lockfile
git add bun.lock
cd -
