import SwiftUI
@_spi(Experimental) import MapboxMaps

struct ContentView: View {
    @ObservedObject private var geofencing = Geofencing()
    let featureCollection = decodeGeoJSON(from: "yellowstone")

    var body: some View {
        let center = CLLocationCoordinate2D(latitude: 44.5979, longitude: -110.6123)
        Map(initialViewport: .camera(center: center, zoom: 9, bearing: 0, pitch: 0)) {
            Puck2D()

            GeoJSONSource(id: "yellowstone-locations")
                .data(.featureCollection(featureCollection))

            FillLayer(id: "yellowstone-regions", source: "yellowstone-locations")
                .fillColor(Exp(.match) {
                    // Use an expression to update the color of the feature's polygon when an event is received for that feature
                    Exp(.id)
                    geofencing.lastEvent?.feature.identifier?.string ?? "default"
                    Exp(.match) {
                        // Change to color based on the type of event
                        geofencing.lastEvent?.type.description ?? "none"
                        "entry"
                        "rgba(7, 144, 30, 0.8)" // green
                        "exit"
                        "rgba(173, 17, 5, 0.8)" // red
                        "dwell"
                        "rgba(17, 97, 195, 0.8)" // blue
                        "rgba(119, 119, 119, 1)" // gray
                    }
                    "rgba(119, 119, 119, 1)" // gray
                })
                .fillOpacity(0.7)
        }
        .onMapLoaded { _ in
            geofencing.start {
                for feature in featureCollection.features {
                    // To receive dwell events we need to set a time.
                    // After a user has spent that amount of time in the geofence
                    // the Geofencing service will send a dwell event
                    var geofencingFeature = feature
                    geofencingFeature.properties?[GeofencingPropertiesKeys.dwellTimeKey] = 1 // minutes
                    geofencing.add(feature: geofencingFeature)
                }
            }
        }
        .ignoresSafeArea()
        .overlay(alignment: .bottom) {
            EventView(event: geofencing.lastEvent)
        }
    }
}

private final class Geofencing: ObservableObject {
    @Published var lastEvent: GeofenceEvent?

    func start(_ completion: @escaping () -> Void) {
        let geofencing = GeofencingFactory.getOrCreate()
        geofencing.configure(options: GeofencingOptions()) { [weak self] result in
            guard let self else { return }
            /// Geofences are stored in database on disk.
            /// To make this example isolated and synchronized with the UI we delete existing feature from database.
            geofencing.clearFeatures { result in
                geofencing.addObserver(observer: self) { result in
                    print("Add observer: \(result)")
                }
                completion()
            }
        }
    }

    func add(feature: Turf.Feature) {
        let geofencing = GeofencingFactory.getOrCreate()
        geofencing.addFeature(feature: feature) { result in
            print("Add feature result: \(result)")
        }
    }
}

extension Geofencing: GeofencingObserver {
    // Push a new GeofenceEvent when the user enters a geofence region
    func onEntry(event: GeofencingEvent) {
        DispatchQueue.main.async { self.lastEvent = GeofenceEvent(type: .entry, event: event) }
    }

    // Push a new GeofenceEvent when the user dwells in a geofence region
    func onDwell(event: GeofencingEvent) {
        DispatchQueue.main.async { self.lastEvent = GeofenceEvent(type: .dwell, event: event) }
    }

    // Push a new GeofenceEvent when the user exits a geofence region
    func onExit(event: GeofencingEvent) {
        DispatchQueue.main.async { self.lastEvent = GeofenceEvent(type: .exit, event: event) }
    }

    func onError(error: GeofencingError) {
        DispatchQueue.main.async {
            print(error)
        }
    }

    func onUserConsentChanged(isConsentGiven: Bool) {
        DispatchQueue.main.async {
            print("Is consent given: \(isConsentGiven)")
        }
    }
}

private struct GeofenceEvent {
    enum GeofenceEventType {
        case entry
        case dwell
        case exit

        var description: String {
            switch self {
            case .entry:
                return "entry"
            case .dwell:
                return "dwell"
            case .exit:
                return "exit"
            }
        }

        var formatted: String {
            switch self {
            case .entry:
                return "Entered"
            case .dwell:
                return "Dwelled in"
            case .exit:
                return "Exited"
            }
        }
    }

    var type: GeofenceEventType
    var feature: Turf.Feature
    var geofenceName: String
    var timestamp: Date

    init(type: GeofenceEventType, event: GeofencingEvent) {
        self.type = type
        self.feature = event.feature
        self.geofenceName = {
            switch event.feature.properties?["name"] {
            case let .string(name):
                name
            default:
                "unknown geofence"
            }
        }()
        self.timestamp = event.timestamp
    }
}

private struct EventView: View {
    let event: GeofenceEvent?
    var body: some View {
        if let event {
            VStack {
                Text(event.type.formatted) +
                Text(" \(event.geofenceName) at ") +
                Text(event.timestamp, style: .time)
            }
            .font(.subheadline)
            .padding(10)
            .background(.white)
            .clipped()
            .shadow(radius: 1.4, y: 0.7)
            .cornerRadius(10)
            .offset(y: -50)
        }
    }
}

// Load GeoJSON file from local bundle and decode into a `FeatureCollection`.
private func decodeGeoJSON(from fileName: String) -> FeatureCollection {
    guard let path = Bundle.main.path(forResource: fileName, ofType: "geojson") else {
        preconditionFailure("File '\(fileName)' not found.")
    }

    let filePath = URL(fileURLWithPath: path)
    var featureCollection: FeatureCollection
    do {
        let data = try Data(contentsOf: filePath)
        featureCollection = try JSONDecoder().decode(FeatureCollection.self, from: data)
    } catch {
        print("Error parsing data: \(error)")
        featureCollection = FeatureCollection(features: [])
    }

    return featureCollection
}

#Preview {
    ContentView()
}
