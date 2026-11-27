# Employee Directory UI

Standalone Angular frontend for the Spring Boot employee CRUD API. The frontend source and dependencies live in this folder and do not alter the Maven project.

## Run locally

1. Start Spring Boot from the repository root on port 8081 with `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`.
2. From this folder, install dependencies and build once:

   ```sh
   npm install
   npm run build
   ```

3. Start the frontend with `npm start` and open <http://localhost:4200>.

The local server serves the Angular build and proxies `/api/*` requests to `http://localhost:8081`. Re-run `npm run build` after changing Angular source.

## Source structure

- `src/app/components/employee-form/` contains the create/edit form.
- `src/app/components/employee-list/` contains the employee table and row actions.
- `src/app/services/employee.service.ts` owns all HTTP calls.
- `src/app/models/employee.ts` defines the API data shape.
- `src/app/app.ts` coordinates loading and CRUD state between the components.

## Employee API

The UI lists, creates, edits, and deletes employees using:

- `GET /api/employees?page=0&size=10&sortBy=emp_id&direction=asc`
- `GET /api/employees/{empId}`
- `POST /api/employees`
- `PUT /api/employees/{empId}`
- `DELETE /api/employees/{empId}`

The employee list endpoint returns a page object containing `content`, `page`, `size`, `totalElements`, `totalPages`, `sortBy`, and `direction`. Page numbers are zero-based, page size is limited to 1–100, and sorting supports `emp_id`, `emp_name`, or `emp_dept` with `asc` or `desc` direction. The UI sends all four query parameters and lets users change page, page size, and sorting.

Employee JSON fields are `emp_id`, `emp_name`, and `emp_dept`.

The database initializer adds 35 demo employees (IDs 90001–90035) only when those IDs are not already present, so the UI has multiple pages to explore.

## Authentication

- `POST /api/auth/signup` creates an account and returns an access token.
- `POST /api/auth/login` authenticates an account and returns an access token.
- `POST /api/auth/logout` revokes the current token.

The Angular app stores the token in browser local storage and sends it as an `Authorization: Bearer ...` header with API requests. Tokens expire after 24 hours. Employee endpoints require a valid token; passwords are stored as BCrypt hashes.

## AWS deployment

The employee UI is built into the Spring Boot JAR and served from the same origin as the API. Elastic Beanstalk runs the JAR on port 5000; its pre-deploy hook creates `/var/app/data` for the H2 database configured by `DB_FILE`. This keeps H2 data across application updates on the current instance, but it is not backed up and can be lost if the instance is replaced.
