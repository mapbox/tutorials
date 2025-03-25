import React, { useEffect, useRef, useState, useCallback } from 'react'
import mapboxgl from 'mapbox-gl'

import Marker from './Marker'
import Popup from './Popup'

import 'mapbox-gl/dist/mapbox-gl.css';
import './App.css'


function Map() {

    const mapRef = useRef()
    const mapContainerRef = useRef()

    const [earthquakeData, setEarthquakeData] = useState()
    const [activeFeature, setActiveFeature] = useState()

    const getBboxAndFetch = useCallback(async () => {
        const bounds = mapRef.current.getBounds()

        try {
            const data = await fetch(`https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2024-01-01&endtime=2024-01-30&minlatitude=${bounds._sw.lat}&maxlatitude=${bounds._ne.lat}&minlongitude=${bounds._sw.lng}&maxlongitude=${bounds._ne.lng}`)
                .then(d => d.json())

            setEarthquakeData(data)
        } catch (error) {
            console.error(error)
        }
    }, [])

    useEffect(() => {
        mapboxgl.accessToken = 'YOUR_MAPBOX_ACCESS_TOKEN'
        mapRef.current = new mapboxgl.Map({
            container: mapContainerRef.current,
            center: [124, -1.98],
            minZoom: 5.5,
            zoom: 5.5
        });

        mapRef.current.on('load', () => {
            getBboxAndFetch()
        })

        mapRef.current.on('moveend',  () => {
            getBboxAndFetch()
        })

        return () => {
            mapRef.current.remove()
        }
    }, [])

    const handleMarkerClick = (feature) => {
        setActiveFeature(feature)
    }

    return (
        <>
            <div id='map-container' ref={mapContainerRef} />
            {mapRef.current && earthquakeData && earthquakeData.features?.map((feature) => {
                return (
                    <Marker
                        key={feature.id}
                        map={mapRef.current}
                        feature={feature}
                        isActive={activeFeature?.id === feature.id}
                        onClick={handleMarkerClick}
                    />
                )
            })}
            {mapRef.current && (
                <Popup
                    map={mapRef.current}
                    activeFeature={activeFeature}
                />
            )}
        </>
    )
}

export default Map