import { Injectable } from '@angular/core';
import * as signalR from '@microsoft/signalr';
import { HubConnection, HubConnectionBuilder } from '@microsoft/signalr';

@Injectable({
  providedIn: 'root',
})
export class SignalRService {
  private hubConnection!: HubConnection;

  startConnection = (Id: any) => {
    const userId = Id;
    console.log('eeeeeeeeee',userId)
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
      console.log(data);
    });
  };
}
