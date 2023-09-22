import { Component, OnInit} from '@angular/core';
import { SignalRService } from './signalr.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'angular-tour-of-heroes';
  notifications : any[] = [];
  usedId: any;
  constructor(private signalRService: SignalRService) {}
  ngOnInit(): void {
  
  }

  notification(userId: any): void {
    this.signalRService.startConnection(userId);
    this.signalRService.addTransferChartDataListener();
    this.signalRService.data$.subscribe((data) => {
      this.notifications.push(...data);
      console.log('eeeeeeeeeee',this.notifications)
    });
  }
}
