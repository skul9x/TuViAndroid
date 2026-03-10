# Plan: Fix Empty Star Name in Prompt
Created: 260310-1510
Status: 🟡 In Progress

## Overview
Lá số ở cung Dậu bị lọt một thẻ Sub_star rỗng `"name": ""`. Cần sửa logic `GeminiClient.kt` lọc chuỗi rỗng trước khi push vào JSON.

## Tech Stack
- Frontend: N/A
- Backend: Kotlin
- Database: N/A

## Phases

| Phase | Name | Status | Progress |
|-------|------|--------|----------|
| 01 | Code Fix & Test | ✅ Complete | 0% |
