<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col d-flex justify-content-center">
        <h3>{{ $t('digibib.contact.frontend.manager.title') }}</h3>
      </div>
    </div>
    <div v-if="errorCode === 'unauthorizedError'" class="row">
      <div class="col">
        <div class="alert alert-danger text-center" role="alert">
          {{ $t(`digibib.contact.frontend.manager.error.${errorCode}`) }}
        </div>
      </div>
    </div>
    <template v-else>
      <div v-if="errorCode" class="row">
        <div class="col">
          <div class="alert alert-danger text-center" role="alert">
            {{ $t(`digibib.contact.frontend.manager.error.${errorCode}`) }}
          </div>
        </div>
      </div>
      <div class="row">
        <div class="col">
          <OverviewTable />
        </div>
      </div>
      <div class="row">
        <div class="col d-flex justify-content-center">
          <Pagination />
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
const errorCode = computed(() => store.state.applicationErrorCode);
</script>
