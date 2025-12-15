export function buildAirportIndex(airportData) {
  const iataIndex = new Map();
  
  if (!airportData?.features) return iataIndex;
  
  for (const feature of airportData.features) {
    const iata = feature.properties.IDENT;
    if (iata) {
      // Only index 2-char, 3-char, and 4-char prefixes
      for (let i = 2; i <= Math.min(iata.length, 4); i++) {
        const prefix = iata.substring(0, i);
        if (!iataIndex.has(prefix)) {
          iataIndex.set(prefix, []);
        }
        iataIndex.get(prefix).push(feature);
      }
    }
  }
  
  return iataIndex;
}

export async function searchAirports(query, iataIndex, maxResults = 5) {
  if(!query || query.length < 2) return []; // Only search if query is 2+ chars

  const q = query.toUpperCase().trim()
  console.log('q', q)
  // Use index for IATA lookup (super fast)
  if(q.length <= 4 && iataIndex) {
    const matches = iataIndex.get(q) || []
    
    return matches  
      .slice(0, maxResults)
      .map(formatAirportResult)
  }
  
  // For longer queries, no airport search (could add name/city search later)
  return [];
}

function formatAirportResult(feature) {
  const props = feature.properties;
  return {
    name: `${props.IDENT} - ${props.NAME}`,
    place_formatted: `${props.SERVCITY}, ${props.STATE}`,
    mapbox_id: `airport_${props.IDENT}`,
    feature_type: 'airport',
    coordinates: feature.geometry.coordinates,
    original_data: feature
  }
}