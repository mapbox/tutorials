// Import necessary frameworks for the app
import SwiftUI          // For the user interface
import MapboxMaps       // For map rendering and interaction
import MapboxSearch     // For location search functionality
import CoreLocation     // For coordinate handling

struct ContentView: View {
    // Viewport controls the map's camera position (center, zoom level)
    // Starting with a view of the continental United States
    @State var viewport: Viewport = .camera(
        center: CLLocationCoordinate2D(
            latitude: 39.5,   
            longitude: -98.0  
        ),
        zoom: 2)
    
    // Store the selected search result to display as a marker
    @State private var selectedResult: PlaceAutocomplete.Result?
    
    // Track the ID of the selected result to trigger map animations
    @State private var selectedResultId: String?
    
    
    var body: some View {
        ZStack {
            // Map takes full screen as the base layer
            MapReader { proxy in
                Map(viewport: $viewport) {
                    // Add a marker annotation when a location is selected
                    if let result = selectedResult, let coordinate = result.coordinate {
                        PointAnnotation(coordinate: CLLocationCoordinate2D(
                            latitude: coordinate.latitude,
                            longitude: coordinate.longitude
                        ))
                        .image(named: "marker")      // Use custom marker image
                        .iconSize(0.5)               // Scale the marker to half size
                        .iconAnchor(.bottom)         // Anchor point at bottom of marker
                        .iconOffset(x: 0, y: 12)    // Fine-tune marker positioning
                    }
                }
                .mapStyle(.standard())  // Use the Mapbox Standard style
            }
            
            // Floating search interface overlaid on the map
            VStack {
                // Search component handles autocomplete and result selection
                SearchScreen(onSuggestionSelected: handleSuggestionSelection)
                Spacer()  // Pushes search UI to top of screen
            }
            .padding(.top, 50)  // Add padding to avoid status bar overlap
        }
        // Monitor changes to selectedResultId to trigger map animations
        .onChange(of: selectedResultId) { oldValue, newValue in
            // When a new result is selected, animate the map to that location
            if let _ = newValue, let result = selectedResult {
                flyToLocation(result)
            }
        }
    }
    
    // Called when user selects a search suggestion
    private func handleSuggestionSelection(_ result: PlaceAutocomplete.Result) {
        selectedResult = result           // Store the selected result
        selectedResultId = result.mapboxId // Update ID to trigger map animation
    }
    
    // Animates the map camera to fly to the selected location
    private func flyToLocation(_ result: PlaceAutocomplete.Result) {
        // Ensure the result has valid coordinates
        if let coordinate = result.coordinate {
            // Convert search result coordinate to CLLocationCoordinate2D
            let mapCoordinate = CLLocationCoordinate2D(
                latitude: coordinate.latitude,
                longitude: coordinate.longitude
            )
            
            // Animate the viewport change with a smooth fly animation
            withViewportAnimation(.fly(duration: 3)) {
                viewport = .camera(
                    center: mapCoordinate,
                    zoom: 15.0  // Zoom in for detailed view of the location
                )
            }
        }
    }
}



