import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { keycloak } from './app.config';

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  title = 'Quote REST API';
  isAuthenticated = false;
  username = '';

  ngOnInit(): void {
    this.isAuthenticated = keycloak?.authenticated ?? false;
    if (this.isAuthenticated) {
      this.username = keycloak?.tokenParsed?.['preferred_username'] || 'User';
    }
  }

  logout(): void {
    keycloak?.logout({ redirectUri: window.location.origin });
  }
}
