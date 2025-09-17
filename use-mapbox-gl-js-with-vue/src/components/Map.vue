<template>
  <div ref="mapContainer" class="map-container"></div>
</template>

<script>
import mapboxgl from "mapbox-gl";
import "mapbox-gl/dist/mapbox-gl.css";

mapboxgl.accessToken = "YOUR_MAPBOX_ACCESS_TOKEN";

export default {
  props: ["modelValue"],
  mounted() {
    const { center, zoom } = this.modelValue

    const map = new mapboxgl.Map({
      container: this.$refs.mapContainer,
      style: "mapbox://styles/mapbox/standard",
      center,
      zoom,
    });

     // function to update the modelValue prop with the map's current location
    const updateLocation = () => this.$emit("update:modelValue", this.getLocation());

    // add event listeners to update the location on map move and zoom
    map.on("move", updateLocation);
    map.on("zoom", updateLocation);

    // assign the map instance to this component's map property
    this.map = map;
  },

  // clean up the map instance when the component is unmounted
  unmounted() {
    this.map.remove();
    this.map = null;
  },
  // watch for external changes to the modelValue prop and update the map accordingly
  watch: {
    modelValue(next) {
      const curr = this.getLocation();

      // Only flyTo if any of the values have changed
      if (
        curr.center.lng !== next.center.lng ||
        curr.center.lat !== next.center.lat ||
        curr.zoom !== next.zoom
      ) {
        this.map.flyTo({
          center: next.center,
          zoom: next.zoom,
        });
      }
    },
  },
  methods: {
    getLocation() {
      return {
        center: this.map.getCenter(),
        zoom: this.map.getZoom(),
      };
    },
  }
};
</script>

<style>
/* make the map container fill its parent */
.map-container {
  width: 100%;
  height: 100%;
}
</style>