<template>
  <table class="table">
    <thead>
      <tr>
        <th v-for="field in fields" :key="field.key" :class="field.class">
          {{ field.label }}
        </th>
        <th class="col-1">
          <!-- {{ t('digibib.contact.frontend.manager.label.actions') }} -->
        </th>
      </tr>
    </thead>
    <tbody v-if="requests">
      <tr v-for="(item, index) in requests" :key="item">
        <td v-for="field in fields" :key="field.key" :class="field.class">
          {{ item[field.key] }}
        </td>
        <td class="col-1">
          <span class="icon" @click="viewTask(index)">
            <i class="fa fa-eye"></i>
          </span>
          <span class="icon" @click="editTask(index)">
            <i class="fa fa-edit"></i>
          </span>
          <span class="icon" @click="removeTask(index)">
            <i class="fa fa-trash"></i>
          </span>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useStore } from 'vuex';
import { useI18n } from 'vue-i18n';

const store = useStore();
const requests = computed(() => store.state.requests);
const { t } = useI18n();
const fields = [
  {
    key: 'created',
    label: t('digibib.contact.frontend.manager.label.created'),
    class: 'col-2',
  },
  {
    key: 'objectID',
    label: t('digibib.contact.frontend.manager.label.objectID'),
    class: 'col-1',
  },
  {
    key: 'name',
    label: t('digibib.contact.frontend.manager.label.name'),
    class: 'col-2',
  },
  {
    key: t('email'),
    label: t('digibib.contact.frontend.manager.label.email'),
    class: 'col-3',
  },
  {
    key: 'orcid',
    label: t('digibib.contact.frontend.manager.label.orcid'),
    class: 'col-2',
  },
  {
    key: 'state',
    label: t('digibib.contact.frontend.manager.label.state'),
    class: 'col-1',
  },
];
</script>
