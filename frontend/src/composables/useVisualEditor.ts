import { onBeforeUnmount, ref, watch, type Ref } from 'vue'

export interface VisualEditorSelectionInfo {
  tagName: string
  selector: string
  content: string
  id?: string
  className?: string
}

const VISUAL_EDITOR_MESSAGE_TYPE = 'ai-zero-code-platform:visual-editor'

const buildVisualEditorBootstrapScript = () => {
  const messageType = JSON.stringify(VISUAL_EDITOR_MESSAGE_TYPE)

  return `
(function () {
  var MESSAGE_TYPE = ${messageType};
  var STYLE_ID = 'ai-zero-code-platform-visual-editor-style';
  var HOVER_CLASS = 'ai-zero-code-platform-visual-editor-hover';
  var SELECTED_CLASS = 'ai-zero-code-platform-visual-editor-selected';
  var IGNORED_TAGS = {
    html: true,
    body: true,
    head: true,
    script: true,
    style: true,
    link: true,
    meta: true,
    title: true,
    noscript: true
  };

  var active = false;
  var selectedElement = null;
  var hoverElement = null;
  var attachedDocument = null;

  var toText = function (value, limit) {
    var text = String(value || '').replace(/\\s+/g, ' ').trim();
    if (!limit || text.length <= limit) {
      return text;
    }
    return text.slice(0, limit - 3) + '...';
  };

  var escapeSelectorPart = function (value) {
    return String(value).replace(/[^a-zA-Z0-9_-]/g, '\\\\$&');
  };

  var isIgnoredElement = function (element) {
    if (!element || !element.tagName) {
      return true;
    }
    var tagName = element.tagName.toLowerCase();
    return !!IGNORED_TAGS[tagName];
  };

  var clearHover = function () {
    if (hoverElement) {
      hoverElement.classList.remove(HOVER_CLASS);
      hoverElement = null;
    }
  };

  var clearSelection = function () {
    if (selectedElement) {
      selectedElement.classList.remove(SELECTED_CLASS);
      selectedElement = null;
    }
  };

  var getDirectIndex = function (element) {
    if (!element || !element.parentElement) {
      return 1;
    }

    return Array.prototype.indexOf.call(element.parentElement.children, element) + 1;
  };

  var buildSelectorPath = function (element) {
    var segments = [];
    var current = element;

    while (current && current.nodeType === 1) {
      var tagName = current.tagName.toLowerCase();
      if (tagName === 'html') {
        segments.unshift('html');
        break;
      }

      var segment = tagName;

      if (current.id) {
        segment += '#' + escapeSelectorPart(current.id);
        segments.unshift(segment);
        break;
      }

      segment += ':nth-child(' + getDirectIndex(current) + ')';
      segments.unshift(segment);

      current = current.parentElement;

      if (segments.length >= 6) {
        break;
      }
    }

    return segments.join(' > ');
  };

  var describeElement = function (element) {
    var className = '';
    if (element.className && typeof element.className === 'string') {
      className = element.className.trim();
    }

    return {
      tagName: element.tagName.toLowerCase(),
      selector: buildSelectorPath(element),
      content: toText(
        element.innerText ||
          element.textContent ||
          element.getAttribute('aria-label') ||
          element.getAttribute('alt') ||
          element.getAttribute('title') ||
          element.getAttribute('placeholder') ||
          '',
        160,
      ),
      id: element.id || '',
      className: className
    };
  };

  var ensureStyle = function () {
    if (!attachedDocument || attachedDocument.getElementById(STYLE_ID)) {
      return;
    }

    var styleElement = attachedDocument.createElement('style');
    styleElement.id = STYLE_ID;
    styleElement.textContent = [
      '.' + HOVER_CLASS + ' {',
      '  outline: 2px solid #52c41a !important;',
      '  outline-offset: 2px !important;',
      '  cursor: crosshair !important;',
      '}',
      '.' + SELECTED_CLASS + ' {',
      '  outline: 3px solid #1677ff !important;',
      '  outline-offset: 2px !important;',
      '  cursor: crosshair !important;',
      '  transition: outline-color 0.15s ease, outline-width 0.15s ease;',
      '}',
    ].join('\\n');

    var head = attachedDocument.head || attachedDocument.documentElement;
    head.appendChild(styleElement);
  };

  var postSelection = function (selection) {
    window.parent.postMessage(
      {
        type: MESSAGE_TYPE,
        action: 'selection',
        selection: selection
      },
      '*',
    );
  };

  var selectElement = function (element) {
    if (!element || isIgnoredElement(element)) {
      return;
    }

    clearHover();
    clearSelection();
    selectedElement = element;
    selectedElement.classList.add(SELECTED_CLASS);
    postSelection(describeElement(selectedElement));
  };

  var selectBySelector = function (selector) {
    if (!attachedDocument || !selector) {
      return;
    }

    var element = attachedDocument.querySelector(selector);
    if (element && element instanceof Element) {
      selectElement(element);
    }
  };

  var resolveTargetElement = function (target) {
    var element = target;

    if (!element) {
      return null;
    }

    if (!(element instanceof Element)) {
      if (element.parentElement) {
        element = element.parentElement;
      } else {
        return null;
      }
    }

    while (element && element instanceof Element) {
      if (!isIgnoredElement(element)) {
        return element;
      }
      element = element.parentElement;
    }

    return null;
  };

  var handleMouseOver = function (event) {
    if (!active) {
      return;
    }

    var target = resolveTargetElement(event.target);
    if (!target || target === selectedElement) {
      return;
    }

    clearHover();
    hoverElement = target;
    hoverElement.classList.add(HOVER_CLASS);
  };

  var handleMouseOut = function (event) {
    if (!active || !hoverElement) {
      return;
    }

    if (event.relatedTarget && hoverElement.contains(event.relatedTarget)) {
      return;
    }

    clearHover();
  };

  var handleClick = function (event) {
    if (!active) {
      return;
    }

    var target = resolveTargetElement(event.target);
    if (!target) {
      return;
    }

    event.preventDefault();
    event.stopPropagation();
    selectElement(target);
  };

  var handleMessage = function (event) {
    var data = event.data || {};
    if (!data || data.type !== MESSAGE_TYPE) {
      return;
    }

    if (data.action === 'set-active') {
      active = !!data.active;
      if (!active) {
        clearHover();
      }
      return;
    }

    if (data.action === 'clear-selection') {
      clearHover();
      clearSelection();
      return;
    }

    if (data.action === 'select-by-selector' && typeof data.selector === 'string') {
      selectBySelector(data.selector);
    }
  };

  var attach = function () {
    if (!attachedDocument) {
      return;
    }

    ensureStyle();
    attachedDocument.addEventListener('mouseover', handleMouseOver, true);
    attachedDocument.addEventListener('mouseout', handleMouseOut, true);
    attachedDocument.addEventListener('click', handleClick, true);
    window.addEventListener('message', handleMessage);
  };

  var detach = function () {
    if (!attachedDocument) {
      return;
    }

    attachedDocument.removeEventListener('mouseover', handleMouseOver, true);
    attachedDocument.removeEventListener('mouseout', handleMouseOut, true);
    attachedDocument.removeEventListener('click', handleClick, true);
    window.removeEventListener('message', handleMessage);
    clearHover();
    clearSelection();
  };

  var boot = function () {
    attachedDocument = document;
    attach();
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot, { once: true });
  } else {
    boot();
  }

  window.addEventListener('beforeunload', detach);
})();
`
}

