<template>
  <b-table :fields="fields" :items="requests" sort-icon-left responsive striped>
    <template #cell(objectID)="data">
      <a :href="`${getWebApplicationBaseURL()}receive/${data.item.objectID}`" target="_blank">
        {{ data.item.objectID }}
      </a>
    </template>
    <template #cell(requester)="data">
      {{ data.item.name + ' (' + data.item.email + ')' }}
    </template>
    <template #cell(edit)="data">
      <div class="btn-group">
        <button v-b-modal.request-modal class="btn shadow-none pt-0 pb-0 pr-1 pl-2"
          @click="viewRequest(data.item)">
          <i class="fa fa-eye"></i>
        </button>
        <button class="btn shadow-none pt-0 pb-0 pl-1 pr-2" @click="removeRequest(data.item.uuid)"
          :disabled="data.item.state > RequestState.Processed">
          <i class="fa fa-trash"></i>
        </button>
      </div>
    </template>
  </b-table>
  <RequestModal />
</template>

<script setup lang="ts">
import { Component, computed, getCurrentInstance } from 'vue';
import { useI18n } from 'vue-i18n';
import { BvModal, BTable } from 'bootstrap-vue';
import {
  getI18nKey,
  Request,
  RequestState,
  getWebApplicationBaseURL,
} from '@/utils';
import { useApplicationStore, useRequestStore } from '@/stores';
import RequestModal from './RequestModal.vue';

const instance: Component = getCurrentInstance();
const store = useApplicationStore();
const requestStore = useRequestStore();
const { t } = useI18n();
const tc = (value: string, obj?) => t(getI18nKey(value), obj);
const requests = computed(() => store.requests);
const fields = [
  {
    key: 'uuid',
    thClass: 'col-3 text-center',
    tdClass: 'col-3 text-center align-middle',
    label: tc('label.id'),
  },
  {
    key: 'objectID',
    thClass: 'col-2 text-center',
    tdClass: 'col-2 text-center align-middle',
    label: tc('label.objectID'),
  },
  {
    key: 'created',
    thClass: 'col-1 text-center',
    tdClass: 'col-1 text-center align-middle',
    label: tc('label.created'),
    formatter: (value) => ((value) ? new Date(value).toLocaleDateString() : '-'),
  },
  {
    key: 'forwarded',
    thClass: 'col-1 text-center',
    tdClass: 'col-1 text-center align-middle',
    label: tc('label.forwarded'),
    formatter: (value) => ((value) ? new Date(value).toLocaleDateString() : '-'),
  },
  {
    key: 'state',
    thClass: 'col-1 text-center',
    tdClass: 'col-1 text-center align-middle',
    label: tc('label.state'),
    formatter: (value) => ((value) ? RequestState.toString(value) : '-'),
  },
  {
    key: 'requester',
    thClass: 'col-3 text-center',
    tdClass: 'col-3 text-center align-middle',
    label: tc('label.requester'),
  },
  {
    key: 'edit',
    thClass: 'col-1 text-right',
    tdClass: 'col-1 text-right align-middle',
    label: '',
  },
];
const viewRequest = (request: Request) => {
  requestStore.request = request;
  requestStore.showModal = true;
};
const removeRequest = async (id: string) => {
  const bvModal = instance.ctx._bv__modal as BvModal;
  const value = await bvModal.msgBoxConfirm(tc('confirm.deleteRequest.message', {
    requestID: id,
  }), {
    title: tc('confirm.deleteRequest.title'),
  });
  if (value) {
    try {
      await store.removeRequest(id);
      bvModal.msgBoxOk(tc('info.deleteRequestSuccess'), {
        title: tc('confirm.deleteRequest.title'),
      });
    } catch (error) {
      bvModal.msgBoxOk(tc('error.deleteRequestFailed'), {
        title: t('confirm.deleteRequest.title'),
        okVariant: 'danger',
      });
    }
  }
};
</script>
