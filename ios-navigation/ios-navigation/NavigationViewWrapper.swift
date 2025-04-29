import SwiftUI
import MapboxNavigationUIKit
import MapboxNavigationCore
import CoreLocation
import MapboxMaps

// Coordinator to bridge UIKit's NavigationViewController delegate with SwiftUI
class Coordinator: NSObject, NavigationViewControllerDelegate {
    var onCancel: () -> Void
    
    // Initialize with a closure that will be called when navigation is cancelled or dismissed
    init(onCancel: @escaping () -> Void) {
        self.onCancel = onCancel
    }
    
    // Called when the NavigationViewController is dismissed
    func navigationViewControllerDidDismiss(
        _ navigationViewController: NavigationViewController,
        byCanceling canceled: Bool
    ) {
        print("Dismissed. Canceled: \(canceled)")
        onCancel() // Invoke the callback to dismiss the SwiftUI sheet
    }
}



// SwiftUI wrapper to embed the Mapbox NavigationViewController
struct NavigationViewWrapper: UIViewControllerRepresentable {
    let preparedNavigation: PreparedNavigation
    var onCancel: () -> Void = {}  // Callback when navigation is cancelled
    
    // Provides the coordinator instance for delegate handling
    func makeCoordinator() -> Coordinator {
        Coordinator(onCancel: onCancel)
    }
    
    // Creates the UIKit view controller that hosts the navigation experience
    func makeUIViewController(context: Context) -> UIViewController {
        let routes = preparedNavigation.routes
        let navigationProvider = preparedNavigation.navigationProvider
        let navigationOptions = NavigationOptions(
            mapboxNavigation: navigationProvider.mapboxNavigation,
            voiceController: navigationProvider.routeVoiceController,
            eventsManager: navigationProvider.eventsManager()
        )
        
        // 4. Create a NavigationViewController using the routes and options
        let navigationViewController = NavigationViewController(
            navigationRoutes: routes,
            navigationOptions: navigationOptions
        )
        
        navigationViewController.delegate = context.coordinator // Set the delegate for dismissal
        navigationViewController.routeLineTracksTraversal = true // Show route line tracking
        navigationViewController.modalPresentationStyle = .fullScreen
        
        // Optionally set the initial camera view
        if let origin = routes.waypoints.first?.coordinate {
            navigationViewController.navigationMapView?.mapView.mapboxMap.setCamera(
                to: CameraOptions(center: origin, zoom: 11.0)
            )
        }
        
        return navigationViewController
    }
    
    // No need to update the UIViewController after it's created
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
