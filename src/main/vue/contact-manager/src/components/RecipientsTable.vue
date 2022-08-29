<template>
  <table class="table table-striped">
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
      <template v-for="recipient in recipients" :key="recipient">
        <EditRecipientRow :recipient="recipient" />
      </template>
      <!-- TODO check request state -->
      <AddRecipientRow />
    </tbody>
  </table>
</template>
<script setup lang="ts">
import AddRecipientRow from './AddRecipientRow.vue';
import EditRecipientRow from './EditRecipientRow.vue';

defineProps({
  recipients: {
    type: Array,
    default: () => [],
  },
});
</script>
