<template>
  <div :style="headerDiv">
    <!-- 流程名称和步骤条容器 -->
    <div :style="headerContainer" v-if="!onlyDesignShow">
      <!-- 流程名称 -->
      <el-tooltip :content="logicJson.flowName" placement="top">
        <div class="flow-name">
          <svg-icon icon-class="flowName" style="margin-right: 5px"/>
          {{ logicJson.flowName }}
        </div>
      </el-tooltip>
      <!-- 自定义步骤按钮 -->
      <div class="steps-container">
        <div class="steps">
          <div
              v-for="(step, index) in steps"
              :key="index"
              class="step-item"
              :class="{ 'active': activeStep === index }"
              @click="handleStepClick(index)"
          >
            <svg-icon :icon-class="step.icon" style="margin-right: 5px"/>
            <span>{{ step.title }}</span>
          </div>
        </div>
      </div>
    </div>

    <el-header :style="headerStyle">
      <div style="padding: 5px 0; text-align: right;">
        <div v-if="activeStep === 1">
          <el-button size="small" icon="ZoomOut" @click="zoomViewport(false)">缩小</el-button>
          <el-button size="small" v-if="'CLASSICS' === logicJson.modelValue" icon="Rank" @click="zoomViewport(1)">自适应</el-button>
          <el-button size="small" icon="ZoomIn" @click="zoomViewport(true)">放大</el-button>
          <el-button size="small" icon="DArrowLeft" @click="undoOrRedo(true)">上一步</el-button>
          <el-button size="small" icon="DArrowRight" @click="undoOrRedo(false)">下一步</el-button>
          <el-button size="small" icon="Delete" @click="clear()">清空</el-button>
          <el-button size="small" icon="Download" @click="downLoad">下载流程图</el-button>
          <el-button size="small" icon="Download" @click="downJson">下载JSON</el-button>
          <el-button size="small" style="background-color: #48D1CC;"  @click="saveJsonModel" v-if="!disabled">
            <svg-icon icon-class="save" style="margin-right: 5px;"/>保存
          </el-button>
        </div>
        <div v-else>
          <el-button size="small" style="background-color: #48D1CC;" @click="saveJsonModel" v-if="!disabled">
            <svg-icon icon-class="save" style="margin-right: 5px;"/>保存
          </el-button>
        </div>
      </div>

      <BaseInfo :style="baseInfoStyle" ref="baseInfoRef" v-if="!onlyDesignShow" v-show="activeStep === 0"
                :logic-json="logicJson" :category-list="categoryList" :form-path-list="formPathList"
                :definition-id="definitionId" :disabled="disabled"
                @update:flow-name="handleFlowNameUpdate" @update:model-value="handleModelValueUpdate"/>

      <div class="container" ref="containerRef" v-show="activeStep === 1">
        <PropertySetting ref="propertySettingRef" :node="nodeClick" :lf="lf" :disabled="disabled"
                         :skipConditionShow="skipConditionShow" :nodes="nodes" :skips="skips"
                         :form-path-list="formPathList">
        </PropertySetting>
      </div>
      <div class="logo-text" v-if="activeStep === 1">流程设计器</div>
    </el-header>
  </div>

  <!-- 弹框组件 -->
  <EdgeTooltip
      v-if="tooltipVisible"
      :position="tooltipPosition"
      :tooltipEdge="tooltipEdge"
      @option-click="handleOptionClick"
      @close-tooltip="tooltipVisible = false"
  />
</template>

<script setup name="Design">
import LogicFlow from "@logicflow/core";
import "@logicflow/core/lib/style/index.css";
import {DndPanel, InsertNodeInPolyline, Menu, Snapshot} from '@logicflow/extension';
import '@logicflow/extension/lib/style/index.css'
import { ElLoading, ElMessage } from 'element-plus'
import PropertySetting from '@/components/design/common/vue/propertySetting.vue'
import { queryDef, saveJson } from "@/api/flow/definition";
import { getFlowDefinitionById } from "@/api/flow";
import {
    getPreviousNodes,
    isClassics, isGateWay,
    json2LogicFlowJson,
    logicFlowJsonToWarmFlow
} from "@/components/design/common/js/tool";
import StartC from "@/components/design/classics/js/start";
import BetweenC from "@/components/design/classics/js/between";
import SerialC from "@/components/design/classics/js/serial";
import ParallelC from "@/components/design/classics/js/parallel";
import InclusiveC from "@/components/design/classics/js/inclusive";
import EndC from "@/components/design/classics/js/end";
import SkipC from "@/components/design/classics/js/skip";
import StartM from "@/components/design/mimic/js/start";
import BetweenM from "@/components/design/mimic/js/between";
import SerialM from "@/components/design/mimic/js/serial";
import ParallelM from "@/components/design/mimic/js/parallel";
import InclusiveM from "@/components/design/mimic/js/inclusive";
import EndM from "@/components/design/mimic/js/end";
import SkipM from "@/components/design/mimic/js/skip";
import useAppStore from "@/store/app";
import {computed, onMounted, onUnmounted, ref, watch, nextTick, getCurrentInstance} from "vue";
import { useRoute, useRouter } from "vue-router";
import BaseInfo from "@/components/design/common/vue/baseInfo.vue";
import initClassicsData from "@/components/design/classics/initClassicsData.json";
import initMimicData from "@/components/design/mimic/initMimicData.json";
import {addBetweenNode, addGatewayNode, gatewayAddNode, removeNode} from "@/components/design/mimic/js/mimic.js";
import EdgeTooltip from "@/components/design/mimic/vue/EdgeTooltip.vue";
import SvgIcon from "@/components/SvgIcon/index.vue";

