import SwiftUI
import MapboxMaps

struct ContentView: View {
    @State var selectedPlaces = [StandardPlaceLabelsFeature]()
    @State private var selectedPriceLabel: FeaturesetFeature?

    var body: some View {
        Map(initialViewport: .camera(center: .init(latitude: 40.72, longitude: -73.99), zoom: 11, pitch: 45)) {
            StyleImport(id: "new-york-hotels", uri: StyleURI(url: styleURL)!)

            TapInteraction(.standardPlaceLabels) { placeLabel, _ in
                selectedPlaces.append(placeLabel)
                return true
            }

            ForEvery(selectedPlaces, id: \.id) { placeLabel in
                FeatureState(placeLabel, .init(select: true))
            }

            LongPressInteraction { _ in
                selectedPlaces.removeAll()
                return true
            }

            TapInteraction(.featureset("hotels-price", importId: "new-york-hotels")) { priceLabel, _ in
                /// Select a price label when it's clicked
                selectedPriceLabel = priceLabel
                return true
            }

            if let selectedPriceLabel, let coordinate = selectedPriceLabel.geometry.point?.coordinates {
                /// When there's a selected price label, we use it to set a feature state.
                /// The `hidden` state is implemented in `new-york-hotels.json` and hides label and icon.
                FeatureState(selectedPriceLabel, ["hidden": true])

                /// Instead of label we show a callout annotation with animation.
                MapViewAnnotation(coordinate: coordinate) {
                    HotelCallout(feature: selectedPriceLabel)
                    /// The `id` makes the view to be re-created for each unique feature
                    /// so appearing animation plays each time.
                        .id(selectedPriceLabel.id)
                }
                .variableAnchors([.init(anchor: .bottom)])
            }
        }
        .mapStyle(.standard(
            lightPreset: .dawn
        ))
        .ignoresSafeArea()
    }
}

private struct HotelCallout: View {
    var feature: FeaturesetFeature

    @State private var scale: CGFloat = 0.1

    var body: some View {
        VStack(alignment: .center, spacing: 2) {
            Text(feature.properties["name"]??.string ?? "—")
                .font(.headline)
                .foregroundColor(.black)
            Text(feature.properties["price"]??.number.map { "$ \(Int($0))" } ?? "—")
                .font(.subheadline)
                .foregroundColor(.green)
                .fontWeight(.bold)
        }
        .padding(6)
        .background(Color.white.opacity(0.9))
        .cornerRadius(8)
        .scaleEffect(scale, anchor: .bottom)
        .onAppear {
            withAnimation(Animation.interpolatingSpring(stiffness: 200, damping: 16)) {
                scale = 1.0
            }
        }
    }
}

private let styleURL = Bundle.main.url(forResource: "new-york-hotels", withExtension: "json")!

#Preview {
    ContentView()
}
