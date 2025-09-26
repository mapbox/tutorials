import React, { useRef, useEffect, useState } from 'react'
import mapboxgl from 'mapbox-gl'
import customMarkerPng from './img/custom-marker.png'
import restaurantsData from './data/providence-restaurants.json'
import Popup from './Popup'

import './map.css'

export default function Map({ layerState }) {
  const mapContainer = useRef(null)
  const mapRef = useRef(null)

  const [popupData, setPopupData] = useState(null)

  const handleMarkerClick = (e) => {
    setPopupData({ lngLat: e.feature.geometry.coordinates, properties: e.feature.properties });
  }

  useEffect(() => {
    if (mapRef.current) return // init once

    const token = import.meta.env.VITE_MAPBOX_TOKEN
    mapboxgl.accessToken = token

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

        // add layer
        if (!mapRef.current.getLayer(layerId)) {
          mapRef.current.addLayer({
            id: layerId,
            type: 'symbol',
            source: 'restaurants',
            layout: {
              'icon-image': 'custom-marker',
              'icon-size': 1,
              'icon-allow-overlap': true
            },
            'paint': {
              'icon-color': color,
              'icon-opacity': 0.8
            },
            filter: ['in', ['get', 'foodType'], ['literal', [name]]]
          })
        }
      }

      // add a click interaction for each of the layers to be used to render the popup
      layerState.forEach(layer => {
        const layerId = `restaurants-${layer.name}-symbol`
        mapRef.current.addInteraction(`${layerId}-click`, {
          type: 'click',
          target: { layerId },
          handler: handleMarkerClick
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
  }, [layerState])


  // Render toggles on the webpage
  return (
    <div className="map-wrapper">
      <Popup mapRef={mapRef} popupData={popupData} />
      <div ref={mapContainer} className="map-container" />
    </div>
  )

}

