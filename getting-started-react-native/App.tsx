/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import { MapView, StyleImport, ShapeSource, LineLayer, Camera, LocationPuck } from '@rnmapbox/maps';
import { StyleSheet, Platform, PermissionsAndroid } from 'react-native';
import { useEffect } from 'react';

// Load local GeoJSON (Metro supports requiring JSON files directly)
const lineString = require('./assets/rio_marathon.json');

// Mapbox LineLayer style (not a React Native StyleSheet)
const lineLayerStyle: any = {
  lineColor: '#ff0000',
  lineWidth: 6.0,
};

const requestLocationPermission = async () => {
  if (Platform.OS === 'android') {
    try {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
        {
          title: 'Location Permission',
          message: 'This app needs access to your location to show your position on the map.',
          buttonNeutral: 'Ask Me Later',
          buttonNegative: 'Cancel',
          buttonPositive: 'OK',
        },
      );
      return granted === PermissionsAndroid.RESULTS.GRANTED;
    } catch (err) {
      console.warn(err);
      return false;
    }
  }
  return true;
};

const App = () => {
  useEffect(() => {
    requestLocationPermission();
  }, []);

  return (
    <MapView
      styleURL={"mapbox://styles/mapbox/standard"}
      style={styles.map}
      projection='globe'
      scaleBarEnabled={false}
      logoPosition={Platform.OS === 'android' ? { bottom: 40, left: 10 } : undefined}
      attributionPosition={Platform.OS === 'android' ? { bottom: 40, right: 10 } : undefined}
    >
      <Camera
        zoomLevel={12.1}
        centerCoordinate={[-43.2268, -22.9358]}
        pitch={70}
        heading={-161.81}
        animationDuration={0}
        animationMode='none'
      />
      <StyleImport
        id="basemap"
        existing
        config={{
          lightPreset: 'dawn',
          showLandmarkIcons: 'true'
        }}
      />
      <ShapeSource id="line-source" shape={lineString}>
        <LineLayer id="line-layer" style={lineLayerStyle} slot='middle' />
      </ShapeSource>
      <LocationPuck
        puckBearingEnabled
        puckBearing="heading"
        pulsing={{ isEnabled: true }}
      />
    </MapView>
  );
};

const styles = StyleSheet.create({
  map: {
    flex: 1,
    width: '100%',
  },
});

export default App;
