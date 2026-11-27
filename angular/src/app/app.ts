import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { EmployeeFormComponent } from './components/employee-form/employee-form.component';
import { EmployeeListComponent } from './components/employee-list/employee-list.component';
import { Employee } from './models/employee';
import { AuthService, Credentials } from './services/auth.service';
import { EmployeePage, EmployeeQuery, EmployeeService } from './services/employee.service';

@Component({
  imports: [FormsModule, EmployeeFormComponent, EmployeeListComponent],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly employeeService = inject(EmployeeService);

  protected readonly isAuthenticated = this.authService.isAuthenticated;
  protected readonly authMode = signal<'login' | 'signup'>('login');
  protected readonly authBusy = signal(false);
  protected readonly authError = signal('');
  protected credentials: Credentials = { username: '', password: '' };
  protected readonly employees = signal<Employee[]>([]);
  protected readonly page = signal(0);
  protected readonly size = signal(10);
  protected readonly totalElements = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly sortBy = signal<EmployeeQuery['sortBy']>('emp_id');
  protected readonly direction = signal<EmployeeQuery['direction']>('asc');
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly deletingId = signal<number | null>(null);
  protected readonly error = signal('');
  protected readonly selectedEmployee = signal<Employee | null>(null);

  ngOnInit(): void {
    if (this.isAuthenticated()) {
      this.loadEmployees();
    }
  }

  protected submitAuth(): void {
    this.authBusy.set(true);
    this.authError.set('');
    const request = this.authMode() === 'signup'
      ? this.authService.signup(this.credentials)
      : this.authService.login(this.credentials);

    request.subscribe({
      next: () => {
        this.authBusy.set(false);
        this.loadEmployees();
      },
      error: (error: HttpErrorResponse) => {
        this.authError.set(
          error.status === 401
            ? 'Invalid username or password.'
            : error.status === 409
              ? 'That username is already registered.'
              : error.status === 400
                ? 'Choose a username (3-64 letters, numbers, dots, underscores, or hyphens) and a password of at least 8 characters.'
                : 'Could not connect to the authentication service. Try again.',
        );
        this.authBusy.set(false);
      },
    });
  }

  protected switchAuthMode(): void {
    this.authMode.update((mode) => mode === 'login' ? 'signup' : 'login');
    this.authError.set('');
  }

  protected logout(): void {
    this.authService.logout().subscribe({
      error: () => this.authError.set('You have been signed out, but the server could not revoke the token.'),
    });
  }

  protected loadEmployees(): void {
    this.loading.set(true);
    this.error.set('');
    this.employeeService.getAll({
      page: this.page(),
      size: this.size(),
      sortBy: this.sortBy(),
      direction: this.direction(),
    }).subscribe({
      next: (result: EmployeePage) => {
        const lastPage = Math.max(result.totalPages - 1, 0);
        if (this.page() > lastPage) {
          this.page.set(lastPage);
          this.loadEmployees();
          return;
        }
        this.employees.set(result.content);
        this.totalElements.set(result.totalElements);
        this.totalPages.set(result.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load employees. Check that the Spring Boot API is running on port 8081.');
        this.loading.set(false);
      },
    });
  }

  protected changePage(page: number): void {
    this.page.set(page);
    this.loadEmployees();
  }

  protected changePageSize(size: number): void {
    this.size.set(size);
    this.page.set(0);
    this.loadEmployees();
  }

  protected changeSort(sort: { sortBy: EmployeeQuery['sortBy']; direction: EmployeeQuery['direction'] }): void {
    this.sortBy.set(sort.sortBy);
    this.direction.set(sort.direction);
    this.page.set(0);
    this.loadEmployees();
  }

  protected saveEmployee(employee: Employee): void {
    if (!employee.emp_id || !employee.emp_name || !employee.emp_dept) {
      this.error.set('Enter a valid ID, name, and department.');
      return;
    }

    this.saving.set(true);
    this.error.set('');
    const request = this.selectedEmployee()
      ? this.employeeService.update(employee.emp_id, employee)
      : this.employeeService.create(employee);

    request.subscribe({
      next: () => {
        this.selectedEmployee.set(null);
        this.saving.set(false);
        this.loadEmployees();
      },
      error: (error: { status?: number }) => {
        this.error.set(
          error.status === 404
            ? 'That employee no longer exists. Refresh the directory and try again.'
            : error.status === 409
              ? 'An employee with that ID already exists.'
              : 'Could not save employee. Check the ID and try again.',
        );
        this.saving.set(false);
      },
    });
  }

  protected editEmployee(employee: Employee): void {
    this.selectedEmployee.set({ ...employee });
    this.error.set('');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  protected cancelEdit(): void {
    this.selectedEmployee.set(null);
    this.error.set('');
  }

  protected deleteEmployee(employee: Employee): void {
    if (!window.confirm(`Delete ${employee.emp_name}? This cannot be undone.`)) {
      return;
    }

    this.deletingId.set(employee.emp_id);
    this.error.set('');
    this.employeeService.delete(employee.emp_id).subscribe({
      next: () => {
        if (this.selectedEmployee()?.emp_id === employee.emp_id) {
          this.cancelEdit();
        }
        this.deletingId.set(null);
        this.loadEmployees();
      },
      error: () => {
        this.error.set('Could not delete employee. Refresh the directory and try again.');
        this.deletingId.set(null);
      },
    });
  }
}
