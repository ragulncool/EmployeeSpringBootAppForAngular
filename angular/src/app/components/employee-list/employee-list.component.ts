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
  @Output() refresh = new EventEmitter<void>();
  @Output() edit = new EventEmitter<Employee>();
  @Output() delete = new EventEmitter<Employee>();

  protected initials(name: string): string {
    return name
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0].toUpperCase())
      .join('');
  }
}
