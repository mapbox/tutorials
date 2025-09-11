import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:mapbox_maps_flutter/mapbox_maps_flutter.dart';

void main() {
  runApp(MyApp());
  
  // Configure Mapbox access token from environment variable
  String accessToken = const String.fromEnvironment("ACCESS_TOKEN");
  MapboxOptions.setAccessToken(accessToken);
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Mapbox Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MapScreen(),
    );
  }
}

class MapScreen extends StatefulWidget {
  @override
  _MapScreenState createState() => _MapScreenState();
}

class _MapScreenState extends State<MapScreen> {
    MapboxMap? mapboxMap;

  _onMapCreated(MapboxMap mapboxMap) async {
    this.mapboxMap = mapboxMap;
    
    // Wait for style to load, then configure it
    await mapboxMap.style.isStyleLoaded();
    
    // Configure the Standard style with dawn lighting
    await mapboxMap.style.setStyleImportConfigProperty(
      "basemap", 
      "lightPreset", 
      "dawn"
    );
    
    // Enable landmark icons for interaction
    await mapboxMap.style.setStyleImportConfigProperty(
      "basemap", 
      "showLandmarkIcons", 
      true
    );

    // Add landmark icon tap interaction
    var landmarkIconTapInteraction = TapInteraction(
      FeaturesetDescriptor(
        featuresetId: "landmark-icons", 
        importId: "basemap"
      ), 
      (landmarkIcon, tapContext) {
        // Get the tapped location and landmark name
        var landmarkLocation = tapContext.point;
        var landmarkName = landmarkIcon.properties['name_en'] ?? 'unknown landmark';

        // Animate the camera to the landmark
        mapboxMap.flyTo(
          CameraOptions(center: landmarkLocation), 
          MapAnimationOptions(duration: 2000)
        );

        // Show user feedback
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text("Flying to $landmarkName"),
            duration: Duration(seconds: 2),
          )
        );
      }
    );

    // Add the interaction to the map
    mapboxMap.addInteraction(landmarkIconTapInteraction);

        // Load and display the marathon route
    var marathonData = await rootBundle.loadString('assets/rio_marathon.geojson');
    
    // Add the data as a source
    await mapboxMap.style.addSource(GeoJsonSource(
      id: "rio_marathon_source", 
      data: marathonData
    ));

    // Add a line layer to visualize the route
    await mapboxMap.style.addLayer(LineLayer(
      id: "rio_marathon", 
      sourceId: "rio_marathon_source", 
      slot: "middle",
      lineColor: Colors.red.toARGB32(), 
      lineWidth: 6.0,
    ));

        // Enable user location with pulsing animation
    await mapboxMap.location.updateSettings(LocationComponentSettings(
      enabled: true, 
      pulsingEnabled: true,
    ));

  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: MapWidget(
        cameraOptions: CameraOptions(
          center: Point(coordinates: Position(-43.18326, -22.90796)),
          zoom: 14.5,
          bearing: -161.81,
          pitch: 70,
        ),
        onMapCreated: _onMapCreated,
      ),
    );
  }
}
