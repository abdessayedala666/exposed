import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-lobby',
  imports: [FormsModule],
  templateUrl: './lobby.html',
  styleUrl: './lobby.css',
})
export class Lobby {
  private http = inject(HttpClient);
  private router  = inject(Router) ;

  roomId = '';
  errorMessage = signal('') ;
  joinRoom(){
    this.http.get(`${environment.apiUrl}/room/${this.roomId}` ,{
      withCredentials : true ,
      responseType : 'text',
    }).subscribe({
      next : (exists) =>{
        if(exists == "true"){
          this.router.navigate(['room' , this.roomId]) ;
        }
        else{
          this.errorMessage.set("this room doesn't exist")
        }
      }
    })
  }
  createRoom(){
    this.http.post(`${environment.apiUrl}/room/create` ,
      {},
      {withCredentials : true , 
      responseType: 'text',
      },
     )
     .subscribe({
      next : (roomId) => {
        this.router.navigate(['/room' , roomId]) ;
      },
      error : (error) => {
        console.error(error)
      }
     }
     )
    
  }
}
