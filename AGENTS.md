# Repository Guidelines

## Project Structure & Module Organization
- `flowOA-server/`: Spring Boot 3 backend (Java 17). Main code is in `src/main/java/com/flowoa` with layered packages: `controller`, `service`, `mapper`, `entity`, `dto`, `vo`, `config`, `common`.
- `flowOA-server/src/main/resources`: runtime config and DB scripts (`application.yml`, `db/schema.sql`, `db/data.sql`).
- `flowOA-web/`: Vue 3 + Vite frontend. Main code is in `src/`, split into `views/`, `api/`, `stores/`, `router/`, `layout/`, and `utils/`.
- Keep features aligned across modules: backend endpoint/service/mapper plus corresponding frontend API and view/store updates.

## Build, Test, and Development Commands
- Backend run: `cd flowOA-server && mvn spring-boot:run` (starts API on `http://localhost:8080`).
- Backend build: `cd flowOA-server && mvn clean package` (creates deployable jar).
- Backend tests: `cd flowOA-server && mvn test` (runs JUnit/Spring tests).
- Frontend install: `cd flowOA-web && npm install`.
- Frontend dev: `cd flowOA-web && npm run dev` (Vite at `http://localhost:5173`, `/api` proxied to backend).
- Frontend build/preview: `cd flowOA-web && npm run build` then `npm run preview`.

## Coding Style & Naming Conventions
- Java: 4-space indentation, `PascalCase` class names, `camelCase` members/methods, one public class per file.
- Vue/JS: 2-space indentation; SFCs in `PascalCase` (for example, `LoginView.vue`); utility/API/store files in concise lowercase/camel style (for example, `request.js`, `user.js`).
- Frontend imports should prefer the `@` alias for `src`.
- No formatter/linter config is committed; follow surrounding style and keep diffs minimal.

## Testing Guidelines
- Backend tests belong in `flowOA-server/src/test/java` and should be named `*Test.java`.
- Prioritize service logic, controller response codes, and auth/permission paths.
- Frontend has no committed automated test framework yet; manually verify critical flows (login, apply, approve/reject) before opening a PR.

## Commit & Pull Request Guidelines
- Current history uses short, direct commit subjects (for example, `Initial commit`, `first successful run`).
- Prefer clear scoped messages: `<module>: <action summary>` (for example, `server: add leave approval validation`).
- PRs should include: summary, affected module(s), config/SQL impact, verification evidence (test output or UI screenshots), and linked issue/task.

## Security & Configuration Tips
- Do not commit real credentials. Use environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) to override defaults.
- When schema changes are introduced, update `db/schema.sql` and `db/data.sql` together to keep local bootstrap consistent.