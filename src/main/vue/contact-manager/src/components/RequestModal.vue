<template>
  <transition name="modal">
    <Modal :title="request.uuid" size="xl" scrollable @close="close">
      <div v-if="errorCode" class="alert alert-danger" role="alert">
        {{ $t(`digibib.contact.frontend.manager.error.${errorCode}`) }}
      </div>
      <div v-if="infoCode" class="alert alert-success" role="alert">
        {{ $t(`digibib.contact.frontend.manager.info.${infoCode}`) }}
      </div> <!-- TODO add warning if request is in received state -->
      <div class="form-row">
        <div class="form-group col-md-4">
          <label for="inputName">
            {{ $t('digibib.contact.frontend.manager.label.name') }}
          </label>
          <input type="text" class="form-control" id="inputName" v-model="request.name"
            disabled />
        </div>
        <div class="form-group col-md-5">
          <label for="inputEmail">
            {{ $t('digibib.contact.frontend.manager.label.email') }}
          </label>
          <input type="text" class="form-control" id="inputEmail" v-model="request.email"
            disabled />
        </div>
        <div class="form-group col-md-3">
          <label for="inputOrcid">
            {{ $t('digibib.contact.frontend.manager.label.orcid') }}
          </label>
          <input type="text" class="form-control" id="inputOrcid" v-model="request.orcid"
            disabled />
        </div>
      </div>
      <div class="form-row">
        <div class="col-md-12">
          <textarea class="form-control" rows="5" readonly v-model="request.message" />
        </div>
      </div>
      <hr class="my-4" />
      <h6 class="mb-2">
        {{ $t('digibib.contact.frontend.manager.label.recipients') }}
      </h6>
      <p>
        {{ $t('digibib.contact.frontend.manager.info.recipients') }}
      </p>
      <recipients-table />
      <template v-slot:footer>
        <div class="btn-group">
          <button type="button" class="btn btn-success" @click="forward"
              :disabled="isProcessed">
            {{ $t('digibib.contact.frontend.manager.button.forward') }}
          </button>
        </div>
      </template>
    </Modal>
  </transition>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import { useStore } from 'vuex';
import Modal from './Modal.vue';
import { RequestState } from '../utils';
import RecipientsTable from './RecipientsTable.vue';
import { ActionTypes } from '../store/modal/action-types';

const store = useStore();
const request: Request = computed(() => store.state.modal.currentRequest);
const errorCode = computed(() => store.state.modal.errorCode);
const infoCode = computed(() => store.state.modal.infoCode);
const close = () => {
  store.dispatch(`modal/${ActionTypes.HIDE_REQUEST}`);
};
const forward = () => {
  store.dispatch(`modal/${ActionTypes.FORWARD_REQUEST}`);
};
const isProcessed = computed(() => request.value.state !== RequestState.Processed);
</script>
<style>
.modal-enter-from {
  opacity: 0;
}
.modal-leave-to {
  opacity: 0;
}
.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  -webkit-transform: scale(1.02);
  transform: scale(1.02);
}
</style>
