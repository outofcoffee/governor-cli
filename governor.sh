#!/usr/bin/env bash
set -e

# Convenience script that wraps the Docker image build, then
# starts a container passing all arguments to this script.
#
# Prerequisites:
# - JDK 8 or newer
# - Docker
# - your configuration files in the examples directory
#
# Usage:
#     ./governor.sh [args]
#
# See README.md for list of valid arguments.
#

./gradlew dockerBuildImage --quiet

exec docker run --rm -it -v $PWD/examples:/app/examples outofcoffee/guvernor:latest "$@"
