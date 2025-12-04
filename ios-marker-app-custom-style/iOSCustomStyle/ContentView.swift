import SwiftUI
import MapboxMaps

struct ContentView: View {
    
    @State var presentSheet = false
    @State var locationName = "Test Name"
    @State var locationAddress = "Test Address"
    @State var locationPhoneNumber = "Test Phone Number"
    @State var locationRating = "Test Rating"
    
    var body: some View {
        // Center coordinates for Boston, MA
        let center = CLLocationCoordinate2D(
            latitude: 42.34622,
            longitude: -71.09290
        )
    ZStack(alignment: .bottom) {
        Map(initialViewport: .camera(center: center, zoom: 7, bearing: 0, pitch: 0))
        {
            TapInteraction(.layer("dog-groomers-boston-marker")) { feature, context in
                grabPOIData(properties: feature.properties)
                return true // stops propagation
            }
            
            TapInteraction(.layer("dog-groomers-3o4sdb")) { feature, context in
                grabPOIData(properties: feature.properties)
                return true // stops propagation
            }
        }
        .mapStyle(MapStyle(uri: StyleURI(rawValue: "mapbox://styles/examples/cm37hh1nx017n01qk2hngebzt")!))
        .ornamentOptions(OrnamentOptions(scaleBar: ScaleBarViewOptions(visibility: .hidden),compass: CompassViewOptions(visibility: .hidden)))
        .ignoresSafeArea()
        
        // Adds UI element with call to action and app title text
        VStack(alignment: .trailing) {
                HStack {
                    Image(systemName: "pawprint.fill")
                        .foregroundColor(.blue)
                        .padding([.leading, .top, .bottom], 6)
                    Text("Pet Spa Finder")
                        .font(.headline)
                        .foregroundColor(.black)
                        .cornerRadius(3)
                        .padding([.top, .trailing, .bottom], 6)
                }
                .padding(12)
                .background(
                    RoundedRectangle(cornerRadius: 3)
                        .fill(Color.white)
                )
                
                Text("Click a marker for more information.")
                    .font(.caption)
                    .foregroundColor(.black)
                    .padding(8)
                    .frame(width: UIScreen.main.bounds.width * 0.45)
                    .background(
                        RoundedRectangle(cornerRadius: 3)
                            .fill(Color.white)
                    )
                    .cornerRadius(2)
        }.offset(x:100, y: -650)
        
    }
        .ignoresSafeArea(edges: [.bottom, .leading]).sheet(isPresented: $presentSheet) {
            VStack(alignment: .leading) {
                Text(locationName)
                    .font(.system(size: 30))
                    .presentationDetents([.height(200)])
                Text(locationRating)
                    .safeAreaInset(edge: .leading) { Image(systemName: "star") }
                    .presentationDetents([.height(200)])
                Text(locationAddress)
                    .safeAreaInset(edge: .leading) { Image(systemName: "house") }
                    .presentationDetents([.height(200)])
                Text(locationPhoneNumber)
                    .safeAreaInset(edge: .leading) { Image(systemName: "phone") }
                    .presentationDetents([.height(200)])
            }
        }
}
    
    func grabPOIData(properties: JSONObject)
    {
        locationName = (properties["storeName"]!!).rawValue as! String;
        
        let streetAddress = (properties["address"]!!).rawValue as! String;
        let city = (properties["city"]!!).rawValue as! String;
        let postalCode = (properties["postalCode"]!!).rawValue as! String;
        
        locationAddress = streetAddress + ", " + city + ", " + postalCode;
        locationPhoneNumber = (properties["phoneFormatted"]!!).rawValue as! String;
        let rating = (properties["rating"]!!).rawValue as! Double;
        locationRating = "\(rating)"
        
        presentSheet = true;
    }
    
    
}
