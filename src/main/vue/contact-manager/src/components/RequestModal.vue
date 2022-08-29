<template>
  <transition name="modal">
    <Modal :title="state.id" size="xl" scrollable @close="close">
      <div> <!-- alert div -->
      </div>
      <div class="form-row">
        <div class="form-group col-md-4">
          <label for="inputName">
            {{ $t('digibib.contact.frontend.manager.label.name') }}
          </label>
          <input type="text" class="form-control" id="inputName" v-model="state.name"
            disabled />
        </div>
        <div class="form-group col-md-5">
          <label for="inputEmail">
            {{ $t('digibib.contact.frontend.manager.label.email') }}
          </label>
          <input type="text" class="form-control" id="inputEmail" v-model="state.email"
            disabled />
        </div>
        <div class="form-group col-md-3">
          <label for="inputOrcid">
            {{ $t('digibib.contact.frontend.manager.label.orcid') }}
          </label>
          <input type="text" class="form-control" id="inputOrcid" v-model="state.orcid"
            disabled />
        </div>
      </div>
      <div class="form-row">
        <div class="col-md-12">
          <textarea class="form-control" rows="5" readonly v-model="state.message" />
        </div>
      </div>
      <hr class="my-4" />
      <h6 class="mb-2">
        {{ $t('digibib.contact.frontend.manager.label.recipients') }}
      </h6>
      <p>
        {{ $t('digibib.contact.frontend.manager.info.recipients') }}
      </p>
      <recipients-table :recipients="state.recipients" />
      <template v-slot:footer>
        <div class="btn-group">
          <button type="button" class="btn btn-danger" @click="reject"
              :disabled="state.state !== 'PROCESSED'">
            {{ $t('digibib.contact.frontend.manager.button.reject') }}
          </button>
          <button type="button" class="btn btn-success" @click="forward"
              :disabled="state.state !== 'PROCESSED'">
            {{ $t('digibib.contact.frontend.manager.button.forward') }}
          </button>
        </div>
      </template>
    </Modal>
  </transition>
</template>
<script setup lang="ts">
import {
  reactive,
  onMounted,
} from 'vue';
import { useStore } from 'vuex';
import Modal from './Modal.vue';
import RecipientsTable from './RecipientsTable.vue';

const props = defineProps({
  size: String,
  id: String,
});
const store = useStore();
const state = reactive({
  id: '',
  name: '',
  email: '',
  orcid: '',
  created: '',
  state: '',
  message: '',
  objectID: '',
  recipients: [],
});
const setFields = () => {
  const request = store.getters.getRequestById(props.id);
  if (request) {
    state.id = request.uuid;
    state.name = request.name;
    state.email = request.email;
    state.orcid = request.orcid;
    state.created = new Date(request.created).toLocaleString();
    state.state = request.state;
    state.message = request.message;
    state.objectID = request.objectID;
    state.recipients = request.recipients;
  }
};
onMounted(() => {
  setFields();
});
const close = () => {
  store.commit('setModal', { show: false, id: undefined });
};
const forward = () => {
  store.dispatch('forwardContactRequest', state.id);
};
const reject = () => {
  store.dispatch('rejectContactRequest', state.id);
};
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
