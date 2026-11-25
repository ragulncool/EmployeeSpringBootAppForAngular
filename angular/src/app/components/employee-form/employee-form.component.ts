import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Employee } from '../../models/employee';

@Component({
  selector: 'app-employee-form',
  imports: [FormsModule],
  templateUrl: './employee-form.component.html',
  styleUrl: './employee-form.component.css',
})
export class EmployeeFormComponent implements OnChanges {
  @Input() employee: Employee | null = null;
  @Input() saving = false;
  @Output() submitted = new EventEmitter<Employee>();
  @Output() cancelled = new EventEmitter<void>();

  protected form: Employee = this.emptyEmployee();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['employee']) {
      this.form = this.employee ? { ...this.employee } : this.emptyEmployee();
    }
  }

  protected submitForm(): void {
    this.submitted.emit({
      emp_id: Number(this.form.emp_id),
      emp_name: this.form.emp_name.trim(),
      emp_dept: this.form.emp_dept.trim(),
    });
  }

  private emptyEmployee(): Employee {
    return { emp_id: 0, emp_name: '', emp_dept: '' };
  }
}
