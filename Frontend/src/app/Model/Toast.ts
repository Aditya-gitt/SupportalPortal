export class Toast {
    message!: string;
    color!: string;

    constructor(message: string, color: string){
        this.message = message;
        this.color = color;
    }
}