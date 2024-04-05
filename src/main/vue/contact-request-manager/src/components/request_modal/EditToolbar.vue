<template>
  <div v-if="!editMode" class="btn-group">
    <button class="btn shadow-none pr-1 pb-0 pt-0 border-0" @click="$emit('mail')"
        :disabled="!mail">
      <i class="fa fa-envelope"></i>
    </button>
    <template v-if="editable">
      <button class="btn shadow-none pr-1 pb-0 pt-0 border-0" @click="$emit('edit')"
        :disabled="!edit">
        <i class="fas fa-edit"></i>
      </button>
      <button class="btn shadow-none pl-1 pb-0 pt-0 border-0" @click="$emit('remove')"
        :disabled="!remove">
        <i class="fas fa-trash"></i>
      </button>
    </template>
  </div>
  <div v-else class="btn-group">
    <button class="btn shadow-none pr-1 pb-0 pt-0 border-0" @click="$emit('update')">
      <i class="fas fa-check"></i>
    </button>
    <button class="btn shadow-none pl-1 pb-0 pt-0 border-0" @click="$emit('cancel')">
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
  remove: {
    type: Boolean,
    default: false,
  },
  edit: {
    type: Boolean,
    default: false,
  },
  mail: {
    type: Boolean,
    default: false,
  },
  editable: {
    type: Boolean,
    default: false,
  },
});
defineEmits(['cancel', 'edit', 'mail', 'remove', 'update']);
</script>
