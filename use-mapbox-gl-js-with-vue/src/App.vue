<template>
   <div id="sidebar">
    Longitude: {{ location.center.lng.toFixed(4) }} | Latitude:
    {{ location.center.lat.toFixed(4) }} | Zoom:
    {{ location.zoom.toFixed(2) }} |
    <button
      @click="
        location = {
          center: { lng: -71.05987, lat: 42.35982 },
          zoom: 14,
        }
      "
    >Reset
    </button>
  </div>
  <Map v-model="location" />
</template>

<script>
import Map from "./components/Map.vue";

export default {
  components: { Map },
  data() {
    return {
      location: {
        center: { lng: -71.05987, lat: 42.35982 },
        zoom: 14,
      },
    };
  },
};
</script>

<style>
#sidebar {
  background-color: rgb(35 55 75 / 90%);
  color: #fff;
  padding: 6px 12px;
  font-family: monospace;
  z-index: 1;
  position: absolute;
  top: 0;
  left: 0;
  margin: 12px;
  border-radius: 4px;
}
</style>