const route = useRoute();
const router = useRouter();
const appStore = useAppStore();
const appParams = computed(() => appStore.appParams || route.query);

const { proxy } = getCurrentInstance();

// Add $modal for compatibility
proxy.$modal = {
  msgSuccess: (msg) => ElMessage.success(msg),
  msgError: (msg) => ElMessage.error(msg),
  msgWarning: (msg) => ElMessage.warning(msg),
};

// Reactive variables - must be declared at top level before any hooks
const lf = ref(null);
const definitionId = ref();
const nodeClick = ref(null);
const disabled = ref(false);
const propertySettingRef = ref({});
const logicJson = ref({});
const jsonString = ref('');
const skipConditionShow = ref(true);
const nodes = ref([]);
const skips = ref([]);
const categoryList = ref([]);
const formPathList = ref([]);
const isDark = ref(false);
const activeStep = ref(0);
const onlyDesignShow = ref(false);

const tooltipVisible = ref(false);
const tooltipPosition = ref({ x: 0, y: 0 });
const tooltipEdge = ref({});

// Computed styles
const headerStyle = computed(() => {
  return {
    top: "5px",
    right: "50px",
    zIndex: "2",
    height: "auto",
    backgroundColor: isDark.value ? "#141414" : "#fff",
    border: "1px solid #ddd",
    borderRadius: "6px",
    margin: "5px",
  };
});

const baseInfoStyle = computed(() => {
  return {
    margin: "5px",
    backgroundColor: isDark.value ? "#141414" : "#fff",
  };
});

const headerDiv = computed(() => {
    return {
        backgroundColor: isDark.value ? "#141414" : "#fff",
    };
});

const headerContainer = computed(() => {
  return {
    display: "flex",
    alignItems: "center",
    border: "1px solid #ddd",
    borderRadius: "6px",
    height: "100%",
    top: "5px",
    margin: "0px 5px",
  };
});

// Steps data
const steps = [
  { title: '基础信息', icon: 'baseInfo' },
  { title: '流程设计', icon: 'flowDesign' },
];

// Functions
const handleOptionClick = (item) => {
  if (item.icon === "between") {
    addBetweenNode(lf.value, item.tooltipEdge);
  } else {
    addGatewayNode(lf.value, item.tooltipEdge, item.icon);
  }
  tooltipVisible.value = false;
};

async function handleStepClick(index) {
  if (activeStep.value === 0 && !onlyDesignShow.value) {
    let validate = await proxy.$refs.baseInfoRef.validate();
    if (!validate) return
  }
  activeStep.value = index;

  if (index === 1) {
    handleModelValueUpdate()
  }
}

// Extract initialization logic to separate function
function initLogicFlow() {
  if (proxy.$refs.containerRef) {
    use();
    lf.value = new LogicFlow({
      container: proxy.$refs.containerRef,
      textEdit: false,
      snapToGrid: true,
      hideAnchors: !isClassics(logicJson.value.modelValue),
      adjustNodePosition: isClassics(logicJson.value.modelValue),
      hoverOutline: isClassics(logicJson.value.modelValue),
      nodeSelectedOutline: isClassics(logicJson.value.modelValue),
      edgeSelectedOutline: isClassics(logicJson.value.modelValue),
      grid: {
        size: 20,
        visible: Boolean(appParams.value?.showGrid || route.query.showGrid),
        type: 'dot',
        config: {
          color: '#ccc',
          thickness: 1,
        },
        background: {
          backgroundColor: "#fff",
        },
      },
      keyboard: isClassics(logicJson.value.modelValue) ? {
        enabled: true,
        shortcuts: [
          {
            keys: ["delete"],
            callback: () => {
              const elements = lf.value.getSelectElements(true);
              lf.value.clearSelectElements();
              elements.edges.forEach((edge) => lf.value.deleteEdge(edge.id));
              elements.nodes.forEach((node) => lf.value.deleteNode(node.id));
            },
          },
        ],
      } : {},
    });
    lf.value.setTheme({
      snapline: {
        stroke: '#1E90FF',
        strokeWidth: 2,
      },
      edgeText: {
        fontSize: 13,
        strokeWidth: 1,
        background: {
          fill: "#fff",
        },
      },
    });

    initDndPanel();
    register();
    initMenu()
    initEvent();
    if (logicJson.value) {
      lf.value.render(logicJson.value);
      zoomViewport(1);
    }
  }
}

watch(isDark, (v) => {
  if (!lf.value) {
    return;
  }
  lf.value.graphModel.background = {
    background: v ? "#141414" : "#fff",
  };
});

/**
 * Listen for window messages
 */
function listeningMessage(e) {
  const { data } = e;
  switch (data.type) {
    case "theme-dark": {
      isDark.value = true;
      return;
    }
    case "theme-light": {
      isDark.value = false;
      return;
    }
  }
}

/**
 * Initialize drag panel
 */
