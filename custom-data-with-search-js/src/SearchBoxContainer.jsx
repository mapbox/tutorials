import React, { useState, useEffect, useRef } from "react"
import { SearchBoxCore, SearchSession } from "@mapbox/search-js-core"
import { searchAirports } from "./utils/search"
import SearchBox from "./SearchBox"
import mapboxgl from 'mapbox-gl'

const SearchBoxContainer = ({mapRef, airportIndex}) => {
  const [searchInput, setSearchInput] = useState('')
  const [suggestions, setSuggestions] = useState([])
  const [selectedResult, setSelectedResult] = useState(null)
  const sessionRef = useRef(null)
  const markerRef = useRef(null)

  const handleChange = (e) => {
    setSearchInput(e.target.value)
  }

  useEffect(() => {
    // Initialize Search Core and Session
    const search = new SearchBoxCore({ 
      accessToken: import.meta.env.VITE_MAPBOX_ACCESS_TOKEN 
    })
    const session = new SearchSession(search)
    
    sessionRef.current = session
    // Instiantiate a Mapbox Marker to attach to selected results (see useEffect below)
    markerRef.current = new mapboxgl.Marker()
  }, [])

  useEffect(() => {
    if(!searchInput) {
      setSuggestions([])
      return
    }

    // Debounce search - wait 300ms after user stops typing
    const timeoutId = setTimeout(async () => {
      try {
        // Search both sources in parallel
        const [searchBoxResults, airportResults] = await Promise.all([
          sessionRef.current.suggest(searchInput, {
            types: ['address', 'place', 'street', 'poi', 'city', 'locality', 'country']
          }),
          searchAirports(searchInput, airportIndex, 5)
        ])
        
        console.log("airportResults", airportResults)
        if (searchBoxResults?.suggestions.length === 0 && airportResults.length === 0) {
          setSuggestions([])
          return
        }

        // Merge results: airports first, then Mapbox results
        const combined = [
          ...(airportResults || []),
          ...(searchBoxResults?.suggestions || [])
        ]

        setSuggestions(combined)
      } catch(err) {
        console.error("Search error:", err)
      }
    }, 300)

    return () => clearTimeout(timeoutId)
  }, [searchInput, airportIndex])

  useEffect(() => {
    if(!selectedResult) return

    async function retrieveSuggestion() {
   let feature
      // Handle airport result click - match structure of SearchBox Retrieve response
      if(selectedResult.feature_type == 'airport') {
        feature =  {
          type: 'Feature',
          properties: selectedResult,
          geometry: {
            type: 'Point',
            coordinates: selectedResult.coordinates
          }
        }
      } else {
        // Handle retrieve suggestion from Mapbox result
        if(sessionRef.current.canRetrieve(selectedResult)) {
        const { features } = await sessionRef.current.retrieve(selectedResult);
        feature = features[0]
        }
      }

      // Fly map to selectedResult
      mapRef.current.flyTo({
        center: feature.geometry.coordinates,
        zoom: 16
      })

      // Create a marker and add it to the map.
      markerRef.current.setLngLat(feature.geometry.coordinates).addTo(mapRef.current);
    }
    
    retrieveSuggestion()
    setSearchInput('') // Clear input after selection
  }, [selectedResult])

  return (
    <div>
      <SearchBox 
        searchInput={searchInput} 
        handleChange={handleChange}
        suggestions={suggestions} 
        setSelectedResult={setSelectedResult}
        />
    </div>
  )
}

export default SearchBoxContainer