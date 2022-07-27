<template>
  <table class="table table-striped">
    <thead>
      <tr>
        <th scope="col" class="col-1">
          {{ t('digibib.contact.frontend.manager.label.id') }}
        </th>
        <th scope="col" class="col-2">
          {{ t('digibib.contact.frontend.manager.label.created') }}
        </th>
        <th scope="col" class="col-2">
          {{ t('digibib.contact.frontend.manager.label.objectID') }}
        </th>
        <th scope="col" class="col-5">
          {{ t('digibib.contact.frontend.manager.label.email') }}
        </th>
        <th scope="col" class="col-1">
          {{ t('digibib.contact.frontend.manager.label.state') }}
        </th>
        <th scope="col" class="col-1">
          {{ t('digibib.contact.frontend.manager.label.actions') }}
        </th>
      </tr>
    </thead>
    <tbody v-if="requests">
      <tr v-for="item in requests" :key="item">
        <td class="col-1">
          {{ item.id }}
        </td>
        <td class="col-2">
          {{ new Date(item.created).toLocaleString() }}
        </td>
        <td class="col-2">
          {{ item.objectID }}
        </td>
        <td class="col-5">
          {{ item.email }}
        </td>
        <td class="col-1">
          {{ item.state }}
        </td>
        <td class="col-1">
          <div class="btn-group">
            <a class="btn pt-0 pb-0" @click="viewRequest(item.id)">
              <i class="fa fa-eye"></i>
            </a>
            <a class="btn pt-0 pb-0" @click="removeRequest(item.id)">
              <i class="fa fa-trash"></i>
            </a>
          </div>
        </td>
      </tr>
    </tbody>
  </table>
  <Modal v-if="showModal" :id="currentId" size="lg" />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useStore } from 'vuex';
import { useI18n } from 'vue-i18n';
import Modal from './Modal.vue';

const store = useStore();
const requests = computed(() => store.state.requests);
const { t } = useI18n();
const currentId = ref(null);
const showModal = computed(() => store.state.showModal);
const viewRequest = (id: number) => {
  currentId.value = id;
  store.commit('setModal', { show: true, id });
};
const removeRequest = (id: number) => {
  console.log(id);
};
</script>
