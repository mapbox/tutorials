import SwiftUI
import CoreLocation
import Combine
import MapboxNavigationCore

// LocationManager handles the device's location updates
class LocationManager: NSObject, ObservableObject, CLLocationManagerDelegate {
    private let manager = CLLocationManager()
    
    // Publishes the current location to subscribers
    @Published var currentLocation: CLLocationCoordinate2D?
    
    // Initializes the CLLocationManager and starts updating location
    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
        manager.requestWhenInUseAuthorization() // Request location access permission from the user
        manager.startUpdatingLocation() // Start receiving location updates
    }
    
    // Delegate method called when location is updated
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        currentLocation = locations.last?.coordinate // Store the most recent location
    }
    
    // Delegate method called when location updates fail
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("Failed to get location: \(error)") // Print error if location fails
    }
}

// Represents a destination point with name and coordinates
struct Destination: Identifiable {
    let id = UUID()
    let name: String
    let coordinates: CLLocationCoordinate2D
}

//
struct PreparedNavigation: Identifiable {
    let id = UUID()
    let routes: NavigationRoutes
    let navigationProvider: MapboxNavigationProvider
}

// List of predefined destination points
let destinations = [
    Destination(name: "Columbus Circle", coordinates: CLLocationCoordinate2D(latitude: 40.76804, longitude: -73.98190)),
    Destination(name: "Empire State Building", coordinates: CLLocationCoordinate2D(latitude: 40.74843, longitude: -73.98568)),
    Destination(name: "Brooklyn Bridge Park", coordinates: CLLocationCoordinate2D(latitude: 40.70223, longitude: -73.99656)),
]

// Main view that displays a list of destinations to choose from
struct DestinationListView: View {
    @State private var preparedNavigation: PreparedNavigation? = nil
    @StateObject private var locationManager = LocationManager() // Observes the user's current location
    private let navigationLoader = NavigationLoader()
    
    var body: some View {
        NavigationStack {
            List(destinations) { destination in
                Button(action: {
                    // Check if the current location is available before proceeding
                    guard let origin = locationManager.currentLocation else {
                        print("Current location not available yet.")
                        return
                    }
                    
                    Task {
                        let destinationCoord = destination.coordinates
                        if let result = try? await navigationLoader.loadNavigation(from: origin, to: destinationCoord) {
                            preparedNavigation = result
                            let distance = result.routes.mainRoute.route.distance
                            print("✅ Navigation to \(destination.name) ready: route distance: \(distance) meters.")
                        }
                    }
                    
                }) {
                    // Layout for each destination button
                    HStack {
                        Image(systemName: "location.fill").foregroundColor(.blue)
                        Text(destination.name)
                        Spacer()
                        Image(systemName: "arrow.turn.up.right").foregroundColor(.gray)
                    }
                    .padding(.vertical, 8)
                }
            }
            .navigationTitle("Choose Destination") // Title for the navigation stack
            // Present the navigation view full screen when OD pair is set
            .fullScreenCover(item: $preparedNavigation) { preparedNavigation in
                NavigationViewWrapper(
                    preparedNavigation: preparedNavigation,
                    onCancel: {
                        self.preparedNavigation = nil // Reset when user cancels
                    }
                )
                .edgesIgnoringSafeArea(.all) // Make the navigation view full screen
            }
            
        }
    }
}
