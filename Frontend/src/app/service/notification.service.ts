import { DOCUMENT } from '@angular/common';
import { Inject } from '@angular/core';
import { Injectable } from '@angular/core';
import { Toast } from '../Model/Toast';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  toasts: Toast[] = [];
  public message!: string;
  public color!: string;

  constructor(@Inject(DOCUMENT) private document: HTMLDocument) { }

  
  public addToast(message: string, color: string): void {
    var myToast: Toast = new Toast(message, color);
    this.toasts.push(myToast);
    var _this = this;
    setTimeout(function() {const index = _this.toasts.indexOf(myToast);
                            if (index > -1) {
                              _this.toasts.splice(index, 1);
                            }}, 5000);
  }

  public onCloseToast(toast: Toast): void {
    const index = this.toasts.indexOf(toast);
    if (index > -1) {
      this.toasts.splice(index, 1);
    }
  }

  ///////////////
  public onToatsIt(message: string, color: string) : void {
    this.onToast();
  }

  private onToast(): void {
    var x = document.getElementById('liveToast');
    if(x!=null) {
      x.classList.add('show');
    }
    setTimeout(function() {
        var x = document.getElementById('liveToast');
        if(x!=null) {
      x.classList.remove('show');
    }
    }, 5000);
  }

    public onClosetoast(): void {
    var x = document.getElementById('liveToast');
    if(x!=null) {
      x.classList.remove('show');
    }
  }
}
