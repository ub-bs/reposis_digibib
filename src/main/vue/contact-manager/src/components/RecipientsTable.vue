<template>
  <table class="table table-striped">
    <thead>
      <tr>
        <th class="col-3">
          {{ $t('digibib.contact.frontend.manager.label.name') }}
        </th>
        <th class="col-2">
          {{ $t('digibib.contact.frontend.manager.label.origin') }}
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
import { computed } from 'vue';
import { useStore } from 'vuex';
import AddRecipientRow from './AddRecipientRow.vue';
import EditRecipientRow from './EditRecipientRow.vue';

const store = useStore();
const recipients = computed(() => store.getters.getCurrentRecipients);
</script>
