import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import mapboxgl from "mapbox-gl";

const MarkerSVG = () => (
    <svg width="32" height="40" viewBox="0 0 88 106" fill="none" xmlns="http://www.w3.org/2000/svg">
        <g filter="url(#filter0_d_2001_2)">
            <path d="M84.5254 40.7407C84.5254 63.2412 54.0169 100 43.8475 100C32.7535 100 3.16949 63.2412 3.16949 40.7407C3.16949 18.2403 21.3816 0 43.8475 0C66.3133 0 84.5254 18.2403 84.5254 40.7407Z" fill="#43538D" />
        </g>
        <circle cx="43.8983" cy="40.8983" r="33.8983" fill="#6B82D6" />
        <defs>
            <filter id="filter0_d_2001_2" x="0.169495" y="0" width="87.3559" height="106" filterUnits="userSpaceOnUse" colorInterpolationFilters="sRGB">
                <feFlood floodOpacity="0" result="BackgroundImageFix" />
                <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha" />
                <feOffset dy="3" />
                <feGaussianBlur stdDeviation="1.5" />
                <feComposite in2="hardAlpha" operator="out" />
                <feColorMatrix type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.25 0" />
                <feBlend mode="normal" in2="BackgroundImageFix" result="effect1_dropShadow_2001_2" />
                <feBlend mode="normal" in="SourceGraphic" in2="effect1_dropShadow_2001_2" result="shape" />
            </filter>
        </defs>
    </svg>
)

// marker component receives a Map() instance, a GeoJSON point feature representing a Point of Interest, and a category string to determine which icon to display in the marker
const POIMarker = ({ map, feature, category }) => {
    const { geometry, properties } = feature;

    // ref for the Marker() instance, used to manage the marker lifecycle and remove it from the map
    const markerRef = useRef(null);
    // store the category in a state variable to use only its initial value
    const [emojiCategory] = useState(category);

    // refs for the DOM nodes to be used for the marker and popup content
    const markerContentRef = useRef(document.createElement("div"));
    const popupContentRef = useRef(document.createElement("div"));

    // when the component mounts, create a new marker and popup and add them to the map
    useEffect(() => {
        // instantiate a new Mapbox Popup()
        const popup = new mapboxgl.Popup({
            closeButton: false,
            closeOnMove: true,
            offset: 40,
        })
            .setDOMContent(popupContentRef.current) // use the popupContentRef as the content of the popup

        // instantiate a new Mapbox Marker()
        markerRef.current = new mapboxgl.Marker(markerContentRef.current, { // use the markerContentRef as the content of the marker
            anchor: "bottom",
        })
            .setLngLat(geometry.coordinates) // set the marker's position using the coordinates from the feature
            .setPopup(popup) // set the popup to be displayed when the marker is clicked
            .addTo(map); // add the marker to the map

        // cleanup function: remove marker when component unmounts
        return () => {
            markerRef.current.remove();
        };
    }, []);

    // assign an emoji based on the category
    let emoji;
    switch (emojiCategory) {
        case "restaurant":
            emoji = "🍽️";
            break;
        case "coffee":
            emoji = "☕";
            break;
        case "bar":
            emoji = "🍸";
            break;
        case "hotel":
            emoji = "🏨";
            break;
        case "museum":
            emoji = "🏛️";
            break;
        default:
            emoji = "📍"; // default icon if category doesn't match known types
    }

    // render nothing directly into the component tree,
    // instead, use React portals to inject content into the DOM nodes displayed in the marker and popup.
    return (
        <>
            {/* Portal 1: Popup content, rendered into the DOM node for the popup */}
            {createPortal(
                <div>
                    <div className="popup-title">{properties.name}</div>
                    <div className="popup-address">{properties.full_address}</div>
                </div>,
                popupContentRef.current
            )}

            {/* Portal 2: Marker content, rendered into the DOM node for the marker */}
            {createPortal(
                <>
                    <MarkerSVG />
                    <div className="marker-emoji">{emoji}</div>
                </>,
                markerContentRef.current
            )}
        </>
    );
};

export default POIMarker;
