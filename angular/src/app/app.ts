import { Component, OnInit, inject, signal } from '@angular/core';
import { EmployeeFormComponent } from './components/employee-form/employee-form.component';
import { EmployeeListComponent } from './components/employee-list/employee-list.component';
import { Employee } from './models/employee';
import { EmployeeService } from './services/employee.service';

@Component({
  imports: [EmployeeFormComponent, EmployeeListComponent],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App implements OnInit {
  private readonly employeeService = inject(EmployeeService);

  protected readonly employees = signal<Employee[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly deletingId = signal<number | null>(null);
  protected readonly error = signal('');
  protected readonly selectedEmployee = signal<Employee | null>(null);

  ngOnInit(): void {
    this.loadEmployees();
  }

  protected loadEmployees(): void {
    this.loading.set(true);
    this.error.set('');
    this.employeeService.getAll().subscribe({
      next: (employees) => {
        this.employees.set(employees);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load employees. Check that the Spring Boot API is running on port 8081.');
        this.loading.set(false);
      },
    });
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
