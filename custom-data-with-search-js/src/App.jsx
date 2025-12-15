import { useRef, useEffect, useState } from 'react'
import mapboxgl from 'mapbox-gl'
import SearchBoxContainer from './SearchBoxContainer'
import { buildAirportIndex, searchAirports } from './utils/search'
import 'mapbox-gl/dist/mapbox-gl.css';
import './App.css'

const accessToken = import.meta.env.VITE_MAPBOX_ACCESS_TOKEN;
const center = [-71.05953, 42.36290];

function App() {

  const mapRef = useRef()
  const mapContainerRef = useRef()
  const airportDataRef = useRef(null)
  const [airportIndex, setAirportIndex] = useState(null)

  useEffect(() => {
    mapboxgl.accessToken = accessToken

     mapRef.current = new mapboxgl.Map({
      container: mapContainerRef.current,
      center:  center,
      zoom: 13,
    });

   // Load airport data and build index
    const loadAirportData = async () => {
      try {
        const res = await fetch('./US_Airports.geojson')
        const json = await res.json()
        
        airportDataRef.current = json
        const index = buildAirportIndex(json)
        setAirportIndex(index)
        
        console.log('Airport index built:', index.size, 'prefixes indexed')
        // highlight-start
        // Test the search function
        const testResults = await searchAirports('JFK', index, 3)
        console.log('Search results for "JFK":', testResults)
        // highlight-end
      } catch(err) {
        console.error("Error fetching airport data:", err)
      }
    }

    loadAirportData();

    return () => {
      mapRef.current.remove()
    }
  }, [])


return (
  <>
    <div style={{
        margin: '10px 10px 0 0',
        width: 300,
        right: 0,
        top: 0,
        position: 'absolute',
        zIndex: 10 }}>
        <SearchBoxContainer 
            mapRef={mapRef}
            airportIndex={airportIndex}/>
    </div>
    <div id='map-container' ref={mapContainerRef} />
  </>
  )
}

export default App

