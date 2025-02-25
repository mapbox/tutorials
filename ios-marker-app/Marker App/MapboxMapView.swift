import SwiftUI
import MapboxMaps

// MARK: Struct Declaration
// with failable initializer
struct DogGroomerLocation: Equatable {
    var storeName: String
    var address: String
    var city: String
    var postalCode: String
    var phoneFormatted: String? = nil
    var rating: Double? = nil

    init?(queriedFeature: QueriedFeature) {
        guard let properties = queriedFeature.feature.properties,
            case let .string(storeName) = properties["storeName"],
            case let .string(address) = properties["address"],
            case let .string(city) = properties["city"],
            case let .string(postalCode) = properties["postalCode"] else { return nil }
        self.storeName = storeName
        self.address = address
        self.city = city
        self.postalCode = postalCode
        if case let .string(phoneFormatted) = properties["phoneFormatted"] {
            self.phoneFormatted = phoneFormatted
        }
        if case let .number(rating) = properties["rating"] {
            self.rating = rating
        }
    }
}

// MARK: MapboxMapView
struct MapboxMapView: UIViewRepresentable {
    let center: CLLocationCoordinate2D
    @ObservedObject var gestureTokens: GestureTokens // Use the mutable tokens class
    @Binding var selectedFeature: DogGroomerLocation? // Binding for selected feature name
    var onMapViewCreated: ((MapView) -> Void)? // Closure to pass back MapView
  
    func makeUIView(context: Context) -> MapView {
        let mapInitOptions = MapInitOptions(
            cameraOptions: CameraOptions(
                center: center,
                zoom: 8.5),
            styleURI: StyleURI(rawValue: "mapbox://styles/examples/cm37hh1nx017n01qk2hngebzt") ?? .streets
        )
        let mapView = MapView(frame: .zero, mapInitOptions: mapInitOptions)
        
        // Removes scale & compass on map
        mapView.ornaments.options.compass.visibility = .hidden
        mapView.ornaments.options.scaleBar.visibility = .hidden

        // Use dispatch to make sure the closure is executed after the map view is initialized.
          DispatchQueue.main.async {
              if let onMapViewCreated = onMapViewCreated {
                  onMapViewCreated(mapView)
              }
          }
        
        // Set up tap gesture handling on the layer
        setupLayerTapGesture(for: mapView)

        return mapView
    }

    func updateUIView(_ uiView: MapView, context: Context) {
        // Any updates if needed
    }
    
    // MARK:  Private Helper Function
    private func setupLayerTapGesture(for mapView: MapView) {
        mapView.gestures.onLayerTap("dog-groomers-boston-marker") { queriedFeature, _ in
            let selectedGroomerLocation = DogGroomerLocation(queriedFeature: queriedFeature)
            //print("queriedFeature", queriedFeature.feature)
            
            // Use self.selectedFeature since selectedFeature is a @Binding passed down from the parent view
            self.selectedFeature = selectedGroomerLocation
            
            var featureId: String = ""
            if let identifier = queriedFeature.feature.identifier,
               case let .number(value) = identifier {
                featureId = String(Int(value)) // Convert to integer and then to string
                print("featureId", featureId)
            } else {
                print("featureId is nil or not a number")
            }
            
            mapView.mapboxMap.setFeatureState(
                    sourceId: "composite",
                    sourceLayerId: "dog-groomers-selected",
                    featureId: featureId,
                    state: ["selected": true]) { result in
                       switch result {
                       case .failure(let error):
                           print("Could not retrieve feature state: \(error).")
                       case .success:
                           print("Succesfully set feature state.")
                       }
                   }
//            
           
            
            return true
        }.store(in: &gestureTokens.tokens)
        
        
    }

}


@_spi(Experimental) import MapboxMaps // Interactions API is experimental.
import SwiftUI

struct InteractionsExample: View {
    // A state for currently selected feature.
    @State var selectedFeature: FeaturesetFeature?

    var body: some View {
        Map {
            if let selectedFeature {
                // Apply highlight state to the selected feature.
                // When selectedFeature becomes nil, the selection will be automatically removed.
                FeatureState(selectedFeature, ["selected": true])
            }

            TapInteraction(.layer("dog-groomers-selected")) { feature, context in
                // Select the feature.
                selectedFeature = feature
                return true // Return false if you want features below to continue handling this interaction.
            }

            TapInteraction { context in
                // When tap didn't hit any features, reset the selected feature.
                selectedFeature = nil
                return true
            }
        }
    }
}


