<template>
  <div class="form-row">
    <div class="form-group col-md-4">
      <label for="inputName">{{ $t('digibib.contact.frontend.manager.label.name') }}</label>
      <input type="text" class="form-control" id="inputName" :value="body.name"
        disabled />
    </div>
    <div class="form-group col-md-5">
      <label for="inputEmail">{{ $t('digibib.contact.frontend.manager.label.email') }}</label>
      <input type="text" class="form-control" id="inputEmail" :value="body.email"
        disabled />
    </div>
    <div class="form-group col-md-3">
      <label for="inputOrcid">{{ $t('digibib.contact.frontend.manager.label.orcid') }}</label>
      <input type="text" class="form-control" id="inputOrcid" :value="body.orcid"
        disabled />
    </div>
  </div>
  <div class="form-group">
    <textarea class="form-control" rows="5" readonly :value="body.message" />
  </div>
</template>
<script setup lang="ts">
import { PropType } from 'vue';
import { RequestBody } from '@/utils';

defineProps({
  body: {
    type: Object as PropType<RequestBody>,
    required: true,
  },
});
</script>
