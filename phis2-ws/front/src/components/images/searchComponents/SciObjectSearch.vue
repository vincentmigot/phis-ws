<template>
  <b-form-group label="Scientific Object Alias">
    <b-form-tags v-model="value" no-outer-focus class="mb-2">
      <template v-slot="{ tags, disabled }">
        <ul v-if="tags.length > 0" class="list-inline d-inline-block mb-2">
          <li v-for="(tag, index) in tags" :key="tag" class="list-inline-item">
            <b-form-tag
              @remove="onRemove(index)"
              :title="tag"
              :disabled="disabled"
              variant="info"
            >{{ tag }}</b-form-tag>
          </li>
        </ul>
        <b-form-input
          v-model="search"
          list="input-list"
          id="input-with-list"
          placeholder="Enter Alias to search "
          autocomplete="off"
          @input="onWrite($event)"
        ></b-form-input>
        <datalist id="input-list">
          <option v-for="option in options" :key="option">{{ option }}</option>
        </datalist>
      </template>
    </b-form-tags>
  </b-form-group>
</template>


<script lang="ts">
import { Component } from "vue-property-decorator";
import Vue from "vue";

import HttpResponse, { OpenSilexResponse } from "../../../lib/HttpResponse";
import { ScientificObjectsService } from "../../../lib/api/scientificObjects.service";
import { ScientificObjectDTO } from "../../../lib/model/scientificObjectDTO";
import { EventBus } from "./../event-bus";

@Component
export default class ObjectSearch extends Vue {
  $opensilex: any;
  $store: any;
  get user() {
    return this.$store.state.user;
  }
  selectedExperiment: any;
  selectedSoType: any;
  alias: any = undefined;

  currentPage: number = 0;
  pageSize: number = 8000;
  options = [];
  search = "";
  value = [];
  valueWithURI = {};
  selectedValueWithUri = {};

  created() {
    this.options = [];

     EventBus.$on("imageTypeSelected", type => {
      this.selectedExperiment = null;
      this.selectedSoType = null;
      this.value = [];
      this.options = [];
      this.search = "";
    });

    EventBus.$on("experienceHasChanged", experience => {
      this.selectedExperiment = experience;
      this.value = [];
      this.options = [];
      this.search = "";
    });

    EventBus.$on("soTypeHasChanged", type => {
      this.selectedSoType = type;
      this.value = [];
      this.options = [];
      this.search = "";
    });

  }

  onChange(selectedValue) {
    console.log("onChange");
    this.value.push(selectedValue);
    this.selectedValueWithUri = {};
    this.value.forEach(element => {
      this.selectedValueWithUri[this.valueWithURI[element]] = element;
    });
    this.search = "";
    EventBus.$emit("searchObjectSelected", this.selectedValueWithUri);
  }

  onRemove(index) {
    this.value.splice(index, 1);
    this.selectedValueWithUri = {};
    this.value.forEach(element => {
      this.selectedValueWithUri[this.valueWithURI[element]] = element;
    });

    this.search = "";
    EventBus.$emit("searchObjectSelected", this.selectedValueWithUri);
  }

  onWrite(value) {
    if (this.options.includes(value)) {
      this.onChange(value);

    } else {
      this.alias = value;
      let service: ScientificObjectsService = this.$opensilex.getService(
        "opensilex.ScientificObjectsService"
      );
      if (this.selectedExperiment === null) {
        this.selectedExperiment = undefined;
      }
      if (this.selectedSoType === null) {
        this.selectedSoType = undefined;
      }
      
      const result = service
        .getScientificObjectsBySearch(
          this.user.getAuthorizationHeader(),
          this.pageSize,
          this.currentPage,
          undefined,
          this.selectedExperiment,
          this.alias,
          this.selectedSoType
        )
        .then(
          (
            http: HttpResponse<OpenSilexResponse<Array<ScientificObjectDTO>>>
          ) => {
            const res = http.response.result as any;
            const data = res.data;
            this.options = [];
            data.forEach(element => {
              this.options.push(element.label);
              this.valueWithURI[element.label] = element.uri;
            });
          }
        )
        .catch(error => {
          console.log(error);
          this.options = [];
        });
    }
  }

  onEnter() {
    console.log("enter");
  }
}
</script>





<style scoped lang="scss">
.badge-info {
    background-color: #00A38D;
}
</style>
