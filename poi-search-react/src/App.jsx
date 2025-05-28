import { useState, useEffect, useRef } from 'react'
import { SearchBoxCore, SessionToken } from '@mapbox/search-js-core'
import mapboxgl from 'mapbox-gl'

import POIMarker from './POIMarker'

import 'mapbox-gl/dist/mapbox-gl.css'
import './App.css'

const MAPBOX_ACCESS_TOKEN = "YOUR_MAPBOX_ACCESS_TOKEN"

const DEFAULT_MAP_BOUNDS = [
  [-74.03189, 40.69684],
  [-73.98121, 40.72286]
]


function App() {
  const mapRef = useRef() // ref for the Map() instance
  const searchRef = useRef() // ref for the SearchBoxCore() instance
  const mapContainerRef = useRef() // ref for the map container DOM element

  const [searchResults, setSearchResults] = useState([]) // an array of search results
  const [searchCategory, setSearchCategory] = useState() // the selected category
  const [mapBounds, setMapBounds] = useState() // the current map bounds
  const [searchBounds, setSearchBounds] = useState() // the bounds of the search results
  const [showSearchAreaButton, setShowSearchAreaButton] = useState(false) // whether to show the "Search this area" button

 
  // function to perform a category search using the SearchBoxCore() instance
  // uses the current map bounds and the selected category to search for points of interest
  const performCategorySearch = async () => {
    if (!searchCategory || !mapBounds) return;
    const { features } = await searchRef.current.category(searchCategory, { bbox: mapBounds, limit: 25 });
    setSearchResults(features);
    setSearchBounds(mapBounds);
  };

  useEffect(() => {
    mapRef.current = new mapboxgl.Map({
      accessToken: MAPBOX_ACCESS_TOKEN, // set the Mapbox access token
      container: mapContainerRef.current, // display the map in this DOM element
      bounds: DEFAULT_MAP_BOUNDS, // set the initial map bounds
      minZoom: 13, // set the minimum zoom level to avoid zooming out too far
      config: {
        basemap: {
          showPointOfInterestLabels: false, // disable POI labels
        }
      },
    })

    // when the map is loaded, set mapBounds to the current map bounds
    mapRef.current.on('load', () => {
      setMapBounds(mapRef.current.getBounds().toArray())
    })

    // when the map moves, set mapBounds to the current map bounds
    mapRef.current.on('moveend', () => {
      setMapBounds(mapRef.current.getBounds().toArray())
    })



    // instantiate the search box
    searchRef.current = new SearchBoxCore({ accessToken: MAPBOX_ACCESS_TOKEN })
    new SessionToken();

    // cleanup function: remove the map when the component unmounts
    return () => {
      mapRef.current.remove()
    }
  }, [])

  // when the searchCategory changes, perform a category search
  useEffect(() => {
    performCategorySearch()
  }, [searchCategory])

  // determine whether to show the "Search this area" button based on the current map bounds and the bounds used for the most recent search
  // if the map bounds have changed since the last search, show the button
  useEffect(() => {
    function boundsChanged(boundsA, boundsB) {
      if (!boundsA || !boundsB) return false
      return JSON.stringify(boundsA) !== JSON.stringify(boundsB)
    }

    if (searchCategory && boundsChanged(mapBounds, searchBounds)) {
      setShowSearchAreaButton(true)
    } else {
      setShowSearchAreaButton(false)
    }
  }, [mapBounds, searchCategory, searchBounds])

  // configuration array for category search buttons
  const categoryButtons = [
    { label: "☕ Coffee", value: "coffee" },
    { label: "🍽️ Restaurants", value: "restaurant" },
    { label: "🍸 Bars", value: "bar" },
    { label: "🏨 Hotels", value: "hotel" },
    { label: "🏛️ Museums", value: "museum" }
  ]

  return (
    <>
      {/* Show category search buttons */}
      <div className="button-container">
        {categoryButtons.map(({ label, value }) => (
          <button
            key={value}
            onClick={() => setSearchCategory(value)}
            className={`category-button ${searchCategory === value && 'active'}`}
          >
            {label}
          </button>
        ))}
      </div>

      {/* Show "search this area" button */}
      {showSearchAreaButton && (
        <button
          onClick={performCategorySearch}
          className="search-area-button"
        >
          Search this area
        </button>
      )}

      {/* Map container */}
      <div id='map-container' ref={mapContainerRef} />

      {/* render a POIMarker for each feature in searchResults */}
      {searchResults.length > 0 && searchResults.map((feature) => (
        <POIMarker key={feature.properties.mapbox_id} map={mapRef.current} feature={feature} category={searchCategory} />
      ))}
    </>
  )
}

export default App