function initDndPanel() {
  if (isClassics(logicJson.value.modelValue)) {
    lf.value.extension.dndPanel.setPatternItems([
      {
        type: 'start',
        text: '开始',
        label: '开始节点',
        icon: 'data:image/svg+xml;charset=utf-8;base64,PHN2ZyB0PSIxNzQ4MTc1OTQ3Mzg4IiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjIwODA5IiB3aWR0aD0iMzYiIGhlaWdodD0iMzYiPjxwYXRoIGQ9Ik01MTIgMTAyNEMyMjkuMjMwNDMxIDEwMjQgMCA3OTQuNzY5NTY5IDAgNTEyUzIyOS4yMzA0MzEgMCA1MTIgMHM1MTIgMjI5LjIzMDQzMSA1MTIgNTEyLTIyOS4yMzA0MzEgNTEyLTUxMiA1MTJ6IG0wLTk1MC4zNzkwODVDMjY5Ljg4OTI1NSA3My42MjA5MTUgNzMuNjIwOTE1IDI2OS44ODkyNTUgNzMuNjIwOTE1IDUxMnMxOTYuMjY4MzQgNDM4LjM3OTA4NSA0MzguMzc5MDg1IDQzOC4zNzkwODUgNDM4LjM3OTA4NS0xOTYuMjY4MzQgNDM4LjM3OTA4NS00MzguMzc5MDg1Uzc1NC4xMTA3NDUgNzMuNjIwOTE1IDUxMiA3My42MjA5MTV6IiBmaWxsPSIjMDAwMDAwIiBwLWlkPSIyMDgxMCI+PC9wYXRoPjwvc3ZnPg==',
      },
      {
        type: 'between',
        text: '中间节点',
        label: '中间节点',
        icon: 'data:image/svg+xml;charset=utf-8;base64,PHN2ZyB0PSIxNzQ4MTc1Mzc1ODI3IiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjgzMTkiIHdpZHRoPSIzNiIgaGVpZ2h0PSIzNiI+PHBhdGggZD0iTTMzNS43NTE4MDQgMjQ0Ljc4MzE0N0MyODMuNjA3MDI5IDI0NC43ODMxNDcgMjQ2LjMwMzg5OSAyODQuODY5MDYgMjQ2LjE5OTI3IDMzMC41Mjk2NDJsMCAwLjAxMzIwNyAwIDAuMDEyNjQ4YzAuMDAzMjk0IDEzLjgwODQ2NSAzLjczOTc0MyAyOC4zODE3NDcgOS41Nzc0MzEgNDEuNTI2MzQyIDQuMjE1MTMyIDkuNDkxMTE1IDkuNDU1Nzk4IDE4LjIxNjY3NiAxNS44NDE3NDQgMjUuMjA2NjE0QzIzMy42NjU1ODggNDEwLjI3Mjg1MyAxODkuMjAxOTQ5IDQzMS42NDI4ODMgMTY2LjcyOCA0NzMuNzg1NTNMMTY1LjUxNjk2IDQ3Ni4wNTI1MTRsMCAxMzYuNDA4NDgyIDM0MC40Njk2ODkgMCAwLTEzNi40MDg0ODItMS4yMTEwNDMtMi4yNzA5ODRjLTIyLjE1MDcwOC00MS41MzI1NjYtNjUuNjUyMjA0LTYyLjg3Mjg0OC0xMDMuMjM4MjA0LTc1LjkxMTExNiAxOC4zNDg0MTktMTguNjU4MjE4IDIzLjc2MTA0OS00Mi43NDA1NzMgMjMuNzY2OTMyLTY3LjMxNDkybDAtMC4wMTI2NDggMC0wLjAxMzIwN0M0MjUuMTk5NzA3IDI84Ljg2OTA2MyAzODcuODk2NTgzIDI0NC43ODMxNDcgMzM1Ljc1MTgwNCAyNDQuNzgzMTQ3Wk0zMDAuMTQ4NTIzIDI5My40MDQ1MjFjMi40MTAzMjggMC4wMDYwNTkgNS4wNTY2NTAgMC4wODY1NzIgNy45NzQwMTUgMC4yNTg1MjIgMjMuMjQ0MDI5IDEuMzcwMDI5IDMxLjA2Njk5NiA1LjU1NDA1OSAzNy4wODAzMjMgOS41MjIyODMgNi4wMTMyOTggMy45NjgyMjMgMTAuMjUyNDE3IDcuNzQ1Njk5IDI2LjE0NDE4OSA4LjIwODk4MmwwLjAwNDcwNiAwIDAuMDA1Mjk1IDBjMTIuMzgzODktMC40NjMyMTUgMTguMzM5NTA2LTIuNjcxMTM1IDIyLjYxMDQ1Mi01LjE3MjE5MiAxLjczMDY0NS0xLjAxMzQ1MyAzLjE4MzgyNS0yLjA2NzAwMyA0LjY3Mjk1LTMuMDcyOTg0IDMuOTM2MDM2IDguNDM2NjY4IDYuMDQ5NjU0IDE3Ljc2MjkzOCA2LjA3MzU5NyAyNy40MTQ5ODEtMC4wMDgyMzYgMjcuNDg0NjUyLTQuNzMzMzA4IDQ2LjczMjMwMi0yOS45MzQxNTQgNjIuNDgyODNsMi40NjUxNzcgMTguNTgwOTQ0YzUuMjQ1MzUxIDEuNTkyODg5IDEwLjY2NzMwNSAzLjM0MDY3MSAxNi4xNzAzNTQgNS4yNTcyMiAwLjc2ODUzNSAzLjIwNjE4MyAxLjY1NjQ5MiA3LjQxMTMwNiAyLjI1Mzc0OCAxMS44ODE3MzkgMC42MjU2NyA0LjY4MzQ1NCAwLjg3MTc0OSA5LjU1NjMzMyAwLjQ4NjAxMSAxMy4yMTUxMzktMC4zODU3MzggMy42NTg4MDYtMS41MjE3MTYgNS42MzM5NzItMS43MjExNzQgNS44MzM0NTktMTIuODA4OTg0IDEyLjgwODk1NS0zNS41NDYwMzYgMjAuMjc5MTM5LTU4LjYwODQzOCAyMC4yNzkxMzktMjMuMDYyMzkzIDAtNDUuNzk5NDQ0LTcuNDcwMTg0LTU4LjYwODQyMy0yMC4yNzkxMzktMC4xOTk0NjEtMC4xOTk0ODctMS4zMzU0NTYtMi4xNzQ2NTMtMS43MjExOTQtNS44MzM0NTktMC4zODU3MzUtMy42NTg4MDYtMC4xMzk2NjItOC41MzE2ODUgMC40ODYwMjYtMTMuMjE1MTM5IDAuNjAwNTI3LTQuNDk1MTE1IDEuNDk1NTUyLTguNzI1MDI4IDIuMjY2OTYzLTExLjkzNzQ2NyA1LjQ0ODA4Ni0xLjg5NDYwNiAxMC44MTU1NDctMy42MjQwNDIgMTYuMDEwMDczLTUuMjAxNDkxbDEuNDY5NTYxLTE5LjkwOTc1NmMtMS4xOTY1NzEtMS41MzQ1NjQtMi40MTU3ODItMi41NTE1MTY3LTMuODcwOTU5LTMuNjQyODg0LTUuNjI0Mzc3LTQuMjE5NTY1LTEyLjQ0NTE0Mi0xMy41MDIyNDUtMTcuMjYzMDQ5LTI0LjM1MDYxMy00LjgxNjE4OS0xMC44NDQ0OTktNy44MDAwMzgtMjMuMjQwMDIzLTcuODA0NTM2LTMzLjE2MjgxOCAwLjAyOTkyNi0xMS44OTYzODIgMy4yMzEzNzktMjMuMjk5MzA0IDkuMTE5NTE2LTMzLjE0OTAzMiAxLjA1MjExNS0wLjM5MTYxOCAyLjE2MTU2Ni0wLjgwNTUxNyAzLjQwODQ4OC0xLjIxNTYzNCA0LjM4NTAxNy0xLjQ0MjI1IDEwLjM5Mzk3My0yLjgxODg4OSAyMC44Mzg3MTYtMi43OTI2Mjh6TTI1NS42MzA3OCA0MjUuNjM4MTM2Yy0wLjAxODUwMiAwLjEzNDk2MS0wLjAzOTM1MyAwLjI2NjY4MS0wLjA1NzQ0OSAwLjQwMjE0OC0wLjc2MDkxNCA1LjY5NTYzNi0xLjIwODAzMSAxMS44OTUyNzMtMC41NTM4MTcgMTguMTAwNjc1IDAuNjU0MjE0IDYuMjA1NDAyIDIuMjkxNTY4IDEyLjg4NjQzMiA3LjYzODUwNyAxOC4yMzMzNSAxOC4yNTA4NjEgMTguMjUwODgxIDQ1Ljg3MTc1OSAyNi4zMTAyMzMgNzMuMTY3MzE4IDI2LjMxMDIzMyAyNy4yOTU1NTEgMCA1NC45MTY0NTItOC4wNTkzNTEgNzMuMTY3MzA0LTI2LjMxMDIzMyA1LjM0Njk0OC01LjM0NjkxOCA2Ljk4NDMyLTEyLjAyNzk0OCA3LjYzODUyMi0xOC4yMzMzNTAwLjY1NDIwMi02LjIwNTQzMSAwLjIwNzEwNi0xMi40MDUwMzktMC41NTM4MTEtMTguMTAwNjc1LTAuMDE1MDAxLTAuMTEyMjQ4LTAuMDMyNDE0LTAuMjIxMzA3LTAuMDQ3NjgtMC4zMzMzMiAyNy43NDc1MyAxMi4xNjgzNiA1NC41Njc3NDYgMjkuNTk1MjYxIDY5LjM2NzAxNSA1NS42MTQ3MzRsMCAxMTAuNTQ5MjI4LTQ5LjI2ODgzMiAwIDAtNzcuOTQ3NzA0LTIwLjU4OTk2IDAgMCA3Ny45NDc3MDQtMTYwLjAxMzQgMCAwLTc3Ljk0NzcwNC0yMC41ODk5NiAwIDAgNzcuOTQ3NzA0LTQ4LjgyNzYxOCAwIDAtMTEwLjU0OTIyOGMxNC44MjcxNTYtMjYuMDY4NTM2IDQxLjcyMDU0Ny00My41MTIyMzIgNjkuNTIzODYtNTUuNjgzNjcyek0yMTkuOTgxIDEwNy41MTk1NzVjLTEwOS45MzQ4MjQgMC0xOTkuNTAxIDg5LjE4ODM0NS0xOTkuNTAxIDE5OC45MTE5OTlsMCA0MTEuMTM1OTg5YzAgMTA5LjcyMzY1OSA4OS41NjYxNzYgMTk4LjkxMjAxMSAxOTkuNTAxIDE5OC45MTIwMTFsNTg0LjAzNzk4OSAwYzEwOS45MzQ4NTMgMCAxOTkuNTAxMDAxLTg5LjE4ODM1MSAxOTkuNTAxMDAxLTE5OC45MTIwMTFsMC00MTEuMTM1OTg5YzAtMTA5LjcyMzY1My04OS41NjYxNDgtMTk4LjkxMTk5OS0xOTkuNTAxMDAxLTE5OC45MTE5OTlsLTU4NC4wMzc5ODkgMHptMCA2MS40NDAwMDFsNTg0LjAzNzk4OSAwYzc3LjA3NDk1NSAwIDEzOC4wNjEwMDMgNjAuODM4OTE1IDEzOC4wNjEwMDMgMTM3LjQ3MTk5OGwwIDQxMS4xMzU5ODljMCA3Ni42MzMwOTQtNjAuOTg2MDQ4IDEzNy40NzIwMTItMTM4LjA2MTAwMyAxMzcuNDcyMDEybC01ODQuMDM3OTg5IDBjLTc3LjA3NDk2IDAtMTM4LjA2MS02MC44Mzg5MTgtMTM4LjA2MS0xMzcuNDcyMDEybDAtNDExLjEzNTk4OWMwLTc2LjYzMzA4MiA2MC45ODYwNC0xMzcuNDcxOTk4IDEzOC4wNjEtMTM3LjQ3MTk5OHoiIHAtaWQ9IjgzMjAiPjwvcGF0aD48L3N2Zz4=',
        properties: {collaborativeWay: '1'},
      },
      {
        type: 'serial',
        text: '',
        label: '互斥网关',
        properties: {},
        icon: 'data:image/svg+xml;charset=utf-8;base64,PHN2ZyB0PSIxNzQ4MTc1NTgyNzMwIiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjkzNjciIHdpZHRoPSI0NSIgaGVpZ2h0PSI0NSI+PHBhdGggZD0iTTAgMGgxMDI0djEwMjRIMHoiIGZpbGw9IiNGRkZGRkYiIGZpbGwtb3BhY2l0eT0iMCIgcC1pZD0iOTM2OCI+PC9wYXRoPjxwYXRoIGQ9Ik00NjUuMjI3Mjk0IDE0Mi4yNDU2NDdMMTQyLjI0NTY0NyA0NjUuMjI3Mjk0YTYxLjUwMDIzNSA2MS41MDAyMzUgMCAwIDAgMCA4Ni45Nzk3NjVsMzIyLjk4MTY0NyAzMjIuOTgxNjQ3YzI0LjAzMzg4MiAyNC4wMzM4ODIgNjIuOTQ1ODgyIDI4LjAzMzg4MiA4Ni45Nzk3NjUgMGwzMjIuOTgxNjQ3LTMyMi45ODE2NDdhNjEuNTAwMjM1IDYuNTAwMjM1IDAgMCAwIDAtODYuOTc5NzY1TDU1Mi4yMDcwNTkgMTQyLjI0NTY0N2E2MS41MDAyMzUgNjEuNTAwMjM1IDAgMCAwLTg2Ljk3OTc2NSAwIG0wOS42OTQxMTggMzcuMjU1NTI5bDMzMy4wMTE3NjQgMzIzLjAxMTc2NWE4Ljc5NDM1MyA4Ljc5NDM1MyAwIDAgMSAwIDEyLjQwODQ3MUw1MTQuOTIxNDEyIDgzNy45MzMxNzZhOC43OTQzNTMgOC43OTQzNTMgMCAwIDEtMTIuNDA4NDcxIDBMMTc5LjUwMTE3NiA1MTQuOTIxNDEyYTguNzk0MzUzIDguNzk0MzUzIDAgMCAxIDAtMTIuNDA4NDcxTDUwMi41MTI5NDEgMTc5LjUwMTE3NmE4Ljc5NDM1MyA4Ljc5NDM1MyAwIDAgMSAxMi40MDg0NzEgMHoiIGZpbGw9IiMwMDAwMDAiIHAtaWQ9IjkzNjkiPjwvcGF0aD48cGF0aCBkPSJNNTkyLjUzNDU4OCAzODUuMDg0MjM1YTI2LjM1Mjk0MSAyNi4zNTI5NDEgMCAwIDEAzOS41NzQ1ODggMzQuNjM1Mjk0bC0yLjM3OTI5NCAyLjcxMDU4OS0yMTAuODIzNTI5IDIxMC4xMzA4MjNhMjYuMzUyOTQxIDI2LjM1Mjk0MSAwIDAgMS0zOS41NDQ0NzEtMzQuNjM1Mjk0bDIuMzQ5MTc3LTIuNzEwNTg4IDIxMC44MjM1MjktMjEwLjEzMDgyNHoiIGZpbGw9IiMwMDAwMDAiIHAtaWQ9IjkzNzAiPjwvcGF0aD48cGF0aCBkPSJNMzgxLjg2MTY0NyAzODQuOTMzNjQ3YTI2LjM1Mjk0MSAyNi4zNTI5NDEgMCAwIDE0NS41NDQ5NDEtMi4zNDkxNzZsMi43MTA1ODggMi4zNDkxNzYgMjEwLjQ5MjIzNiAyMTAuNTIyMzUzYTI2LjM1Mjk0MSAyNi4zNTI5NDEgMCAwIDEtMzQuNTc1MDU5IDM5LjYwNDcwNmwtMi43MTA1ODgtMi4zNDkxNzctMjEwLjQ2MjExOC0yMTAuNTIyMzUzYTI2LjM1Mjk0MSAyNi4zNTI5NDEgMCAwMSAwLTM3LjI1NTUyOXoiIGZpbGw9IiMwMDAwMDAiIHAtaWQ9IjkzNzEiPjwvcGF0aD48L3N2Zz4=',
      },
      {
        type: 'parallel',
        text: '',
        label: '并行网关',
        properties: {},
        icon: 'data:image/svg+xml;charset=utf-8;base64,PHN2ZyB0PSIxNzQ4MTc1NjIwMDAyIiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjEwNDQxIiB3aWR0aD0iNDUiIGhlaWdodD0iNDUiPjxwYXRoIGQ9Ik0wIDBoMTAyNHYxMDI0SDB6IiBmaWxsPSIjRkZGRkZGIiBmaWxsLW9wYWNpdHk9IjAiIHAtaWQ9IjEwNDQyIj48L3BhdGg+PHBhdGggZD0iTTQ2NS4yMjcyOTQgMTQyLjI0NTY0N0wxNDIuMjQ1NjQ3IDQ2NS4yMjcyOTRhNjEuNTAwMjM1IDYxLjUwMDIzNSAwIDAgMCAwIDg2Ljk3OTc2NWwzMjIuOTgxNjQ3IDMyMi45ODE2NDdjMjQuMDMzODgyIDI0LjAzMzg4MiA2Mi45NDU4ODIgMjQuMDMzODgyIDg2Ljk3OTc2NSAwbDMyMi45ODE2NDctMzIyLjk4MTY0N2E2MS41MDAyMzUgNjEuNTAwMjM1IDAgMCAwIDAtODYuOTc5NzY1TDU1Mi4yMDcwNTkgMTQyLjI0NTY0N2E2MS41MDAyMzUgNjEuNTAwMjM1IDAgMCAwLTg2Ljk3OTc2NSAweiBtNDkuNjk0MTE4IDM3LjI1NTUyOWwzMjMuMDExNzY0IDMyMy4wMTE3NjVhOC43OTQzNTMgOC43OTQzNTMgMCAwIDEgMCAxMi40MDg0NzFMNTE0LjkyMTQxMiA4MzcuOTMzMTc2YTguNzk0MzUzIDguNzk0MzUzIDAgMCAxLTEyLjQwODQ3MSAwTDE3OS41MDExNzYgNTE0LjkyMTQxMmE4Ljc5NDM1MyA4Ljc5NDM1MyAwIDAgMSAwLTEyLjQwODQ3MUw1MDIuNTEyOTQxIDE3OS41MDExNzZhOC43OTQzNTMgOC43OTQzNTMgMCAwIDEgMTIuNDA4NDcxIDB6IiBmaWxsPSIjMDAwMDAwIiBwLWlkPSIxMDQ0MyI+PC9wYXRoPjxwYXRoIGQ9Ik01MDUuNzM1NTI5IDMzMy42NDMyOTRjMTMuNDMyNDcxIDAgMjQuNTE1NzY1IDEwLjA1OTI5NCAyNi4xNDIxMTggMjMuMDRsMC4yMTA4MjQgMy4zMTI5NDF2Mjk3LjY1MjcwNmEyNi4zNTI5NDEgMjYuMzUyOTQxIDAgMCAxLTUyLjQ5NTA1OSAzLjMxMjk0MWwtMC4yMTA4MjQtMy4zMTI5NDF2LTI5Ny42NTI3MDZjMC0xNC41NzY5NDEgMTEuNzc2LTI2LjM1Mjk0MSAyNi4zNTI5NDEtMjYuMzUyOTQxeiIgZmlsbD0iIzAwMDAwMCIgcC1pZD0iMTA0NDQiPjwvcGF0aD48cGF0aCBkPSJNNjU0LjU3Njk0MSA0ODIuNDg0NzA2YTI2LjM1Mjk0MSAyNi4zNTI5NDEgMCAwIDEgMy4zMTI5NDEgNTIuNDk1MDU5bC0zLjMxMjk0MSAwLjIxMDgyM0gzNTYuODk0MTE4YTI2LjM1Mjk0MSAyNi4zNTI5NDEgMCAwIDEtMy4zMTI5NDItNTIuNTI1MTc2bDMuMzEyOTQyLTAuMTgwNzA2aDI5Ny42ODI4MjN6IiBmaWxsPSIjMDAwMDAwIiBwLWlkPSIxMDQ0NSI+PC9wYXRoPjwvc3ZnPg==',
      },
      {
          type: 'inclusive',
          text: '',
          label: '包含网关',
          properties: {},
          icon: 'data:image/svg+xml;charset=utf-8;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIzNiIgaGVpZ2h0PSIzNiIgdmlld0JveD0iMCAwIDEwMCAxMDAiPgogIDxwYXRoIGQ9Ik01MCwwIEwxMDAsNTAgTDUwLDEwMCBMMCw1MCBaIiAKICAgICAgICBzdHJva2U9IiMwMDAwMDAiIHN0cm9rZS13aWR0aD0iNiIgZmlsbD0ibm9uZSIgLz4KICA8Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSIyNSIgCiAgICAgICAgICBzdHJva2U9IiMwMDAwMDAiIHN0cm9rZS13aWR0aD0iNiIgZmlsbD0ibm9uZSIgLz4KPC9zdmc+Cg==',
      },
      {
        type: 'end',
        text: '结束',
        label: '结束节点',
        icon: "data:image/svg+xml;charset=utf-8;base64,PHN2ZyB0PSIxNzUwMzg4OTY4OTA4IiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIg0KICAgICB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjY5MTciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIg0KICAgICB3aWR0aD0iMzYiIGhlaWdodD0iMzYiPg0KICA8cGF0aCBkPSJNNTEyLjAwNTExNyA5NTguNzA4OTcxQzI2NS42ODMwMzUgOTU4LjcwODk3MSA2NS4yOTAwMDUgNzU4LjMxNjk2NSA2NS4yOTAwMDUgNTEyLjk5Mzg2YzAtMjQ2LjMxMDgyNSAyMDAuMzkzMDMtNDQ2LjcwMzg1NSA0NDYuNzE1MTExLTQ0Ni43MDM4NTUgMjQ2LjMxMDgyNSAwIDQ0Ni43MDM4NTUgMjAwLjM5MzAzIDQ0Ni43MDM4NTUgNDQ2LjcwMzg1NUM5NTguNzA4OTcxIDc1OC4zMTY5NjUgNzU4LjMxNjk2NSA5NTguNzA4OTcxIDUxMi4wMDUxMTcgOTU4LjcwODk3MXpNNTEyLjAwNTExNyAxNjkuNzE2MzU2Yy0xODguNzM4NTk1IDAtMzQyLjI4OTc4NCAxNTMuNTQ1MDQ4LTM0Mi4yODk3ODQgMzQyLjI3NzUwNCAwIDE4OC43Mzg1OTUgMTUzLjU1MTE4OCAzNDIuMjg5Nzg0IDM0Mi4yODk7ODQgMzQyLjI4OTc4NCAxODguNzMzNDc5IDAgMzQyLjI3ODUyNy0xNTMuNTUxMTg4IDM0Mi4yNzg1MjctMzQyLjI4OTc4NEM4NTQuMjgzNjQ0IDMyMy4yNjE0MDUgNzAwLjczODU5NSAxNjkuNzE2MzU2IDUxMi4wMDUxMTcgMTY5LjcxNjM1NnoiIHAtaWQ9IjY5MTgiPjwvcGF0aD4NCjwvc3ZnPg=="
      },
    ]);
  }
}

