// Import necessary frameworks
import SwiftUI        // For the user interface components
import MapboxSearch   // For location search and autocomplete functionality

struct SearchScreen: View {
    // State to track the current search input text
    @State private var searchText: String = ""
    
    // Array to store search suggestions returned from Mapbox Search API
    @State private var suggestions: [PlaceAutocomplete.Suggestion] = []
    
    // Mapbox PlaceAutocomplete instance for performing searches
    @State private var placeAutocomplete = PlaceAutocomplete()
    
    // Callback function to notify parent view when a suggestion is selected
    let onSuggestionSelected: (PlaceAutocomplete.Result) -> Void
    
    
    var body: some View {
        VStack(spacing: 20) {
            // Search input field with floating card appearance
            TextField("Search for places...", text: $searchText)
                .textFieldStyle(RoundedBorderTextFieldStyle())  // Built-in rounded border style
                .background(Color.white)        // White background for visibility
                .cornerRadius(10)               // Rounded corners for modern look
                .shadow(radius: 5)              // Drop shadow for floating effect
                .padding(.horizontal)           // Horizontal padding from screen edges
                .onChange(of: searchText) { oldValue, newValue in
                    // Trigger search whenever the user types
                    performSearch(query: newValue)
                }
            
            // Display suggestions list only when we have results
            if !suggestions.isEmpty {
                List(suggestions, id: \.mapboxId) { suggestion in
                    // Each suggestion displayed as a card with name and description
                    VStack(alignment: .leading, spacing: 4) {
                        // Primary name of the location (e.g., "Central Park")
                        Text(suggestion.name)
                            .font(.headline)
                            .fontWeight(.bold)
                        
                        // Secondary description (e.g., "New York, NY, United States")
                        Text(suggestion.description ?? "")
                            .font(.caption)
                            .foregroundColor(.gray)
                    }
                    .padding(.vertical, 4)
                    .onTapGesture {
                        // Handle user selection of this suggestion
                        handleSuggestionSelection(suggestion)
                    }
                }
                .frame(minHeight: 0, maxHeight: 500)    // Limit height to avoid taking full screen
                .listStyle(PlainListStyle())            // Remove default list styling
                .background(Color.white)                // White background for visibility
                .cornerRadius(10)                       // Rounded corners to match search field
                .shadow(radius: 5)                      // Drop shadow for floating effect
                .padding(.horizontal)                   // Match search field padding
            }
        }
    }
    
    
    // Performs a search query using Mapbox Search API
    private func performSearch(query: String) {
        // Clear suggestions if search text is empty
        guard !query.isEmpty else {
            suggestions = []
            return
        }
        
        // Call Mapbox PlaceAutocomplete API with search parameters
        placeAutocomplete.suggestions(
            for: query,
            // Optional: Limit search results to a specific geographic region
            // This example uses a bounding box around Toronto, Canada
            region: BoundingBox(
                CLLocationCoordinate2D(
                    latitude:  43.60698,   // Southwest corner latitude
                    longitude: -79.49555   // Southwest corner longitude
                ),
                CLLocationCoordinate2D(
                    latitude: 43.75953,    // Northeast corner latitude
                    longitude: -79.29422   // Northeast corner longitude
                )
            ),
            // Optional: Bias results toward a specific location (Toronto city center)
            proximity: CLLocationCoordinate2D(latitude: 43.65050, longitude: -79.35954)
            
        ) { result in
            // Handle the API response
            switch result {
            case .success(let suggestionResults):
                // Update UI on main thread when we receive successful results
                DispatchQueue.main.async {
                    print("Received \(suggestionResults.count) suggestions:")
                    // Debug: Print each suggestion for development purposes
                    for (index, suggestion) in suggestionResults.enumerated() {
                        print("Suggestion \(index): \(suggestion)")
                    }
                    // Update the suggestions array to trigger UI refresh
                    suggestions = suggestionResults
                }
                
            case .failure(let error):
                // Handle API errors (network issues, invalid API key, etc.)
                debugPrint(error)
                DispatchQueue.main.async {
                    // Clear suggestions on error
                    suggestions = []
                }
            }
        }
    }
    
    
    // Handles when user taps on a search suggestion
    private func handleSuggestionSelection(_ selectedSuggestion: PlaceAutocomplete.Suggestion) {
        // Clean up the UI by clearing suggestions and search text
        suggestions = []
        searchText = ""
        
        // Convert the suggestion into a full result with complete location details
        placeAutocomplete.select(suggestion: selectedSuggestion) { result in
            switch result {
            case .success(let suggestionResult):
                // Successfully retrieved full location details
                // Pass the result back to the parent view (ContentView)
                onSuggestionSelected(suggestionResult)
                
            case .failure(let error):
                // Handle selection errors (network issues, invalid suggestion, etc.)
                debugPrint(error)
            }
        }
    }
}
