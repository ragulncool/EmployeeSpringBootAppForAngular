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

- `GET /api/employees` and `GET /api/employees/{empId}`
- `POST /api/employees`
- `PUT /api/employees/{empId}`
- `DELETE /api/employees/{empId}`

Employee JSON fields are `emp_id`, `emp_name`, and `emp_dept`.
