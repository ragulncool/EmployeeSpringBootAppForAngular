import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Employee } from '../models/employee';

export interface EmployeePage {
  content: Employee[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  sortBy: string;
  direction: 'asc' | 'desc';
}

export interface EmployeeQuery {
  page: number;
  size: number;
  sortBy: 'emp_id' | 'emp_name' | 'emp_dept';
  direction: 'asc' | 'desc';
}

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/employees';

  getAll(query: EmployeeQuery): Observable<EmployeePage> {
    return this.http.get<EmployeePage>(this.baseUrl, {
      params: {
        page: query.page,
        size: query.size,
        sortBy: query.sortBy,
        direction: query.direction,
      },
    });
  }

  create(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(this.baseUrl, employee);
  }

  update(id: number, employee: Employee): Observable<Employee> {
    return this.http.put<Employee>(`${this.baseUrl}/${id}`, employee);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
