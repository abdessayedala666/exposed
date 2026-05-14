import { Routes } from '@angular/router';
import { Homepage } from './pages/homepage/homepage';
import { Room } from './pages/room/room';
import { Lobby } from './pages/lobby/lobby';
export const routes: Routes = [
    {
        path : '' , 
        component : Homepage
    },
    {
        path : 'room' , 
        component : Room
    },
    {path : 'lobby' , 
        component : Lobby
    } ,
    {path : 'room/:roomId' , 
        component : Room
    }
];
