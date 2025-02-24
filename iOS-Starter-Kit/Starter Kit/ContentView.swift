//
//  ContentView.swift
//  Starter Kit
//
//  Created by Andrew Sepic on 11/8/24.
//

import SwiftUI
import Combine

// The API may change in future releases.
@_spi(Experimental) import MapboxMaps

// MARK: GestureTokens Class
class GestureTokens: ObservableObject {
    var tokens = Set<AnyCancellable>()
}

// MARK:  ContentView
struct ContentView: View {
    @StateObject private var gestureTokens = GestureTokens()
    @State private var selectedFeature: DogGroomerLocation? = nil
    @State private var mapView: MapView? = nil
    @State private var hasInteracted = false
    
    // Hold a strong reference to the gesture delegate
    @State private var gestureDelegate: GestureManagerDelegateImplementation?


    var body: some View {
        let center = CLLocationCoordinate2D(
            latitude: 42.34622,
            longitude: -71.09290
        )
     
        ZStack(alignment: .bottom) {
            MapboxMapView(
                center: center,
                gestureTokens: gestureTokens,
                selectedFeature: $selectedFeature,
                onMapViewCreated: { mapView in
                    self.mapView = mapView
                    // Create and retain the gesture delegate
                   let delegate = GestureManagerDelegateImplementation { gestureType in
                       DispatchQueue.main.async {
                           self.hasInteracted = true
                       }
                   }
                   
                   // Assign the gesture delegate
                   mapView.gestures.delegate = delegate
                   self.gestureDelegate = delegate // Retain the delegate strongly
                    
                })
            .ignoresSafeArea(.all)
            
            VStack(
                alignment: .leading,
                spacing: 10
            ) {
                HStack(alignment: .top) {
                    AppIntro()
                    Spacer()
                    
                    if hasInteracted { // Only show button if interacted
                        Button( action: {
                            mapView?.camera.fly(to: CameraOptions(
                                center: center,
                                zoom: 8.5,
                                bearing: 0,
                                pitch: 0
                            ), duration: 5.0)
                            // Reset the hasInteracted & selectedFeature vars
                            hasInteracted = false
                            selectedFeature = nil
                        }) {
                            Text("Reset Map")
                                .padding()
                                .background(Color.blue)
                                .foregroundColor(.white)
                                .cornerRadius(8)
                        }
                        .controlSize(.mini) // Reduce button size
                        .padding([.trailing], 8) // Add padding to the top
                    }
                    
                   
                }
                //.border(Color.red, width: 1)
                Spacer()
            }
            
            if let feature = selectedFeature {
                DrawerView(feature: feature) {
                    withAnimation {
                        selectedFeature = nil
                    }
                }
                .transition(.move(edge: .bottom))
            }
        }
        .ignoresSafeArea(edges: [.bottom, .leading])
    }
}

// MARK: GestureManager

// GestureManagerDelegate Implementation
class GestureManagerDelegateImplementation: NSObject, GestureManagerDelegate {
    var onGestureBegin: (GestureType) -> Void
    
    init(onGestureBegin: @escaping (GestureType) -> Void) {
        self.onGestureBegin = onGestureBegin
    }
    
    func gestureManager(_ gestureManager: GestureManager, didBegin gestureType: GestureType) {
        // Notify when a gesture begins
        onGestureBegin(gestureType)
        //print("\(gestureType) didBegin")
    }

    func gestureManager(_ gestureManager: GestureManager, didEnd gestureType: GestureType, willAnimate: Bool) {
        //print("\(gestureType) didEnd")
    }

    func gestureManager(_ gestureManager: GestureManager, didEndAnimatingFor gestureType: GestureType) {
        //print("didEndAnimatingFor \(gestureType)")
    }
}


// MARK: Supporting View
// App Header - statically placed in top left of map.
struct AppIntro: View {
    
    var body: some View {
        VStack(alignment: .leading) {
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
            //.border(Color.gray, width: 4)
         
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
        }

    }
        
}


// MARK: Preview

#Preview {
    ContentView()
}


