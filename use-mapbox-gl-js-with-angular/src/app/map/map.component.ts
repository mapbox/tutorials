import {  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
  inject,
  signal } from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';


const INITIAL_CENTER: [number, number] = [-98.54818, 40.00811];
const INITIAL_ZOOM = 3.2;

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [CommonModule], // imports built-in pipes like number, date, currency
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit, OnDestroy {
  @ViewChild('mapContainer', { static: true }) mapContainer!: ElementRef;
  map: any;
  private platformId = inject(PLATFORM_ID);

  // Signals to track center and zoom
  center = signal<[number, number]>(INITIAL_CENTER);
  zoom = signal<number>(INITIAL_ZOOM);
 

  async ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
    // Dynamically import Mapbox GL JS with default export.
    const mapboxgl = (await import('mapbox-gl')).default
  
      this.map = new mapboxgl.Map({
        accessToken: 'YOUR_MAPBOX_ACCESS_TOKEN', // Replace with your Mapbox access token
        container: this.mapContainer.nativeElement,
        center: this.center(),
        zoom: this.zoom()
      });

      this.map.on('move', () => {
        const newCenter = this.map.getCenter();

        this.center.set([newCenter.lng, newCenter.lat]);
        this.zoom.set(this.map.getZoom());
      });
    }
  }

   resetView() {
    if (this.map) {
      this.map.flyTo({
        center: INITIAL_CENTER,
        zoom: INITIAL_ZOOM
      });
    }
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }
}