function getBaseInfo() {
  if (onlyDesignShow.value) {
    return
  }
  const baseInfoData = proxy.$refs.baseInfoRef.getFormData();
  logicJson.value = {
    ...logicJson.value,
    ...baseInfoData
  };
}

function handleFlowNameUpdate(newName) {
  logicJson.value.flowName = newName;
}

function handleModelValueUpdate() {
  const modeOrg = logicJson.value.modelValue;
  getBaseInfo();
  const modeNew = logicJson.value.modelValue;

  if (!lf.value || modeOrg !== modeNew) {
    nextTick(() => {
      if (!jsonString.value.nodeList || jsonString.value.nodeList.length === 0) {
        let initData = isClassics(logicJson.value.modelValue) ? initClassicsData : initMimicData;
        logicJson.value = {
          ...logicJson.value,
          ...initData
        };
      }
      initLogicFlow();
    });
  }
}

async function saveJsonModel() {
  const loadingInstance = ElLoading.service(({fullscreen: true, text: "保存中，请稍等"}))
  if (!onlyDesignShow.value) {
    let validate = await proxy.$refs.baseInfoRef.validate();
    if (!validate) {
      loadingInstance.close();
      return;
    }
  }
  getBaseInfo();

  if (lf.value) {
    let graphData = lf.value.getGraphData()
    logicJson.value['nodes'] = graphData['nodes']
    logicJson.value['edges'] = graphData['edges']
  }
  logicJson.value['id'] = definitionId.value

  let warmFlowJson = logicFlowJsonToWarmFlow(logicJson.value);
  saveJson(warmFlowJson, onlyDesignShow.value).then(response => {
    if (response.code === 200) {
      proxy.$modal.msgSuccess("保存成功");
      setTimeout(() => {
        close()
      }, 500)
    }
  }).finally(() => {
    nextTick(() => {
      loadingInstance.close();
    });
  });
}

