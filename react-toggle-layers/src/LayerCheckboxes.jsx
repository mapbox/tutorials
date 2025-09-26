import React from 'react'

const LayerCheckboxes = ({ layerState, setLayerState }) => {

    const handleCheckboxChange = (event) => {
        const { name, checked } = event.target
        setLayerState(prevLayers => {
            return prevLayers.map(layer =>
                layer.name === name ? { ...layer, isChecked: checked } : layer
            )
        })
    }

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
            <div className="toggle-switch">
                <input
                    type="checkbox"
                    className="toggle-switch-checkbox"
                    name="all"
                    checked={allChecked}
                    ref={input => {
                        if (input) input.indeterminate = someChecked
                    }}
                    onChange={handleAllCheckboxChange}
                />
                <label className="toggle-switch-label" htmlFor="all">
                    <span className="toggle-switch-inner" />
                    <span className="toggle-switch-switch" />
                </label>
                All
            </div>
            {layerState.map((layer) => (
                <div key={layer.name} className="toggle-switch">
                    <input
                        type="checkbox"
                        className="toggle-switch-checkbox"
                        name={layer.name}
                        checked={layer.isChecked}
                        onChange={handleCheckboxChange}
                    />
                    <label className="toggle-switch-label" htmlFor={layer.name}>
                        <span className="toggle-switch-inner" />
                        <span className="toggle-switch-switch" />
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