<template>
  <tr>
    <td class="col-3">
      <input class="form-control form-control-sm" type="text" v-model="name"
          :class="v.name.$error? 'is-invalid' : ''" :disabled="disabled"/>
    </td>
    <td class="col-2">
      {{ ORIGIN_MANUAL }}
    </td>
    <td class="col-4">
      <input class="form-control form-control-sm" type="text" v-model="mail"
          :class="v.mail.$error ? 'is-invalid' : ''" :disabled="disabled" />
    </td>
    <td class="col-1 align-middle text-center">
      <input type="checkbox" v-model="enabled" :disabled="disabled" />
    </td>
    <td class="col-1 text-center align-middle">
      <div class="btn-group">
        <button class="btn shadow-none pr-1 pb-0 pt-0 border-0" @click="addRecipient"
            :disabled="disabled">
          <i class="fas fa-check"></i>
        </button>
        <button class="btn shadow-none pl-1 pb-0 pt-0 border-0" @click="reset"
            :disabled="disabled">
          <i class="fas fa-ban"></i>
        </button>
      </div>
    </td>
  </tr>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import useVuelidate from '@vuelidate/core';
import { required, email } from '@vuelidate/validators';
import { ContactPerson, ORIGIN_MANUAL } from '@/utils';

defineProps({
  disabled: {
    type: Boolean,
    required: true,
  },
});
const name = ref();
const mail = ref();
const origin = ref(ORIGIN_MANUAL);
const enabled = ref(false);
const emit = defineEmits(['add']);
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
const v = useVuelidate(rules, { name, mail, origin });
const reset = () => {
  v.value.$reset();
  name.value = null;
  mail.value = null;
  enabled.value = false;
};
const addRecipient = async () => {
  v.value.$validate();
  if (!v.value.$error) {
    emit('add', {
      name: name.value,
      email: mail.value,
      origin: origin.value,
      enabled: enabled.value,
    } as ContactPerson);
    reset();
  }
};
</script>../../utils
