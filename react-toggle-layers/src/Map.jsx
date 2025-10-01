import React, { useRef, useEffect, useState } from 'react'
import mapboxgl from 'mapbox-gl'
import customMarkerPng from './img/custom-marker.png'
import restaurantsData from './data/providence-restaurants.json'
import Popup from './Popup'

import './app.css'

export default function Map({ layerState }) {
  const mapContainer = useRef(null)
  const mapRef = useRef(null)

  const [popupData, setPopupData] = useState(null)

  // Handles when a marker is clicked, setting grabbing the marker data, passing it to a popup and then renders the over the marker.
  const handleMarkerClick = (e) => {
    setPopupData({ lngLat: e.feature.geometry.coordinates, properties: e.feature.properties });
  }

  // Initializes the map
  useEffect(() => {
    if (mapRef.current) return // init once

    mapboxgl.accessToken = "YOUR_MAPBOX_ACCESS_TOKEN"

    // creates map instance and centers viewport over Providence, RI, USA
    mapRef.current = new mapboxgl.Map({
      container: mapContainer.current,
      center: [-71.407, 41.8205],
      zoom: 15.5
    })

    // fetch geojson and create per-tag sources+layers
    mapRef.current.on('load', async () => {

      // load image to use as a custom marker TODO SWITCH TO LOCAL FILE
      mapRef.current.loadImage(
        customMarkerPng,
        (error, image) => {
          if (error) throw error;
          mapRef.current.addImage("custom-marker", image, { sdf: true });
        }
      );

      // add a single source for all restaurants
      mapRef.current.addSource('restaurants', {
        type: 'geojson',
        data: restaurantsData
      })

      // add a layer for each food type
      for (const layer of layerState) {
        const { name, color } = layer

        const layerId = `restaurants-${name}-symbol`

        // add a symbol layer for each food type, filtering to only show features with that food type.
        if (!mapRef.current.getLayer(layerId)) {
          mapRef.current.addLayer({
            id: layerId,
            type: 'symbol',
            source: 'restaurants',

            // Grabs local image for custom marker, allows markers to over lap and colors each marker based on the related foodType color.
            layout: {
              'icon-image': 'custom-marker',
              'icon-size': 1,
              'icon-allow-overlap': true
            },
            'paint': {
              'icon-color': color,
              'icon-opacity': 0.8,
              'icon-halo-color': '#ffffff',
              'icon-halo-width': 2.5,
              'icon-halo-blur': 1
            },
            filter: ['in', ['get', 'foodType'], ['literal', [name]]]
          })
        }
      }

      // Adds interactivity - click and hover states
      layerState.forEach(layer => {
        const layerId = `restaurants-${layer.name}-symbol`
        // add a click interaction for each of the layers to be used to render the popup
        mapRef.current.addInteraction(`${layerId}-click`, {
          type: 'click',
          target: { layerId },
          handler: handleMarkerClick
        })
        // change the cursor to a pointer when hovering over a marker
        mapRef.current.addInteraction(`${layerId}-mouse-enter`, {
          type: 'mouseenter',
          target: { layerId },
          handler:  () => {
            mapRef.current.getCanvas().style.cursor = 'pointer';
          }
        })
        // reset the cursor to default image when cursor leaves a marker
        mapRef.current.addInteraction(`${layerId}-mouse-leave`, {
          type: 'mouseleave',
          target: { layerId },
          handler:  () => {
            mapRef.current.getCanvas().style.cursor = '';
          }
        })
      })
    })
  }, [])

  useEffect(() => {
    // When layerState changes, update map visibility
    if (!mapRef.current) return // wait for map to initialize

    layerState.forEach(layer => {
      const layerId = `restaurants-${layer.name}-symbol`
      if (mapRef.current.getLayer(layerId)) {
        const visibility = layer.isChecked ? 'visible' : 'none'
        mapRef.current.setLayoutProperty(layerId, 'visibility', visibility)
      }
    })

    // Sends empty data to the popup object to hide it
    setPopupData();

  }, [layerState])


  // Render toggles on the webpage
  return (
    <div className="map-wrapper">
      <Popup mapRef={mapRef} popupData={popupData} />
      <div ref={mapContainer} className="map-container" />
    </div>
  )

}

