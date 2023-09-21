import { Injectable } from '@angular/core';
import * as signalR from '@microsoft/signalr';
import { HubConnection, HubConnectionBuilder } from '@microsoft/signalr';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SignalRService {
  private hubConnection!: HubConnection;
  private dataSubject = new Subject<any>();
  data$ = this.dataSubject.asObservable();

  startConnection = (Id: any) => {
    const userId = Id;
    this.hubConnection = new signalR.HubConnectionBuilder()
      .withUrl(`http://localhost:8080/signalr?userId=${userId}`)
      .withAutomaticReconnect()
      .build();

    this.hubConnection
      .start()
      .then(() => console.log('Connection started'))
      .catch((err) => console.log('Error while starting connection: ' + err));
  };

  addTransferChartDataListener = () => {
    this.hubConnection.on('newMessage', (data) => {
      this.dataSubject.next(data);
    });
  }
}