export const buildEditablePreviewHtml = (html: string, previewRootUrl: string) => {
  const safeBaseHref = previewRootUrl.replace(/"/g, '&quot;')
  const withBase = /<base[\s>]/i.test(html)
    ? html
    : /<head[\s>]/i.test(html)
      ? html.replace(/<head([^>]*)>/i, `<head$1><base href="${safeBaseHref}">`)
      : `<head><base href="${safeBaseHref}"></head>${html}`
  const bootstrapScript = `<script>${buildVisualEditorBootstrapScript()}</script>`

  if (/<\/body>/i.test(withBase)) {
    return withBase.replace(/<\/body>/i, `${bootstrapScript}</body>`)
  }

  return `${withBase}${bootstrapScript}`
}

export const formatVisualEditorSelectionPrompt = (selection: VisualEditorSelectionInfo) => {
  const lines = [
    '[选中的网页元素]',
    `元素: ${selection.tagName}${selection.id ? `#${selection.id}` : ''}`,
    `内容: ${selection.content || '（无）'}`,
    `选择器: ${selection.selector}`,
  ]

  if (selection.className) {
    lines.push(`类名: ${selection.className}`)
  }

  return lines.join('\n')
}

export const useVisualEditor = (previewFrameRef: Ref<HTMLIFrameElement | null>) => {
  const isEditing = ref(false)
  const selectedElement = ref<VisualEditorSelectionInfo | null>(null)

  const postControlMessage = (payload: {
    action: 'set-active' | 'clear-selection' | 'select-by-selector'
    active?: boolean
    selector?: string
  }) => {
    previewFrameRef.value?.contentWindow?.postMessage(
      {
        type: VISUAL_EDITOR_MESSAGE_TYPE,
        ...payload,
      },
      '*',
    )
  }

  const syncFrameState = () => {
    postControlMessage({
      action: 'set-active',
      active: isEditing.value,
    })

    if (selectedElement.value) {
      postControlMessage({
        action: 'select-by-selector',
        selector: selectedElement.value.selector,
      })
    }
  }

  const clearSelectedElement = () => {
    selectedElement.value = null
    postControlMessage({
      action: 'clear-selection',
    })
  }

  const setEditing = (nextValue: boolean) => {
    isEditing.value = nextValue
    if (!nextValue) {
      postControlMessage({
        action: 'clear-selection',
      })
      selectedElement.value = null
    }
    syncFrameState()
  }

  const toggleEditing = () => {
    setEditing(!isEditing.value)
  }

  const disableEditing = () => {
    setEditing(false)
  }

  const handleMessage = (event: MessageEvent) => {
    const data = event.data as
      | {
          type?: string
          action?: string
          selection?: VisualEditorSelectionInfo
        }
      | undefined

    if (!data || data.type !== VISUAL_EDITOR_MESSAGE_TYPE) {
      return
    }

    if (data.action === 'selection' && data.selection && isEditing.value) {
      selectedElement.value = data.selection
    }
  }

  const stopWatchingFrame = watch(
    previewFrameRef,
    (frame, _, onCleanup) => {
      if (!frame) {
        return
      }

      const handleLoad = () => {
        syncFrameState()
      }

      frame.addEventListener('load', handleLoad)
      onCleanup(() => {
        frame.removeEventListener('load', handleLoad)
      })

      if (frame.contentDocument?.readyState === 'complete' || frame.contentDocument?.readyState === 'interactive') {
        queueMicrotask(syncFrameState)
      }
    },
    { immediate: true },
  )

  window.addEventListener('message', handleMessage)

  onBeforeUnmount(() => {
    stopWatchingFrame()
    window.removeEventListener('message', handleMessage)
  })

  return {
    isEditing,
    selectedElement,
    toggleEditing,
    setEditing,
    disableEditing,
    clearSelectedElement,
    syncFrameState,
  }
}
