import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-unauthorized',
  imports: [CommonModule],
  templateUrl: './unauthorized.component.html',
  styleUrl: './unauthorized.component.scss',
  standalone: true,
})
export class UnauthorizedComponent {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  login(): void {
    this.authService.login();
  }

  goBack(): void {
    this.router.navigate(['/']);
  }
}
