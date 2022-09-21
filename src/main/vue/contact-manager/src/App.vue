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
    <template v-if="errorCode !== 'unauthorizedError'">
      <div v-if="requests.length === 0" class="row">
        <div class="col">
          <div class="alert alert-warning text-center" role="alert">
            {{ $t('digibib.contact.frontend.manager.info.noRequests') }}
          </div>
        </div>
      </div>
      <div class="row">
        <div class="col">
          <OverviewTable :requests="requests" />
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
import { computed } from 'vue';
import { useStore } from 'vuex';
import OverviewTable from './components/OverviewTable.vue';
import Pagination from './components/Pagination.vue';

const store = useStore();
const requests = computed(() => store.state.main.requests);
const errorCode = computed(() => store.state.main.errorCode);
const loading = computed(() => store.state.main.loading);
const totalRows = computed(() => store.state.main.totalRows);
const currentPage = computed(() => store.state.main.currentPage);
const perPage = computed(() => store.state.main.perPage);
const handlePageChange = (page) => {
  store.commit('main/SET_CURRENT_PAGE', page);
  store.dispatch('main/fetchData');
};
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
