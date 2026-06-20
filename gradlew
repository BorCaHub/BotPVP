#!/bin/sh
#
# Copyright © 2015-2021 the original authors.
#
# Gradle wrapper script for BotPvP mod.
# Run: ./gradlew build
#
# CATATAN: file gradle-wrapper.jar tidak disertakan di paket ini, jadi
# script ini akan memakai Gradle yang sudah ter-install di sistem kamu.
# Kalau belum ada, install dulu lewat https://gradle.org/install/
# (atau pakai SDKMAN: https://sdkman.io -> "sdk install gradle 8.10")

##############################################################################
# Default: use system Gradle
##############################################################################

GRADLE_OPTS="${GRADLE_OPTS:-"-Dfile.encoding=UTF-8"}"

if ! command -v gradle >/dev/null 2>&1; then
  echo "ERROR: 'gradle' tidak ditemukan di PATH." >&2
  echo "Install Gradle 8.10 terlebih dahulu:" >&2
  echo "  - https://gradle.org/install/" >&2
  echo "  - atau via SDKMAN: sdk install gradle 8.10" >&2
  exit 1
fi

exec gradle "$@"