/**
 * Initialize menu
 */
function initMenu() {
  if (!isClassics(logicJson.value.modelValue)) {
    lf.value.extension.menu.setMenuConfig({
      nodeMenu: [],
      edgeMenu: [],
    });
  }
}

/**
 * Register custom nodes and edges
 */
function register() {
  if (isClassics(logicJson.value.modelValue)) {
    lf.value.register(StartC);
    lf.value.register(BetweenC);
    lf.value.register(SerialC);
    lf.value.register(ParallelC);
    lf.value.register(InclusiveC);
    lf.value.register(EndC);
    lf.value.register(SkipC);
  } else {
    lf.value.register(StartM);
    lf.value.register(BetweenM);
    lf.value.register(SerialM);
    lf.value.register(ParallelM);
    lf.value.register(InclusiveM);
    lf.value.register(EndM);
    lf.value.register(SkipM);
  }
}

/**
 * Add extensions
 */
let extensionsRegistered = false;
let previousBodyOverflow = '';

function use() {
  if (extensionsRegistered) return;
  if (isClassics(logicJson.value.modelValue)) {
    LogicFlow.use(DndPanel);
    LogicFlow.use(InsertNodeInPolyline)
  }
  LogicFlow.use(Menu);
  LogicFlow.use(Snapshot);
  extensionsRegistered = true;
}

