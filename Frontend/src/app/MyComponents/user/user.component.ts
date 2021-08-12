import { HttpErrorResponse, HttpEvent, HttpEventType } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { BehaviorSubject } from 'rxjs/internal/BehaviorSubject';
import { Role } from 'src/app/enum/role.enum';
import { CustomHttpRespone } from 'src/app/Model/Custom-Http-Repsonse';
import { FileUploadStatus } from 'src/app/Model/file-upload.status';
import { User } from 'src/app/Model/User';
import { AuthenticationService } from 'src/app/service/authentication.service';
import { NotificationService } from 'src/app/service/notification.service';
import { UserService } from 'src/app/service/user.service';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit, OnDestroy {
  private titleSubject = new BehaviorSubject<string>('Users');
  public titleAction$ = this.titleSubject.asObservable();
  public users!: User[];
  public user!: User;
  public refreshing!: boolean;
  public selectedUser!: User;
  public fileName!: string;
  public profileImage!: File;
  private subscriptions: Subscription[] = [];
  public editUser = new User();
  private currentUsername!: string;
  public fileStatus = new FileUploadStatus();

  constructor(private router: Router, private authenticationService: AuthenticationService,
              private userService: UserService, public notificationService: NotificationService) {}

  ngOnInit(): void {
    var localUsers = this.authenticationService.getUserFromLocalCache();
    if(localUsers)
    this.user = localUsers;
    this.getUsers(true);
  }

  public changeTitle(title: string): void {
    this.titleSubject.next(title);
  }

  public getUsers(showNotification: boolean): void {
    this.refreshing = true;
    this.subscriptions.push(
      this.userService.getUsers().subscribe(
        (response: User[]) => {
          this.userService.addUsersToLocalcache(response);
          this.users = response;
          this.refreshing = false;
          if (showNotification) {
            this.sendNotification(`${response.length} user(s) loaded successfully.`, 'success');
          }
        },
        (errorResponse: HttpErrorResponse) => {
          this.sendNotification(errorResponse.error.message, 'danger');
          this.refreshing = false;
        }
      )
    );

  }

  public onSelectUser(selectedUser: User): void {
    this.selectedUser = selectedUser;
    this.clickButton('openUserInfo');
  }

  public onProfileImageChange(event: Event): void {
    var htmlInputElement = (event.target as HTMLInputElement);
    if(htmlInputElement?.files){
      this.fileName =  htmlInputElement.files[0].name;
      this.profileImage = htmlInputElement.files[0];
    }
  }

  public saveNewUser(): void {
    this.clickButton('new-user-save');
  }

  public onAddNewUser(userForm: NgForm): void {
      const formData = this.userService.createUserFormDate(null, userForm.value, this.profileImage);
      this.subscriptions.push(
        this.userService.addUser(formData).subscribe(
          (response: User) => {
            this.clickButton('new-user-close');
            this.getUsers(false);
            this.fileName = null;
            this.profileImage = null;
            userForm.reset();
            this.sendNotification(`${response.firstName} ${response.lastName} added successfully`, 'success');
          },
          (errorResponse: HttpErrorResponse) => {
            this.sendNotification(errorResponse.error.message, 'danger');
            this.profileImage = null;
          }
        )
      );
  }

  public onUpdateUser(): void {
      const formData = this.userService.createUserFormDate(this.currentUsername, this.editUser, this.profileImage);
    
      this.subscriptions.push(
        this.userService.updateUser(formData).subscribe(
          (response: User) => {
            this.clickButton('closeEditUserModalButton');
            this.getUsers(false);
            this.fileName = null;
            this.profileImage = null;
            this.sendNotification(`${response.firstName} ${response.lastName} updated successfully`, 'success');
          },
          (errorResponse: HttpErrorResponse) => {
            this.sendNotification(errorResponse.error.message, 'danger');
            this.profileImage = null;
          }
        )
      );
  }

  public onUpdateCurrentUser(user: User): void {
    this.refreshing = true;
    var cache = this.authenticationService.getUserFromLocalCache();
    if(cache !== null)
    this.currentUsername = cache.username;
      const formData = this.userService.createUserFormDate(this.currentUsername, user, this.profileImage);
      this.subscriptions.push(
        this.userService.updateUser(formData).subscribe(
          (response: User) => {
            this.authenticationService.addUserToLocalCache(response);
            this.getUsers(false);
            this.fileName = null;
            this.profileImage = null;
            this.sendNotification(`${response.firstName} ${response.lastName} updated successfully`, 'success');
          },
          (errorResponse: HttpErrorResponse) => {
            this.sendNotification(errorResponse.error.message, 'danger');
            this.refreshing = false;
            this.profileImage = null;
          }
        )
      );
    this.refreshing = false;
  }

  public onUpdateProfileImage(): void {
    const formData = new FormData();
    if(this.user && this.profileImage){
      formData.append('username', this.user.username);
      formData.append('profileImage', this.profileImage);
    }
    this.subscriptions.push(
      this.userService.updateProfileImage(formData).subscribe(
        (event: HttpEvent<any>) => {
          this.reportUploadProgress(event);
        },
        (errorResponse: HttpErrorResponse) => {
          this.sendNotification(errorResponse.error.message, 'danger');
          this.fileStatus.status = 'done';
        }
      )
    );
  }

  private reportUploadProgress(event: HttpEvent<any>): void {
    switch (event.type) {
      case HttpEventType.UploadProgress:
        if(event.total)
        this.fileStatus.percentage = Math.round(100 * event.loaded / event.total);
        this.fileStatus.status = 'progress';
        break;
      case HttpEventType.Response:
        if (event.status === 200) {
          if(this.user)
          this.user.profileImageUrl = `${event.body.profileImageUrl}?time=${new Date().getTime()}`;
          this.sendNotification(`${event.body.firstName}\'s profile image updated successfully`, 'success');
          this.fileStatus.status = 'done';
          break;
        } else {
          this.sendNotification(`Unable to upload image. Please try again`, 'success');
          break;
        }
      default:
        `Finished all processes`;
    }
  }

  public updateProfileImage(): void {
    this.clickButton('profile-image-input');
  }
 
  public onLogOut(): void {
    this.authenticationService.logOut();
    this.router.navigate(['/login']);
    this.sendNotification(`You've been successfully logged out`, 'success');
  }

  public onResetPassword(emailForm: NgForm): void {
    this.refreshing = true;
    const emailAddress = emailForm.value['reset-password-email'];
    this.subscriptions.push(
      this.userService.resetPassword(emailAddress).subscribe(
        (response: CustomHttpRespone) => {
          this.sendNotification(response.message, 'success');
          this.refreshing = false;
        },
        (error: HttpErrorResponse) => {
          this.sendNotification(error.error.message, 'warning');
          this.refreshing = false;
        },
        () => emailForm.reset()
      )
    );
  }

  public onDeleteUder(username: string | undefined): void {
    if(username)
    this.subscriptions.push(
      this.userService.deleteUser(username).subscribe(
        (response: CustomHttpRespone) => {
          this.sendNotification(response.message, 'success');
          this.getUsers(false);
        },
        (error: HttpErrorResponse) => {
          this.sendNotification(error.error.message, 'danger');
        }
      )
    );
  }

  public onEditUser(editUser: User): void {
    this.editUser = editUser;
    this.currentUsername = editUser.username;
    this.clickButton('openUserEdit');
  }

  public searchUsers(searchTerm: string): void {
    const results: User[] = [];
    var localCacheUsers = this.userService.getUserFromLocalCache();
    if(localCacheUsers)
    for (const user of localCacheUsers) {
      if (user.firstName.toLowerCase().indexOf(searchTerm.toLowerCase()) !== -1 ||
          user.lastName.toLowerCase().indexOf(searchTerm.toLowerCase()) !== -1 ||
          user.username.toLowerCase().indexOf(searchTerm.toLowerCase()) !== -1 ||
          user.userId.toLowerCase().indexOf(searchTerm.toLowerCase()) !== -1) {
          results.push(user);
      }
    }
    this.users = results;
    if (results.length === 0 || !searchTerm) {
      this.users = this.userService.getUserFromLocalCache();
    }
  }

  public get isAdmin(): boolean {
    return this.getUserRole() === Role.ADMIN || this.getUserRole() === Role.SUPER_ADMIN;
  }

  public get isManager(): boolean {
    return this.isAdmin || this.getUserRole() === Role.MANAGER;
  }

  public get isAdminOrManager(): boolean {
    return this.isAdmin || this.isManager;
  }

  private getUserRole(): string {
    var localCacheUsers = this.authenticationService.getUserFromLocalCache();
    if(localCacheUsers)
    return localCacheUsers.role;
    return 'no';
  }

  private sendNotification(message: string, color: string): void {
    if (message) {
      this.notificationService.addToast(message, color);
    } else {
      this.notificationService.addToast('An error occurred. Please try again.', color);
    }
  }

  private clickButton(buttonId: string): void {
    document.getElementById(buttonId)?.click();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

}
