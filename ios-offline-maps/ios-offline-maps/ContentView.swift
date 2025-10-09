// Import necessary frameworks for the app
import SwiftUI          // For the user interface
import MapboxMaps       // For map rendering and interaction
import CoreLocation

struct ContentView: View {
    @State private var showingDownloadView = false
    
    var body: some View {
        ZStack {
            // Map takes full screen as the base layer
            MapReader { proxy in
                Map(initialViewport: .camera(
                    center: CLLocationCoordinate2D(
                        latitude: 39.5,
                        longitude: -98.0
                    ),
                    zoom: 2)) {
                        
                        
                    }
                    .mapStyle(.standard())  // Use the Mapbox Standard style
                    .onMapLoaded { event in
                        // Map is loaded and ready
                        print("Map loaded successfully")
                    }
            }
            
            // Download button overlay
            VStack {
                HStack {
                    Spacer()
                    Button(action: {
                        showingDownloadView = true
                    }) {
                        HStack {
                            Image(systemName: "arrow.down.circle.fill")
                            Text("Manage Offline Regions")
                        }
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                        .shadow(radius: 3)
                    }
                    .padding()
                }
                Spacer()
            }
        }
        .sheet(isPresented: $showingDownloadView) {
            TileRegionDownloadView()
        }
    }
}
