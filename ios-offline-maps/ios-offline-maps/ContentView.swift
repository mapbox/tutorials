import SwiftUI
import MapboxMaps

struct ContentView: View {
    @State private var showingDownloadView = false
    
    var body: some View {
        ZStack {
            Map(
                initialViewport: .camera(
                    center: CLLocationCoordinate2D(
                        latitude: 39.5,
                        longitude: -98.0
                    ),
                    zoom: 2)
            )
            .ignoresSafeArea()
            // Download button overlay
            VStack(alignment: .trailing) {
                Button(action: {
                    showingDownloadView = true
                }) {
                    Label("Manage Offline Regions", systemImage: "arrow.down.circle.fill")
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                        .shadow(radius: 3)
                }
                .padding()
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topTrailing)
        }
        .sheet(isPresented: $showingDownloadView) {
            TileRegionDownloadView()
        }
    }
}
