<template>
  <tr>
    <td class="col-3">
      <input class="form-control form-control-sm" type="text" v-model="newRecipient.name"
          :class="v.name.$error ? 'is-invalid' : ''"/>
    </td>
    <td class="col-2">
      <select class="form-control form-control-sm" v-model="newRecipient.origin"
          :class="v.origin.$error ? 'is-invalid' : ''">
        <option disabled value=""></option>
        <option value="MANUAL">manuel</option> <!-- TODO i18n -->
      </select>
    </td>
    <td class="col-4">
      <input class="form-control form-control-sm" type="text" v-model="newRecipient.email"
          :class="v.email.$error ? 'is-invalid' : ''"/>
    </td>
    <td class="col-1 align-middle text-center">
      <input type="checkbox" v-model="newRecipient.state"/>
    </td>
    <td class="col-1 text-center align-middle">
      <div class="btn-group">
        <a class="btn pr-1 pb-0 pt-0 border-0" @click="addNewRecipient">
          <i class="fas fa-check"></i>
        </a>
        <a class="btn pl-1 pb-0 pt-0 border-0" @click="resetNewRecipient">
          <i class="fas fa-times"></i>
        </a>
      </div>
    </td>
  </tr>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import { useStore } from 'vuex';
import useVuelidate from '@vuelidate/core';
import { required, email } from '@vuelidate/validators';
import { Recipient } from '../store/state';

const store = useStore();
const rules = computed(() => ({
  name: {
    required,
  },
  email: {
    required,
    email,
  },
  origin: {
    required,
  },
}));
const newRecipient = ref({});
const v = useVuelidate(rules, newRecipient);
const addNewRecipient = () => {
  v.value.$validate();
  if (!v.value.$error) {
    store.dispatch('addRecipient', newRecipient.value);
  }
};
const resetNewRecipient = () => {
  newRecipient.value = {};
};
</script>
