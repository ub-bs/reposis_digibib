<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col d-flex justify-content-center">
        <h3>{{ $t('digibib.contact.frontend.manager.title') }}</h3>
      </div>
    </div>
    <!-- <div v-if="showAlert" class="row">
      <div class="col">
        <div class="alert alert-danger text-center" role="alert">
          {{ alertMessage }}
        </div>
      </div>
    </div> -->
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
  </div>
</template>
<script setup lang="ts">
import OverviewTable from './components/OverviewTable.vue';
import Pagination from './components/Pagination.vue';
</script>
