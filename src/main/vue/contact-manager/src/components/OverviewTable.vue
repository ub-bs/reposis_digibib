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
          {{ item.state }}
        </td>
        <td class="col-1 align-middle">
          <div class="btn-group">
            <a class="btn pt-0 pb-0 pr-1 pl-2" @click="viewRequest(item.uuid)">
              <i class="fa fa-eye"></i>
            </a>
            <a class="btn pt-0 pb-0 pl-1 pr-2" @click="removeRequest(item.uuid)">
              <i class="fa fa-trash"></i>
            </a>
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
import { useStore, mapGetters } from 'vuex';
import { useI18n } from 'vue-i18n';
import ConfirmModal from './ConfirmModal.vue';
import RequestModal from './RequestModal.vue';

const store = useStore();
const { t } = useI18n();
const requests = computed(() => store.getters.getCurrentRequests);
const confirmModal = ref(null);
const showRequestModal = computed(() => store.state.modal.showRequestModal);
const viewRequest = (id: string) => {
  store.dispatch('modal/showRequestModal', id);
};
const removeRequest = async (id: string) => {
  const ok = await confirmModal.value.show({
    title: t('digibib.contact.frontend.manager.confirmDelete.title'),
    message: t('digibib.contact.frontend.manager.confirmDelete.message', {
      requestID: id,
    }),
  });
  store.dispatch('removeContactRequest', id);
};
</script>
