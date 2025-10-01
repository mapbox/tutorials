import React from 'react'

import './app.css'

// creates a series of checkboxes to toggle layers on and off. One for each type of food and one to toggle on and off all layers.
const LayerCheckboxes = ({ layerState, setLayerState }) => {

    // Handles when a layer checkbox is toggled, turning related layers visible when toggled on and off.
    const handleCheckboxChange = (event) => {
        const { name, checked } = event.target
        setLayerState(prevLayers => {
            return prevLayers.map(layer =>
                layer.name === name ? { ...layer, isChecked: checked } : layer
            )
        })
    }

    // Handles when "All" checkbox is toggled, turning all layers on or off.
    const handleAllCheckboxChange = (event) => {
        const { checked } = event.target
        setLayerState(prevLayers => {
            return prevLayers.map(layer => ({ ...layer, isChecked: checked }))
        })
    }

    const checkedCount = layerState.filter(layer => layer.isChecked).length
    const allChecked = checkedCount === layerState.length
    const someChecked = checkedCount > 0 && checkedCount < layerState.length

    return (
        <div className="map-controls">
            {/* "All" checkbox with indeterminate state */}
            <div className="checkbox">
                <input
                    type="checkbox"
                    className="checkbox-input"
                    name="all"
                    checked={allChecked}
                    ref={input => {
                        if (input) input.indeterminate = someChecked
                    }}
                    onChange={handleAllCheckboxChange}
                />
                <label className="checkbox-label" htmlFor="all">
                    <span className="checkbox-inner" />
                    <span className="checkbox-switch" />
                </label>
                All
            </div>
            {layerState.map((layer) => (
                <div key={layer.name} className="checkbox">
                    <input
                        type="checkbox"
                        className="checkbox-input"
                        name={layer.name}
                        checked={layer.isChecked}
                        onChange={handleCheckboxChange}
                    />
                    <label className="checkbox-label" htmlFor={layer.name}>
                        <span className="checkbox-inner" />
                        <span className="checkbox-switch" />
                    </label>
                    <div 
                        className="color-indicator"
                        style={{
                            width: '12px',
                            height: '12px',
                            borderRadius: '50%',
                            backgroundColor: layer.isChecked ? layer.color : '#ccc',
                            marginRight: '8px'
                        }}
                    />
                    <span style={{ color: layer.isChecked ? 'inherit' : '#ccc' }}>
                        {layer.name}
                    </span>
                </div>
            ))}
        </div>
    )
}

export default LayerCheckboxes