# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FlowOA is an office approval system built with a Spring Boot backend and Vue 3 frontend, using the warm-flow workflow engine for approval processes.

## Tech Stack

- **Backend**: Spring Boot 3.2 + MyBatis-Plus + MySQL + Sa-Token + warm-flow
- **Frontend**: Vue 3 + Element Plus + Vite + Pinia + Vue Router
- **Database**: MySQL 8.0

## Project Structure

```
flowOA/
├── flowOA-server/          # Backend (Spring Boot)
│   └── src/main/java/com/flowoa/
│       ├── config/         # Configuration (SaToken, MyBatis-Plus, CORS, etc.)
│       ├── common/         # Result, PageResult, Constants
│       ├── entity/         # JPA entities (SysUser, SysDept, SysRole, LeaveApply, ExpenseApply, GenericApply)
│       ├── mapper/         # MyBatis-Plus mappers
│       ├── service/        # Business logic (FlowService wraps warm-flow)
│       ├── controller/     # REST controllers
│       ├── dto/            # Request DTOs (ApproveDTO, TransferDTO)
│       ├── vo/             # View objects (LoginVO)
│       └── interceptor/    # Sa-Token interceptor
├── flowOA-web/             # Frontend (Vue 3 + Vite)
│   └── src/
│       ├── api/            # Axios API modules
│       ├── stores/         # Pinia stores (user, app)
│       ├── router/         # Vue Router config
│       ├── layout/         # MainLayout, Sidebar, Header
│       └── views/          # Page components
└── CLAUDE.md
```

## Key Patterns

- **API Base**: Frontend proxies `/api` → `http://localhost:8080`
- **Auth**: Sa-Token with `Authorization` header, login at `/auth/login`
- **Flow Integration**: FlowService wraps warm-flow API; business services call `startProcess()`, `approve()`, `reject()`
- **Default login**: admin / admin123

## Running

1. Create MySQL database `flowoa` and run `flowOA-server/src/main/resources/db/schema.sql` and `data.sql`
2. Backend: `cd flowOA-server && mvn spring-boot:run`
3. Frontend: `cd flowOA-web && npm install && npm run dev`

## Database

- System tables: `sys_user`, `sys_dept`, `sys_role`
- Business tables: `leave_apply`, `expense_apply`, `generic_apply`
- Flow tables: auto-created by warm-flow (prefix `flow_`)
