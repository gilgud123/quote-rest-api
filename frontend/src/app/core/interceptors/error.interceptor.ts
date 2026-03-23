import {HttpInterceptorFn} from '@angular/common/http';
import {catchError, throwError} from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error) => {
      let errorMessage = 'An error occurred';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Error: ${error.error.message}`;
      } else {
        // Server-side error
        switch (error.status) {
          case 400:
            errorMessage = 'Bad Request: ' + (error.error?.message || 'Invalid data');
            break;
          case 401:
            errorMessage = 'Unauthorized: Please log in';
            break;
          case 403:
            errorMessage = 'Forbidden: You do not have permission';
            break;
          case 404:
            errorMessage = 'Not Found: ' + (error.error?.message || 'Resource not found');
            break;
          case 500:
            errorMessage = 'Server Error: ' + (error.error?.message || 'Internal server error');
            break;
          default:
            errorMessage = error.error?.message || `Error: ${error.status} - ${error.statusText}`;
        }
      }

      console.error('HTTP Error:', errorMessage, error);

      // TODO: Add user notification when Material components are set up
      // Example: snackBar.open(errorMessage, 'Close', { duration: 5000 });

      return throwError(() => error);
    })
  );
};
