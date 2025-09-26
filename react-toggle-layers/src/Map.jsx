import React, { useRef, useEffect, useState } from 'react'
import mapboxgl from 'mapbox-gl'
import './map.css'
import CustomMarker from './custom-marker.png'


export default function Map() {
  const mapContainer = useRef(null)
  const mapRef = useRef(null)
  const popupRef = useRef(null)

  const [tags, setTags] = useState([])
  const toggles = useRef(new Set())

  // color map of colors for food types
        const colorMap = {
          vegetarian: '#33a02c',
          sandwich: '#ffff99  ',
          asian: '#6a3d9a',
          american: '#a6cee3',
          coffee: '#e31a1c',
          mexican: '#cab2d6',
          seafood: '#1f78b4',
          'ice cream': '#fb9a99',
          korean: '#cab2d6',
          sushi: '#b2df8a',
          italian: '#ff7f00'
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

    // Add zoom in/zoom out buttons
    mapRef.current.addControl(new mapboxgl.NavigationControl({ visualizePitch: true }), 'top-right')

    // fetch geojson and create per-tag sources+layers
    mapRef.current.on('load', async () => {
      try {

        // grab restaraunt data from the geojson file
        const resp = await fetch('/providence-restaurants.geojson')
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`)

        const data = await resp.json()

        const tagsSet = new Set()
        const features = data.features

        features.forEach(element => {

            if(!tagsSet.has(element.properties.foodType))
            {
                tagsSet.add(element.properties.foodType)
            }
        });

        // load image to use as a custom marker TODO SWITCH TO LOCAL FILE
          mapRef.current.loadImage(
          'https://docs.mapbox.com/mapbox-gl-js/assets/custom_marker.png',
            (error, image) => {
          if (error) throw error;
          mapRef.current.addImage("custom-marker", image, { sdf: true });
        }
      );

        //mapRef.current.addImage('custom-marker', './custom-marker.png', { sdf: true })

        // create a source+layer per tag
        for (const tag of tagsSet) {
          const srcId = `restaurants-${tag}-src`
          const layerId = `restaurants-${tag}-symbol`

          const feats = features.filter((f) => (f.properties.foodType.includes(tag)))
          const fc = { type: 'FeatureCollection', features: feats }

          if (mapRef.current.getSource && mapRef.current.getSource(srcId)) {
              mapRef.current.getSource(srcId).setData(fc)
          } else {
              mapRef.current.addSource(srcId, { type: 'geojson', data: fc })
          }

          // grab related color from colorMap to correctly color the markers
          const color = colorMap[tag]

          // add layer
            if (!mapRef.current.getLayer(layerId)) {
              mapRef.current.addLayer({
                id: layerId,
                type: 'symbol',
                source: srcId,
                layout: {
                  'icon-image': 'custom-marker',
                  'icon-size': 1,
                  'icon-allow-overlap': true
                },
                'paint':
                {
                  'icon-color': color,
                  'icon-opacity': 0.8
                }
              })

              mapRef.current.setLayoutProperty(layerId, 'visibility', 'none')
            }

          // handlers
          const onClick = (e) => {
            const feature = e?.features && e.features[0]
            const coords = (feature.geometry && feature.geometry.coordinates)
            const food = feature.properties.foodType
            const name = feature.properties.name
            const foodHtml = `<span class="cuisine-badge">${food}</span>`
            if (popupRef.current) { popupRef.current.remove(); popupRef.current = null }
            const popup = new mapboxgl.Popup({ offset: 12 }).setLngLat(coords).setHTML(`<div class="popup"><strong>${name}</strong><div class="cuisines">${foodHtml}</div></div>`).addTo(mapRef.current)
            popupRef.current = popup
          }

          const onEnter = () => { mapRef.current.getCanvas().style.cursor = 'pointer' }
          const onLeave = () => { mapRef.current.getCanvas().style.cursor = '' }

          mapRef.current.on('click', layerId, onClick)
          mapRef.current.on('mouseenter', layerId, onEnter)
          mapRef.current.on('mouseleave', layerId, onLeave)
        }

        setTags(tagsSet)

      } catch (err) {
        console.error('Failed to load restaurants GeoJSON', err)
      }
    })
  }, [])


  // When a checkbox is toggled, show/hide the related layer
  function handleCheckboxChange(e) {

  if (popupRef.current) { popupRef.current.remove(); popupRef.current = null }

  // If "all" is toggled, turn all layers on TODO: turn on other checkmarks
  if(e.target.name === 'all')
  {
    toggles.current.forEach(toggle => {
      console.log(toggle) // = e.target.checked
      mapRef.current.setLayoutProperty(`restaurants-${toggle.key}-symbol`, 'visibility', 'visible')
    });
  }
  // If an individual layer is toggled, toggle visibility for that layer
  else
  {
    if(e.target.checked)
      {
        mapRef.current.setLayoutProperty(`restaurants-${e.target.name}-symbol`, 'visibility', 'visible')
      }
      else
      {
        mapRef.current.setLayoutProperty(`restaurants-${e.target.name}-symbol`, 'visibility', 'none')
      }
  } 
}

// Individual toggle switch component TODO: color switch according to food type
// TODO Caveats - accent-color is not fully supported across browsers and other methods require making your own custom component

function ToggleSwitch(props){

    return (
      <div className="toggle-switch">
        <input
          type="checkbox"
          className="toggle-switch-checkbox"
          name={props.Name}
          checked={props.isChecked}
          onChange={handleCheckboxChange}
        />
        <label className="toggle-switch-label" htmlFor={props.Name}>
          <span className="toggle-switch-inner" />
          <span className="toggle-switch-switch" />
        </label>

        {props.Name}

      </div>
    );
  }

// List of toggle switch components
function ToggleSwitchList(props)
{
  const objs = new Set()

  for(const objName of props.objTags)
  {
    objs.add(<ToggleSwitch key={objName} Name={objName}/>)
  }

  toggles.current = objs

return (
      <>
      <div id="toggle-list">
      <div key="all"><ToggleSwitch Name="all"/></div>
      <div>{objs}</div>
      </div>
    </>
  );
}

// Render toggles on the webpage
  return (
    <div className="map-wrapper">
      <div className="map-controls">
      <ToggleSwitchList objTags={tags}/>
      </div>
      <div ref={mapContainer} className="map-container" />
    </div>
  )

}