function initEvent() {
  const { eventCenter } = lf.value.graphModel

  if (!isClassics(logicJson.value.modelValue)) {
    eventCenter.on('update:nodeName', (data) => {
      lf.value.updateText(data.id, data.nodeName)
      lf.value.setProperties(data.id, {
        nodeName: data.nodeName
      })
    })

    eventCenter.on('node:click', (args) => {
      nodeClick.value = args.data
      if (isGateWay(nodeClick.value.type)) {
        gatewayAddNode(lf.value, nodeClick.value);
      }
    })

    eventCenter.on('edit:node', (args) => {
      if (args.click) {
        nodeClick.value = lf.value.getNodeModelById(args.id)
        let graphData = lf.value.getGraphData()
        nodes.value = graphData['nodes']
        skips.value = graphData['edges']
        proxy.$nextTick(() => {
          propertySettingRef.value.show()
        })
      }
    })

    eventCenter.on('show:EdgeSetting', (args) => {
      nodeClick.value = lf.value.getEdgeModelById(args.id)
      const nodeModel = lf.value.getNodeModelById(nodeClick.value.sourceNodeId);
      skipConditionShow.value = ['serial', 'inclusive'].includes(nodeModel['type'])
      let graphData = lf.value.getGraphData()
      nodes.value = graphData['nodes']
      skips.value = graphData['edges']
      proxy.$nextTick(() => {
        propertySettingRef.value.show(['serial', 'inclusive'].includes(nodeModel['type']))
      })
    });

    eventCenter.on('show:EdgeTooltip', (args) => {
      tooltipVisible.value = true;
      tooltipPosition.value = { x: args.e.clientX, y: args.e.clientY };
      tooltipEdge.value = lf.value.getEdgeModelById(args.id)
    });
    eventCenter.on('hide:EdgeTooltip', () => {
      tooltipVisible.value = false;
    });
    eventCenter.on('delete:node', (args) => {
      const nodeModel = lf.value.getNodeModelById(args.id)
      removeNode(lf.value, nodeModel)
    })
  } else {
    eventCenter.on('node:dbclick', (args) => {
      nodeClick.value = args.data
      let graphData = lf.value.getGraphData()
      nodes.value = graphData['nodes']
      skips.value = graphData['edges']
      proxy.$nextTick(() => {
        propertySettingRef.value.show()
      })
    })

    eventCenter.on('edge:dbclick', (args) => {
      nodeClick.value = args.data
      const nodeModel = lf.value.getNodeModelById(nodeClick.value.sourceNodeId);
      skipConditionShow.value = ['serial', 'inclusive'].includes(nodeModel['type'])
      let graphData = lf.value.getGraphData()
      nodes.value = graphData['nodes']
      skips.value = graphData['edges']
      proxy.$nextTick(() => {
        propertySettingRef.value.show(['serial', 'inclusive'].includes(nodeModel['type']))
      })
    })
  }

  eventCenter.on('edge:add', (args) => {
    let graphData = lf.value.getGraphData()
    const previousNodes = getPreviousNodes(graphData['nodes'], graphData['edges'], args.data.sourceNodeId);
    lf.value.changeEdgeType(args.data.id, 'skip')
    lf.value.setProperties(args.data.id, {
      skipType: previousNodes.some(node => node.id === args.data.targetNodeId) ? 'REJECT' : 'PASS'
    })
  })

  eventCenter.on('blank:click', () => {
    nodeClick.value = null
    proxy.$nextTick(() => {
      propertySettingRef.value.handleClose()
    })
  })
}

