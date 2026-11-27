import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Employee } from '../../models/employee';

@Component({
  selector: 'app-employee-list',
  templateUrl: './employee-list.component.html',
  styleUrl: './employee-list.component.css',
})
export class EmployeeListComponent {
  @Input() employees: Employee[] = [];
  @Input() loading = false;
  @Input() deletingId: number | null = null;
  @Input() page = 0;
  @Input() size = 10;
  @Input() totalElements = 0;
  @Input() totalPages = 0;
  @Input() sortBy: 'emp_id' | 'emp_name' | 'emp_dept' = 'emp_id';
  @Input() direction: 'asc' | 'desc' = 'asc';
  @Output() refresh = new EventEmitter<void>();
  @Output() edit = new EventEmitter<Employee>();
  @Output() delete = new EventEmitter<Employee>();
  @Output() pageChange = new EventEmitter<number>();
  @Output() sizeChange = new EventEmitter<number>();
  @Output() sortChange = new EventEmitter<{ sortBy: 'emp_id' | 'emp_name' | 'emp_dept'; direction: 'asc' | 'desc' }>();

  protected sort(column: 'emp_id' | 'emp_name' | 'emp_dept'): void {
    this.sortChange.emit({
      sortBy: column,
      direction: this.sortBy === column && this.direction === 'asc' ? 'desc' : 'asc',
    });
  }

  protected changeSortBy(event: Event): void {
    const select = event.target;
    if (select instanceof HTMLSelectElement) {
      const sortBy = select.value;
      if (sortBy === 'emp_id' || sortBy === 'emp_name' || sortBy === 'emp_dept') {
        this.sortChange.emit({ sortBy, direction: this.direction });
      }
    }
  }

  protected changeDirection(event: Event): void {
    const select = event.target;
    if (select instanceof HTMLSelectElement && (select.value === 'asc' || select.value === 'desc')) {
      this.sortChange.emit({ sortBy: this.sortBy, direction: select.value });
    }
  }

  protected changeSize(event: Event): void {
    const select = event.target;
    if (select instanceof HTMLSelectElement) {
      const size = Number(select.value);
      if ([10, 20, 30, 50, 100].includes(size)) {
        this.sizeChange.emit(size);
      }
    }
  }

  protected get firstVisible(): number {
    return this.totalElements === 0 ? 0 : this.page * this.size + 1;
  }

  protected get lastVisible(): number {
    return Math.min((this.page + 1) * this.size, this.totalElements);
  }

  protected initials(name: string): string {
    return name
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0].toUpperCase())
      .join('');
  }
}
