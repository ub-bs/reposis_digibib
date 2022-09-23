<template>
  <table class="table table-striped">
    <thead>
      <tr>
        <th scope="col" class="col-3">
          {{ $t('digibib.contact.frontend.manager.label.id') }}
        </th>
        <th scope="col" class="col-2">
          {{ $t('digibib.contact.frontend.manager.label.created') }}
        </th>
        <th scope="col" class="col-2">
          {{ $t('digibib.contact.frontend.manager.label.objectID') }}
        </th>
        <th scope="col" class="col-3">
          {{ $t('digibib.contact.frontend.manager.label.email') }}
        </th>
        <th scope="col" class="col-1">
          {{ $t('digibib.contact.frontend.manager.label.state') }}
        </th>
        <th scope="col" class="col-1">
        </th>
      </tr>
    </thead>
    <tbody v-if="requests">
      <tr v-for="item in requests" :key="item">
        <td class="col-3 align-middle">
          {{ item.uuid }}
        </td>
        <td class="col-2 align-middle">
          {{ new Date(item.created).toLocaleString() }}
        </td>
        <td class="col-2 align-middle">
          <a :href="'receive/' + item.objectID" target="_blank">
            {{ item.objectID }}
          </a>
        </td>
        <td class="col-3 align-middle">
          {{ item.email }}
        </td>
        <td class="col-1 align-middle">
          {{ RequestState.toString(item.state) }}
        </td>
        <td class="col-1 align-middle">
          <div class="btn-group">
            <button class="btn shadow-none pt-0 pb-0 pr-1 pl-2" @click="viewRequest(item.uuid)">
              <i class="fa fa-eye"></i>
            </button>
            <button class="btn shadow-none pt-0 pb-0 pl-1 pr-2" @click="removeRequest(item.uuid)"
              :disabled="item.state > RequestState.Processed">
              <i class="fa fa-trash"></i>
            </button>
          </div>
        </td>
      </tr>
    </tbody>
  </table>
  <RequestModal v-if="showRequestModal" />
  <ConfirmModal ref="confirmModal" />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useStore } from 'vuex';
import { useI18n } from 'vue-i18n';
import ConfirmModal from './ConfirmModal.vue';
import RequestModal from './RequestModal.vue';
import { RequestState } from '../utils';
import { ActionTypes as MainActionTypes } from '../store/main/action-types';
import { ActionTypes as ModalActionTypes } from '../store/modal/action-types';

defineProps({
  requests: {
    required: true,
  },
});
const store = useStore();
const { t } = useI18n();
const confirmModal = ref(null);
const showRequestModal = computed(() => store.state.modal.showRequestModal);
const viewRequest = (id: string) => {
  const request = store.getters['main/getRequestById'](id);
  store.dispatch(`modal/${ModalActionTypes.SHOW_REQUEST}`, request);
};
const removeRequest = async (id: string) => {
  const ok = await confirmModal.value.show({
    title: t('digibib.contact.frontend.manager.confirm.deleteRequest.title'),
    message: t('digibib.contact.frontend.manager.confirm.deleteRequest.message', {
      requestID: id,
    }),
  });
  if (ok) {
    store.dispatch(`main/${MainActionTypes.REMOVE_REQUEST}`, id);
  }
};
</script>
