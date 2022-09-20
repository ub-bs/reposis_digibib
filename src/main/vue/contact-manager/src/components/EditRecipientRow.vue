<template>
  <tr :class="recipient.failed ? 'table-danger' : ''">
    <td class="col-3">
      <input v-if="editable && editMode" class="form-control form-control-sm" type="text"
          v-model="recipientSave.name" :class="v.name.$error ? 'is-invalid' : ''" />
      <span v-else>
        {{ recipient.name }}
      </span>
    </td>
    <td class="col-2">
      <select v-if="editable && editMode" class="form-control form-control-sm"
          v-model="recipientSave.origin" :class="v.origin.$error ? 'is-invalid' : ''">
        <option :value="Origin.Manual">
          {{ $t('digibib.contact.frontend.manager.label.origin.manual') }}
        </option>
      </select>
      <span v-else>
        {{ recipient.origin }}
      </span>
    </td>
    <td class="col-4">
      <input v-if="editable && editMode" class="form-control form-control-sm" type="text"
          v-model="recipientSave.mail" :class="v.mail.$error ? 'is-invalid' : ''"/>
      <span v-else>
        {{ recipient.mail }}
      </span>
    </td>
    <td class="col-1 align-middle text-center">
      <input v-if="editMode" type="checkbox" v-model="recipientSave.enabled"/>
      <input v-else type="checkbox" v-model="recipientSave.enabled" disabled/>
    </td>
    <td class="col-1 text-center align-middle">
      <EditToolbar :disabled='disabled' :editMode='editMode' @edit="handleEdit"
          @cancel="handleCancel" @update="handleUpdate" @remove="handleRemove" :remove="editable" />
    </td>
  </tr>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import { useStore } from 'vuex';
import useVuelidate from '@vuelidate/core';
import { required, email } from '@vuelidate/validators';
import { Origin } from '../utils';
import EditToolbar from './EditToolbar.vue';

const props = defineProps({
  recipient: {
    type: Object,
    required: true,
  },
});
const recipientSave = ref(JSON.parse(JSON.stringify(props.recipient)));
const store = useStore();
const rules = computed(() => ({
  name: {
    required,
  },
  mail: {
    required,
    email,
  },
  origin: {
    required,
  },
}));
const editMode = ref(false);
const disabled = computed(() => store.state.main.editRecipientId !== undefined
    && store.state.main.editRecipientId !== props.recipient.mail);
const editable = computed(() => props.recipient.origin === Origin.Manual);
const v = useVuelidate(rules, props.recipient);
const handleEdit = () => {
  store.commit('modal/setEditRecipientId', props.recipient.mail);
  editMode.value = true;
};
const handleCancel = () => {
  store.commit('modal/setEditRecipientId', undefined);
  editMode.value = false;
};
const handleUpdate = () => {
  v.value.$validate();
  if (!v.value.$error) {
    store.dispatch('modal/updateRecipient', recipientSave.value);
    editMode.value = false;
    recipientSave.value = props.recipient;
  }
};
const handleRemove = () => {
  store.dispatch('modal/removeRecipient', props.recipient.mail);
};
</script>
