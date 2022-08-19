<template>
  <table class="table table-striped table-bordered">
    <thead>
      <tr>
        <th class="col-3">
          {{ $t('digibib.contact.frontend.manager.label.name') }}
        </th>
        <th class="col-2">
          {{ $t('digibib.contact.frontend.manager.label.source') }}
        </th>
        <th class="col-4">
          {{ $t('digibib.contact.frontend.manager.label.email') }}
        </th>
        <th class="col-1 text-center">
          {{ $t('digibib.contact.frontend.manager.label.state') }}
        </th>
        <th class="col-1">
        </th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="item in recipients" :key="item">
        <td class="col-3 align-middle">
          {{ item.name }}
        </td>
        <td class="col-2 align-middle">
          {{ item.origin }}
        </td>
        <td class="col-4 align-middle">
          {{ item.email }}
        </td>
        <td class="col-1 text-center align-middle">
          <input type="checkbox" v-model="item.enabled" disabled />
        </td>
        <td class="col-1 text-center align-middle">
          <div class="btn-group">
            <a class="btn pr-1 pt-0 pb-0 border-0" @click="TODO">
              <i class="fas fa-edit"></i>
            </a>
          </div>
        </td>
      </tr>
      <AddRecipientRow />
    </tbody>
  </table>
</template>
<script setup lang="ts">
import AddRecipientRow from './AddRecipientRow.vue';

defineProps({
  recipients: {
    type: Array,
    default: () => [],
  },
});
</script>
