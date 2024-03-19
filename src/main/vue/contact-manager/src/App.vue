<template>
  <div v-if="loading" class="overlay">
    <div class="d-flex justify-content-center">
      <div class="spinner-grow text-primary" role="status"
          style="width: 3rem; height: 3rem; z-index: 20;">
        <span class="sr-only">Loading...</span>
      </div>
    </div>
  </div>
  <div class="container-fluid">
    <div v-if="errorCode" class="row">
      <div class="col">
        <div class="alert alert-danger text-center" role="alert">
          {{ $t(`digibib.contact.frontend.manager.error.${errorCode}`) }}
        </div>
      </div>
    </div>
    <template v-if="isBooted && errorCode !== 'unauthorizedError'">
      <div class="row">
        <div class="col">
          <OverviewTable @error="handleError" />
        </div>
      </div>
    </template>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from 'axios';
import OverviewTable from './components/OverviewTable.vue';

const errorCode = ref();
const loading = ref(true);
const isBooted = ref(false);

const handleError = (code: string) => {
  errorCode.value = code;
};
onMounted(async () => {
  let authError = false;
  if (process.env.NODE_ENV === 'development') {
    axios.defaults.headers.common.Authorization = `Basic ${process.env.VUE_APP_API_TOKEN}`;
  } else {
    try {
      const jwtResponse = await axios.get('rsc/jwt');
      const jwtToken = jwtResponse.data.access_token;
      axios.defaults.headers.common.Authorization = `Bearer ${jwtToken}`;
    } catch (error) {
      authError = true;
    }
  }
  if (authError) {
    errorCode.value = 'unknown';
  }
  isBooted.value = true;
  loading.value = false;
});
</script>

<style scoped>
.overlay {
  position: fixed;
  width: 100%;
  height: 100%;
  z-index: 1000;
  top: 40%;
  left: 0px;
  opacity: 0.5;
  filter: alpha(opacity=50);
}
</style>