/** Close button */
function close() {
  router.push('/flow/definition');
}

const zoomViewport = async (zoom) => {
  lf.value.zoom(zoom);
  if (isClassics(logicJson.value.modelValue)) {
    lf.value.translateCenter();
  }
};

const undoOrRedo = async (undo) => {
  if(undo){
    lf.value.undo(undo)
  }else{
    lf.value.redo(undo)
  }
};

const clear = async () => {
    if (isClassics(logicJson.value.modelValue)) {
        lf.value.clearData()
    } else {
        logicJson.value = {
            ...logicJson.value,
            ...initMimicData
        };
        lf.value.render(logicJson.value);
    }
}

/**
 * Download flow chart
 */
function downLoad() {
  lf.value.getSnapshot(logicJson.value.flowName, {
    fileType: 'png',
    backgroundColor: '#fff',
  })
}

async function downJson() {
  try {
    getBaseInfo();
    if (lf.value) {
      let graphData = lf.value.getGraphData()
      logicJson.value['nodes'] = graphData['nodes']
      logicJson.value['edges'] = graphData['edges']
    }
    let warmFlowJson = logicFlowJsonToWarmFlow(logicJson.value);

    const filename = `${logicJson.value.flowName}_${logicJson.value.version}.json`;
    const jsonPretty = JSON.stringify(JSON.parse(warmFlowJson), null, 2);
    const blob = new Blob([jsonPretty], { type: 'application/json' });
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(downloadUrl);
  } catch (error) {
    console.error('下载失败:', error);
  }
}

