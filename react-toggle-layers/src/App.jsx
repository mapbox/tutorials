import React, {useState} from 'react'
import Map from './Map'
import LayerCheckboxes from './LayerCheckboxes'

import './app.css'

export default function App() {

  // all layers are checked by default
  const [layerState, setLayerState] = useState([
    {
      name: 'vegetarian',
      color: '#33a02c',
      isChecked: true
    },
    {
      name: 'sandwich',
      color: '#ffff99',
      isChecked: true   
    },
    {
      name: 'asian',
      color: '#6a3d9a',
      isChecked: true
    },
    {
      name: 'american',
      color: '#a6cee3',
      isChecked: true     
    },
    {
      name: 'coffee',
      color: '#e31a1c',
      isChecked: true
    },
    {
      name: 'mexican',
      color: '#cab2d6',
      isChecked: true
    },
    {
      name: 'seafood',
      color: '#1f78b4',
      isChecked: true
    },
    {
      name: 'ice cream',
      color: '#fb9a99',
      isChecked: true
    },
    {
      name: 'korean',
      color: '#cab2d6',
      isChecked: true
    },
    {
      name: 'sushi',
      color: '#b2df8a',
      isChecked: true
    },
    {
      name: 'italian',
      color: '#ff7f00',
      isChecked: true
    }
  ])

  return (
    <div className="app">
      <LayerCheckboxes layerState={layerState} setLayerState={setLayerState} />
      <Map layerState={layerState} />
    </div>
  )
}
