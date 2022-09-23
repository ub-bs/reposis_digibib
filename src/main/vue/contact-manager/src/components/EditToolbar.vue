<template>
  <div v-if="!editMode" class="btn-group">
    <button class="btn shadow-none pr-1 pb-0 pt-0 border-0" @click="$emit('edit')"
        :disabled="disabled">
      <i class="fas fa-edit"></i>
    </button>
    <button class="btn shadow-none pl-1 pb-0 pt-0 border-0" @click="$emit('remove')"
        :disabled="disabled || !remove">
      <i class="fas fa-trash"></i>
    </button>
  </div>
  <div v-else class="btn-group">
    <button class="btn shadow-none pr-1 pb-0 pt-0 border-0" @click="$emit('update')"
        :disabled="disabled">
      <i class="fas fa-check"></i>
    </button>
    <button class="btn shadow-none pl-1 pb-0 pt-0 border-0" @click="$emit('cancel')"
        :disabled="disabled">
      <i class="fas fa-ban"></i>
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps({
  editMode: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  remove: {
    type: Boolean,
    default: true,
  },
});
defineEmits(['edit', 'update', 'cancel', 'remove']);
</script>
