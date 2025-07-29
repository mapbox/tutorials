import Foundation
import UIKit
import MapboxSearch
import MapboxSearchUI
import MapboxMaps

class ViewController: UIViewController {
    // Creates a controller for the Mapbox Search Panel, creating UI for you to use Mapbox Search
    let searchController = MapboxSearchController()
    
    // Creates the map
    private var mapView = MapView(frame: .zero)
    
    // Creates and manages location markers
    lazy var annotationsManager = mapView.annotations.makePointAnnotationManager()
    private let image = UIImage(named: "dest-pin")!
    private lazy var markerHeight: CGFloat = image.size.height
    
    // Creates a panel containing the business name and street name of a selected marker
    let detailsPanel = BottomSheetUIView()
    
    // Handles if tracking the users location is cancelled
    private var cancelables = Set<AnyCancelable>()
    private var locationTrackingCancellation: AnyCancelable?
    
    func addSearchController() {
        searchController.delegate = self
        /// Add MapboxSearchUI above the map
        let panelController = MapboxPanelController(rootViewController: searchController)
        addChild(panelController)
    }
    
    func setUserLocation() {
        // By default, the built-in 2D puck doesn't show the user's heading.
        mapView.location.options.puckType = .puck2D()
        
        // To display the heading, you must enable it explicitly as follows:
        let configuration = Puck2DConfiguration.makeDefault(showBearing: true)
        mapView.location.options.puckType = .puck2D(configuration)
        
        locationTrackingCancellation = mapView.location.onLocationChange.observe { [weak mapView] newLocation in
                guard let location = newLocation.last, let mapView else { return }
                mapView.camera.ease(
                    to: CameraOptions(center: location.coordinate, zoom: 15),
                    duration: 1.3)
        }
    }
    
    func addMapToScreen() {
        // Adds the map to the app, stretching it to fill the bounds of the screen
        mapView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(mapView)
        NSLayoutConstraint.activate([
            mapView.topAnchor.constraint(equalTo: view.topAnchor),
            mapView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            mapView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            mapView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor),
        ])
    }
    
    func setupDetailsPanel(){
            self.view.addSubview(detailsPanel)
            detailsPanel.translatesAutoresizingMaskIntoConstraints = false

            NSLayoutConstraint.activate([
                detailsPanel.centerXAnchor.constraint(equalTo: self.view.centerXAnchor),
                detailsPanel.topAnchor.constraint(equalTo: self.view.bottomAnchor, constant: -200),
                detailsPanel.widthAnchor.constraint(equalToConstant: 1000),
                detailsPanel.heightAnchor.constraint(equalToConstant: 1000)
            ])
        }
    
    override func viewDidLoad() {
        super.viewDidLoad()

                addMapToScreen()
                addSearchController()
                setupDetailsPanel()
                setUserLocation()
            }

            let locationManager = CLLocationManager()

            override func viewDidAppear(_ animated: Bool) {
                super.viewDidAppear(animated)

                locationManager.requestWhenInUseAuthorization()
    }
    
    func deletePreviousAnnotations() {
        annotationsManager.annotations.removeAll()
        mapView.viewAnnotations.removeAll()
    }
    
    func showAnnotations(results: [SearchResult], cameraShouldFollow: Bool = true) {
        
           annotationsManager.annotations += results.map { result in
               let point = PointAnnotation(coordinate: result.coordinate)
               
               let address = result.address?.street

               MakeAnnotation(point, pointname: result.name, pointaddress: address ?? "")
               
               return point
           }

           if cameraShouldFollow {
               cameraToAnnotations(annotationsManager.annotations)
           }
       }
    func MakeAnnotation(_ annotation: PointAnnotation, pointname: String, pointaddress: String)
        {
        // Define a geographic coordinate.
        let someCoordinate = annotation.point.coordinates
        var pointAnnotation = PointAnnotation(coordinate: someCoordinate)

        // Make the annotation show a red pin
        pointAnnotation.image = .init(image: image, name: "dest-pin")
        
        pointAnnotation.tapHandler = {_ in
                    //print("tapped annotation: \(pointname)")
                    self.detailsPanel.nameLabel.text = pointname
                    self.detailsPanel.addressLabel.text = pointaddress
                    self.detailsPanel.setHidden(hidden: false)
                    
                    return true
                }

        // Add the annotation to the manager in order to render it on the map.
        annotationsManager.annotations += [pointAnnotation]
        
        let annotationView = AnnotationView(frame: CGRect(x: 0, y: 0, width: 100, height: 80))
        annotationView.title = String(format: pointname, annotation.point.coordinates.latitude, annotation.point.coordinates.longitude)
                let annotation = ViewAnnotation(coordinate: annotation.point.coordinates, view: annotationView)
                annotation.allowOverlap = true
                annotation.variableAnchors = [ViewAnnotationAnchorConfig(anchor: .bottom, offsetY: markerHeight - 7)]
                annotationView.onClose = { [weak annotation] in annotation?.remove() }
                annotationView.onSelect = { [weak annotation] selected in
                    annotation?.priority = selected ? 1 : 0
                    annotation?.setNeedsUpdateSize()
                }
                mapView.viewAnnotations.add(annotation)
        
    }
    
    func cameraToAnnotations(_ annotations: [PointAnnotation]) {
            if annotations.count == 1, let annotation = annotations.first {
                mapView.camera.fly(
                    to: .init(center: annotation.point.coordinates, zoom: 13),
                    duration: 0.25,
                    completion: nil
                )
                
            } else {
                do {
                    let cameraState = mapView.mapboxMap.cameraState
                    let coordinatesCamera = try mapView.mapboxMap.camera(
                        for: annotations.map(\.point.coordinates),
                        camera: CameraOptions(cameraState: cameraState),
                        coordinatesPadding: UIEdgeInsets(top: 24, left: 24, bottom: 24, right: 24),
                        maxZoom: nil,
                        offset: nil
                    )

                    mapView.camera.fly(to: coordinatesCamera, duration: 0.25, completion: nil)
                } catch {
                    _Logger.searchSDK.error(error.localizedDescription)
                }
            }
        }
}

extension ViewController: SearchControllerDelegate {
    func searchResultSelected(_ searchResult: SearchResult) {
        deletePreviousAnnotations()
        showAnnotations(results: [searchResult])
    }
    func categorySearchResultsReceived(category: SearchCategory, results: [SearchResult]) {
        deletePreviousAnnotations()
        showAnnotations(results: results)
    }
    func userFavoriteSelected(_ userFavorite: FavoriteRecord) {
        deletePreviousAnnotations()
        showAnnotations(results: [userFavorite])
    }
}
