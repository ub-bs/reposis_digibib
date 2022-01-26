<template>
  <div>
    <button @click="openModal">Test</button>
    <Modal ref="modal" />
    <!-- <div v-if="showAlert" class="row">
      <div class="col-12">
        <div class="alert alert-danger text-center" role="alert">
          {{ alertMessage }}
        </div>
      </div>
    </div> -->
    <div class="row">
      <div class="col-12">
        <Table />
      </div>
    </div>
    <div class="row mt-3">
      <div class="col-12 d-flex justify-content-center">
        <Pagination />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Table from './components/Table.vue';
import Pagination from './components/Pagination.vue';
import Modal from './components/Modal.vue';

const modal = ref<InstanceType<typeof Modal> | null>(null);
function openModal() {
  modal.value?.show();
}
</script>
