<template>
  <loading-overlay :loading="loading" />
  <div class="container-fluid">
    <div v-if="errorCode" class="row">
      <div class="col">
        <div class="alert alert-danger text-center" role="alert">
          {{ $t(`digibib.contact.frontend.manager.error.${errorCode}`) }}
        </div>
      </div>
    </div>
    <template v-if="isBooted && errorCode !== 'unauthorizedError'">
      <div v-if="totalCount === 0" class="row">
        <div class="col">
          <div class="alert alert-warning text-center" role="alert">
            {{ $t('digibib.contact.frontend.manager.info.noRequests') }}
          </div>
        </div>
      </div>
      <div class="row">
        <div class="col">
          <OverviewTable @error="handleError" />
        </div>
      </div>
      <div class="row">
        <div class="col d-flex justify-content-center">
          <b-pagination :total-rows="totalCount" :per-page="limit" v-model="currentPage"
            @change="handlePageChange" />
        </div>
      </div>
    </template>
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import OverviewTable from '@/components/OverviewTable.vue';
import LoadingOverlay from '@/components/LoadingOverlay.vue';
import { useApplicationStore, useAuthStore } from '@/stores';

const store = useApplicationStore();
const limit = computed(() => store.limit);
const totalCount = computed(() => store.totalCount);
const authStore = useAuthStore();
const errorCode = ref(null);
const loading = ref(true);
const isBooted = ref(false);
const currentPage = computed(() => Math.floor(store.offset / store.limit + 1));
const handleError = (code) => {
  errorCode.value = code;
};
const handlePageChange = async (page) => {
  currentPage.value = page;
  try {
    await store.fetch();
  } catch (error) {
    handleError(error instanceof Error ? error.message : 'unknown');
  }
};
onMounted(async () => {
  try {
    if (process.env.NODE_ENV === 'production') {
      await authStore.login();
    }
    await store.fetch();
  } catch (error) {
    handleError(error instanceof Error ? error.message : 'unknown');
  } finally {
    isBooted.value = true;
    loading.value = false;
  }
});
</script>
