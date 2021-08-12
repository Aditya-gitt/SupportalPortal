import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { User } from 'src/app/Model/User';
import { AuthenticationService } from 'src/app/service/authentication.service';
import { NotificationService } from 'src/app/service/notification.service';
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit, OnDestroy {
  private subscription: Subscription[] = [];
  public showLoading: boolean = false;


  constructor(private router: Router, private authenticationService: AuthenticationService, public notificationService: NotificationService) { }

  ngOnInit(): void {
    if(this.authenticationService.isUserLoggedIn()) {
      this.router.navigateByUrl('/user/management');
    }
  }

  ngOnDestroy(): void {
    this.subscription.forEach(sub => {sub.unsubscribe()});
  }

  public onLogin(user: User) : void {
    this.showLoading = true;
    console.log(user);
    this.subscription.push(
      this.authenticationService.login(user).subscribe(
        response => {
          const token = response.headers.get('Jwt-Token');
          if(token!=null) {
            this.authenticationService.saveToken(token);
          }
          if(response.body!=null) {
            this.authenticationService.addUserToLocalCache(response.body);
          }
          this.notificationService.addToast('You have been logged in successfully', 'success'); 
          this.router.navigateByUrl('/user/management');
        },
        (error: HttpErrorResponse) => {
          if(error.error.message){
            this.notificationService.addToast(error.error.message, 'danger');
            this.showLoading = false;
          }
          else {
            this.notificationService.addToast('An error occured, try again!', 'danger');
            this.showLoading = false;
          }
        }
      )
    );
   }

}
