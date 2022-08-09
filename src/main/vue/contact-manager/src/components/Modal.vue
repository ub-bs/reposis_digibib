<template>
  <transition name="modal">
    <div class="modal-mask">
      <div class="modal-wrapper" @click="close">
        <div class="modal-dialog" :class="'modal-' + props.size"  role="document" @click.stop="">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">
                {{ state.id }}
              </h5>
              <button type="button" class="close" aria-label="Close">
                <span aria-hidden="true" @click="close">&times;</span>
              </button>
            </div>
            <div class="modal-body">
              <div class="form-row">
                <div class="form-group col-md-4">
                  <label for="inputName">
                    {{ t('digibib.contact.frontend.manager.label.name') }}
                  </label>
                  <input type="text" class="form-control" id="inputName" v-model="state.name"
                    disabled />
                </div>
                <div class="form-group col-md-5">
                  <label for="inputEmail">
                    {{ t('digibib.contact.frontend.manager.label.email') }}
                  </label>
                  <input type="text" class="form-control" id="inputEmail" v-model="state.email"
                    disabled />
                </div>
                <div class="form-group col-md-3">
                  <label for="inputOrcid">
                    {{ t('digibib.contact.frontend.manager.label.orcid') }}
                  </label>
                  <input type="text" class="form-control" id="inputOrcid" v-model="state.orcid"
                    disabled />
                </div>
              </div>
              <hr class="my-4" />
              <h6>
                {{ t('digibib.contact.frontend.manager.label.recipients') }}
              </h6>
              <table class="table table-striped table-bordered">
                <thead>
                  <tr>
                    <th class="col-3">
                      {{ t('digibib.contact.frontend.manager.label.name') }}
                    </th>
                    <th class="col-3">
                      {{ t('digibib.contact.frontend.manager.label.source') }}
                    </th>
                    <th class="col-6">
                      {{ t('digibib.contact.frontend.manager.label.email') }}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in state.recipients" :key="item">
                    <td>
                      {{ item.name }}
                    </td>
                    <td>
                      {{ item.source }}
                    </td>
                    <td>
                      {{ item.email }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div class="modal-footer">
              <button type="button" @click="close" class="btn btn-secondary">
                {{ t('digibib.contact.frontend.manager.button.close') }}
              </button>
              <div class="btn-group">
                <button type="button" class="btn btn-danger" @click="reject"
                    :disabled="state.state !== 'PROCESSED'">
                  {{ t('digibib.contact.frontend.manager.button.reject') }}
                </button>
                <button type="button" class="btn btn-success" @click="forward"
                    :disabled="state.state !== 'PROCESSED'">
                  {{ t('digibib.contact.frontend.manager.button.forward') }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import {
  reactive,
  onMounted,
} from 'vue';
import { useI18n } from 'vue-i18n';
import { useStore } from 'vuex';

interface Props {
  size: string,
  id: string,
}
const props = defineProps<Props>();
const { t } = useI18n();
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
.modal-mask {
  position: fixed;
  z-index: 9998;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: table;
  transition: opacity 0.3s ease;
}

.modal-wrapper {
  display: table-cell;
  vertical-align: middle;
}

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