// Initialize on mount
onMounted(() => {
  previousBodyOverflow = document.body.style.overflow;
  document.body.style.overflow = 'hidden';

  // Fetch URL params
  const urlParams = new URLSearchParams(window.location.search);
  const params = {};
  for (const [key, value] of urlParams.entries()) {
    if (value && value !== 'undefined') {
      params[key] = value;
    }
  }
  appStore.appParams = params;

  // Support both URL query params and appStore params
  const id = route.query.id || params.id;
  if (id) {
    definitionId.value = id;
  }
  onlyDesignShow.value = route.query.onlyDesignShow === 'true' ||
      params.onlyDesignShow === 'true';

  // Fetch definition data
  (async () => {
    try {
      if (definitionId.value) {
        const detailRes = await getFlowDefinitionById(definitionId.value);
        if (!detailRes || !detailRes.data) {
          ElMessage.warning('流程定义不存在，已加载空白设计器');
          definitionId.value = undefined;
        }
      }

      const res = await queryDef(definitionId.value);
      if (res && res.data) {
        jsonString.value = res.data;
        logicJson.value = json2LogicFlowJson(res.data);
      }
      initLogicFlow();
    } catch (err) {
      console.error('获取流程定义失败:', err);
      // Initialize with empty data if fetch fails
      handleModelValueUpdate();
    }
  })();

  // Listen for theme messages
  window.addEventListener("message", listeningMessage);
});

onUnmounted(() => {
  window.removeEventListener("message", listeningMessage);
  document.body.style.overflow = previousBodyOverflow;
});

</script>

<style>
.container {
  flex: 1;
  width: 100%;
  height: calc(100vh - 100px);
  min-height: 400px;
}

.logo-text {
  position: absolute;
  font-weight: bold;
  right: 50px;
  bottom: 10px;
  font-size: 15px;
  color: #333;
  z-index: 1;
}

.flow-name {
  font-weight: bold;
  padding-left: 40px;
  padding-right: 200px;
  width: 500px;
  max-width: 500px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #333;
}

.steps-container {
  display: flex;
  align-items: center;
  height: 35px;
  justify-content: space-between;
}

.steps {
  display: flex;
}

.step-item {
  display: flex;
  align-items: center;
  margin-right: 100px;
  cursor: pointer;
}

.step-item i {
  margin-right: 5px;
}

.step-item.active {
  color: #409eff;
}
</style>
