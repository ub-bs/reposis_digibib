<template>
  <div class="form-group">
    <label for="inputComment">
      {{ $t('digibib.contact.frontend.manager.label.comment') }}
    </label>
    <div class="d-flex flex-row">
      <textarea id="inputComment" class="form-control" rows="4"
        :readonly="!enabled || !isCommentEditMode" @blur="cancelCommentEdit"
        :value="comment" @click="startCommentEdit"
        v-on:input="handleCommentChanged" />
      <div v-if="enabled && isCommentEditMode" class="d-flex flex-column pl-1">
        <div class="btn-group-vertical">
          <button class="btn btn-primary shadow-none" @mousedown="handleSave"
              title="Save">
            <i class="fa fa-check"></i>
          </button>
          <button class="btn btn-primary shadow-none" title="Cancel">
            <i class="fas fa-times"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue';

defineProps({
  comment: {
    type: String,
  },
  enabled: {
    type: Boolean,
    required: true,
  },
});
const emit = defineEmits(['save']);
const isCommentEditMode = ref(false);
let updateComment = '';
const handleCommentChanged = (v) => {
  updateComment = v.target.value;
};
const startCommentEdit = (): void => {
  isCommentEditMode.value = true;
};
const cancelCommentEdit = (): void => {
  isCommentEditMode.value = false;
};
const handleSave = (): void => {
  emit('save', updateComment);
  isCommentEditMode.value = false;
};
</script>
