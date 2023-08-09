<template>
  <table class="table table-striped">
    <colgroup>
      <col style="width: 20%">
      <col style="width: 11%">
      <col style="width: 12%">
      <col style="width: 12%">
      <col style="width: 7%">
      <col style="width: 50%">
      <col style="width: 5%">
    </colgroup>
    <thead>
      <tr>
        <th scope="col">
          {{ $t('digibib.contact.frontend.manager.label.id') }}
        </th>
        <th scope="col">
          {{ $t('digibib.contact.frontend.manager.label.objectID') }}
        </th>
        <th scope="col">
          {{ $t('digibib.contact.frontend.manager.label.created') }}
        </th>
        <th scope="col">
          {{ $t('digibib.contact.frontend.manager.label.forwarded') }}
        </th>
        <th scope="col">
          {{ $t('digibib.contact.frontend.manager.label.state') }}
        </th>
        <th scope="col">
          {{ $t('digibib.contact.frontend.manager.label.requester') }}
        </th>
        <th scope="col">
        </th>
      </tr>
    </thead>
    <tbody v-if="requests">
      <tr v-for="item in requests" :key="item">
        <td class="align-middle">
          {{ item.uuid }}
        </td>
        <td class="align-middle">
          <a :href="'receive/' + item.objectID" target="_blank">
            {{ item.objectID }}
          </a>
        </td>
        <td class="align-middle">
          {{ new Date(item.created).toLocaleString() }}
        </td>
        <td class="align-middle">
          {{ item.forwarded != null ? new Date(item.forwarded).toLocaleString() : '-' }}
        </td>
        <td class="align-middle">
          {{ RequestState.toString(item.state) }}
        </td>
        <td class="align-middle">
          {{ item.name + ' (' + item.email + ')' }}
        </td>
        <td class="align-middle">
          <div class="btn-group">
            <button v-b-modal.request-modal class="btn shadow-none pt-0 pb-0 pr-1 pl-2"
              @click="viewRequest(item)">
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
  <RequestModal />
</template>

<script setup lang="ts">
import { computed, getCurrentInstance } from 'vue';
import { useStore } from 'vuex';
import { useI18n } from 'vue-i18n';
import { BvModal } from 'bootstrap-vue';
import RequestModal from './RequestModal.vue';
import { Request, RequestState } from '../utils';
import { ActionTypes } from '../store/request/action-types';

const emit = defineEmits(['error']);
const store = useStore();
const { t } = useI18n();
const requests = computed(() => store.getters['request/getRequests']);
const viewRequest = async (request: Request) => {
  await store.dispatch(`request/${ActionTypes.SET_MODAL_DATA}`, request);
};
// eslint-disable-next-line
const instance = (getCurrentInstance() as any);
const removeRequest = async (id: string) => {
  const bvModal = instance.ctx._bv__modal as BvModal;
  bvModal.msgBoxConfirm(t('digibib.contact.frontend.manager.confirm.deleteRequest.message', {
    requestID: id,
  }), {
    title: t('digibib.contact.frontend.manager.confirm.deleteRequest.title'),
  }).then((value) => {
    if (value) {
      store.dispatch(`request/${ActionTypes.REMOVE_REQUEST}`, id);
    }
  }).catch((error) => {
    emit('error', error instanceof Error ? error.message : 'unknown');
  });
};
</script>
