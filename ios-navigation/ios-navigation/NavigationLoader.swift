import MapboxNavigationCore
import CoreLocation

class NavigationLoader {
    func loadNavigation(
        from origin: CLLocationCoordinate2D,
        to destination: CLLocationCoordinate2D
    ) async throws -> PreparedNavigation {
        let locationSource: LocationSource = .simulation(initialLocation: .init(CLLocation(latitude: origin.latitude, longitude: origin.longitude)))
        // to use live navigation, uncomment the line below and comment out the line above
        // let locationSource: LocationSource = .live()
        
        // instantiate a MapboxNavigationProvider
        let provider = MapboxNavigationProvider(coreConfig: .init(locationSource: locationSource))
        
        // use the navigation provider to fetch routes
        let result = await provider.mapboxNavigation
            .routingProvider()
            .calculateRoutes(options: NavigationRouteOptions(coordinates: [origin, destination]))
            .result
        
        return switch result {
        case .failure(let error):
            throw error
        case .success(let routes):
            // return both the routes and the provider, to be used in presenting the navigation view controller
            PreparedNavigation(routes: routes, navigationProvider: provider)
        }
    }
}
