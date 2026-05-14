import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import {form ,  FormField, submit } from '@angular/forms/signals';
import { Router } from '@angular/router'; 
import { environment } from '../../../environments/environment';
console.log('ENV API URL:', environment.apiUrl);
@Component({
  selector: 'app-homepage',
  imports: [FormField],
  templateUrl: './homepage.html',
  styleUrl: './homepage.css',
})
export class Homepage {

  private http  = inject(HttpClient) ;
  private router  = inject(Router) ;
  name = signal('');


  loginModel = signal({
    name : '',
  }) ;

  loginForm = form(this.loginModel)

  onSubmit(event : Event) {
    event.preventDefault() ; 
    submit(this.loginForm , async() => {
      const credentials = this.loginModel() ;

      this.http
        .post(`${environment.apiUrl}/guest` ,
            credentials , 
            { responseType: 'text' ,   withCredentials: true})
        .subscribe( {
        next : (data: string) => {
          this.router.navigate(['/lobby'])
        } , 
        error : (err) => {
        },

      }) ;
    })
  }


}
