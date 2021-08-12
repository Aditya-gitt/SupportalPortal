import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './MyComponents/login/login.component';
import { RegisterComponent } from './MyComponents/register/register.component';
import { UserComponent } from './MyComponents/user/user.component';

const routes: Routes = [{path: 'login', component: LoginComponent},
                        {path: 'register', component: RegisterComponent},
                        {path: 'user/management', component: UserComponent},
                        {path: '', redirectTo: '/login', pathMatch: 'full'}];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule] 
})
export class AppRoutingModule { }
