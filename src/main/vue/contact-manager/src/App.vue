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
      <div v-if="requests.length === 0" class="row">
        <div class="col">
          <div class="alert alert-warning text-center" role="alert">
            {{ $t('digibib.contact.frontend.manager.info.noRequests') }}
          </div>
        </div>
      </div>
      <div class="row">
        <div class="col">
          <OverviewTable :requests="requests" @error="handleError" />
        </div>
      </div>
      <div class="row">
        <div class="col d-flex justify-content-center">
          <Pagination :total-rows="totalRows" :per-page="perPage" :current-page="currentPage"
            @change="handlePageChange" />
        </div>
      </div>
    </template>
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useStore } from 'vuex';
import axios from 'axios';
import OverviewTable from './components/OverviewTable.vue';
import Pagination from './components/Pagination.vue';
import { ActionTypes } from './store/request/action-types';

const perPage = 8;

const store = useStore();
const requests = computed(() => store.getters['request/getRequests']);
const errorCode = ref(null);
const loading = ref(true);
const totalRows = computed(() => store.state.request.totalCount);
const currentPage = ref(0);
const isBooted = ref(false);
const handlePageChange = async (page) => {
  currentPage.value = page;
  try {
    await store.dispatch(`request/${ActionTypes.FETCH}`, {
      offset: page * perPage,
      limit: page * perPage + perPage,
    });
  } catch (error) {
    errorCode.value = error instanceof Error ? error.message : 'unknown';
  }
};
const handleError = (code) => {
  errorCode.value = code;
};
onMounted(async () => {
  let authError = false;
  if (process.env.NODE_ENV === 'development') {
    axios.defaults.headers.common.Authorization = 'Basic YWRtaW5pc3RyYXRvcjphbGxlc3dpcmRndXQ=';
  } else {
    try {
      const jwtResponse = await axios.get('rsc/jwt');
      const jwtToken = jwtResponse.data.access_token;
      axios.defaults.headers.common.Authorization = `Bearer ${jwtToken}`;
    } catch (error) {
      authError = true;
    }
  }
  if (!authError) {
    try {
      await store.dispatch(`request/${ActionTypes.FETCH}`, {
        offset: 0,
        limit: perPage,
      });
    } catch (error) {
      handleError(error instanceof Error ? error.message : 'unknown');
    }
  } else {
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
