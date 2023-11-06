<template>
  <b-table :fields="fields" :items="requests" sort-icon-left responsive striped>
    <template #cell(objectID)="data">
      <a :href="'receive/' + data.item.objectID" target="_blank">
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
import { Request, RequestState } from '@/utils';
import { useApplicationStore, useRequestStore } from '@/stores';
import RequestModal from './RequestModal.vue';

const emit = defineEmits(['error']);
const instance: Component = getCurrentInstance();
const store = useApplicationStore();
const requestStore = useRequestStore();
const { t } = useI18n();
const requests = computed(() => store.requests);
const fields = [
  {
    key: 'uuid',
    thClass: 'col-3 text-center',
    tdClass: 'col-3 text-center align-middle',
    label: t('digibib.contact.frontend.manager.label.id'),
  },
  {
    key: 'objectID',
    thClass: 'col-2 text-center',
    tdClass: 'col-2 text-center align-middle',
    label: t('digibib.contact.frontend.manager.label.objectID'),
  },
  {
    key: 'created',
    thClass: 'col-1 text-center',
    tdClass: 'col-1 text-center align-middle',
    label: t('digibib.contact.frontend.manager.label.created'),
    formatter: (value) => ((value) ? new Date(value).toLocaleDateString() : '-'),
  },
  {
    key: 'forwarded',
    thClass: 'col-1 text-center',
    tdClass: 'col-1 text-center align-middle',
    label: t('digibib.contact.frontend.manager.label.forwarded'),
    formatter: (value) => ((value) ? new Date(value).toLocaleDateString() : '-'),
  },
  {
    key: 'state',
    thClass: 'col-1 text-center',
    tdClass: 'col-1 text-center align-middle',
    label: t('digibib.contact.frontend.manager.label.state'),
    formatter: (value) => ((value) ? RequestState.toString(value) : '-'),
  },
  {
    key: 'requester',
    thClass: 'col-3 text-center',
    tdClass: 'col-3 text-center align-middle',
    label: t('digibib.contact.frontend.manager.label.requester'),
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
  const value = await bvModal.msgBoxConfirm(t('digibib.contact.frontend.manager.confirm.deleteRequest.message', {
    requestID: id,
  }), {
    title: t('digibib.contact.frontend.manager.confirm.deleteRequest.title'),
  });
  if (value) {
    try {
      await store.removeRequest(id);
    } catch (error) {
      emit('error', error instanceof Error ? error.message : 'unknown');
    }
  }
};
</script>
