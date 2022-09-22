<template>
  <tr :class="recipient.failed ? 'table-danger' : (recipient.confirmed ? 'table-success' : '')">
    <td class="col-3">
      {{ recipient.name }}
    </td>
    <td class="col-2">
      {{ recipient.origin }}
    </td>
    <td class="col-4">
      {{ recipient.mail }}
    </td>
    <td class="col-1 align-middle text-center">
      <input type="checkbox" v-model="recipientSave.enabled" disabled/>
    </td>
    <td class="col-1 text-center align-middle">
      <EditToolbar disabled />
    </td>
  </tr>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import EditToolbar from './EditToolbar.vue';

const props = defineProps({
  recipient: {
    type: Object,
    required: true,
  },
});

const recipientSave = ref(JSON.parse(JSON.stringify(props.recipient)));
</script>
