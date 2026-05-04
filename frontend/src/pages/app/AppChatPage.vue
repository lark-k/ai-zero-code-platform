<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'

import { pageQuery } from '@/api/duihualishixiangguanjiekou.ts'
import { appDeploy, cancelDeploy, getAppVoById } from '@/api/yingyongmokuaixiangguanjiekou.ts'
import logoUrl from '@/assets/logo.png'
import {
  buildEditablePreviewHtml,
  formatVisualEditorSelectionPrompt,
  useVisualEditor,
} from '@/composables/useVisualEditor.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'

interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  createTime?: string
  renderedHtml?: string
  rawContent?: string
  pendingContent?: string
  previewOnComplete?: boolean
  streamFinished?: boolean
  flushFrameId?: number
  streaming?: boolean
}

interface StructuredStreamPayload {
  v?: string
  type?: string
  data?: string
  id?: string
  name?: string
  arguments?: string
  result?: string
}

interface ApiErrorPayload {
  code?: number
  data?: unknown
  message?: string
}

const API_BASE_URL = 'http://localhost:8123/api'
const HISTORY_PAGE_SIZE = 20
const PREVIEW_POLL_DELAY = 2000
const PREVIEW_POLL_MAX_ATTEMPTS = 45

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const app = ref<API.AppVO>()
const loadingApp = ref(false)
const loadingHistory = ref(false)
const loadingMoreHistory = ref(false)
const sending = ref(false)
const deploying = ref(false)
const canceling = ref(false)
const inputMessage = ref('')
const messages = ref<ChatMessage[]>([])
const historyCursor = ref<string>()
const hasMoreHistory = ref(false)
const previewReady = ref(false)
const previewHtml = ref('')
const previewRefreshing = ref(false)
const previewError = ref('')
const previewVersion = ref(Date.now())
const messageListRef = ref<HTMLElement>()
const previewFrameRef = ref<HTMLIFrameElement | null>(null)

let streamAbortController: AbortController | null = null
let previewRequestId = 0

const appId = computed(() => String(route.params.id || ''))
const isVueProjectMode = computed(() => app.value?.codeGenType === 'vue_project')
const appName = computed(() => app.value?.appName || '\u672a\u547d\u540d\u5e94\u7528')
const appAuthorInitial = computed(() => {
  const displayName = app.value?.userVo?.userName || app.value?.userVo?.userAccount || 'A'
  return displayName.trim().charAt(0).toUpperCase() || 'A'
})
const generationModeLabel = computed(() => {
  switch (app.value?.codeGenType) {
    case 'vue_project':
      return 'Vue \u9879\u76ee'
    case 'multi_file':
      return '\u591a\u6587\u4ef6'
    case 'html':
      return 'HTML'
    default:
      return '\u4ee3\u7801\u751f\u6210'
  }
})
const generationModeDescription = computed(() =>
  isVueProjectMode.value
    ? 'Vue \u6a21\u5f0f\u4f1a\u5148\u5199\u5165\u6587\u4ef6\uff0c\u518d\u7b49\u5f85\u540e\u7aef\u6784\u5efa\u5b8c\u6210\u540e\u663e\u793a\u9884\u89c8\u3002'
    : '\u6d41\u5f0f\u54cd\u5e94\u7ed3\u675f\u540e\u4f1a\u81ea\u52a8\u5237\u65b0\u9884\u89c8\u3002',
)
const streamStatusLabel = computed(() => {
  if (sending.value) {
    return isVueProjectMode.value ? 'AI \u6b63\u5728\u5199\u5165\u9879\u76ee\u6587\u4ef6' : 'AI \u6b63\u5728\u751f\u6210\u9875\u9762'
  }
  if (previewRefreshing.value) {
    return '\u6b63\u5728\u5237\u65b0\u9884\u89c8'
  }
  return '\u5c31\u7eea'
})
const isOwner = computed(
  () => Boolean(loginUserStore.loginUser.id) && app.value?.userId === loginUserStore.loginUser.id,
)
const canManage = computed(() => loginUserStore.isAdmin || isOwner.value)
const isDeployed = computed(() => Boolean(app.value?.deployKey))
const deployedUrl = computed(() => (app.value?.deployKey ? `http://localhost/${app.value.deployKey}` : ''))
const previewRootUrl = computed(() => `/api/static/${appId.value}/`)
const previewIframeSrc = computed(() =>
  isVueProjectMode.value ? `/api/static/${appId.value}/?t=${previewVersion.value}` : '',
)
const {
  isEditing: isVisualEditing,
  selectedElement: selectedVisualElement,
  toggleEditing: toggleVisualEditing,
  disableEditing: disableVisualEditing,
  clearSelectedElement,
  syncFrameState: syncVisualEditorFrame,
} = useVisualEditor(previewFrameRef)
const previewTitle = computed(() => {
  if (previewError.value && !sending.value && !previewRefreshing.value) {
    return '\u9884\u89c8\u4e0d\u53ef\u7528'
  }
  if (sending.value) {
    return isVueProjectMode.value ? '\u6b63\u5728\u6784\u5efa\u9884\u89c8...' : '\u6b63\u5728\u66f4\u65b0\u9884\u89c8...'
  }
  return '\u9884\u89c8'
})
const previewDescription = computed(() => {
  if (previewError.value && !sending.value && !previewRefreshing.value) {
    return previewError.value
  }
  return isVueProjectMode.value
    ? '\u540e\u7aef\u5b8c\u6210 npm install / npm run build \u4e14\u751f\u6210\u6587\u4ef6\u53ef\u8bbf\u95ee\u540e\uff0c\u8fd9\u91cc\u4f1a\u663e\u793a\u9884\u89c8\u3002'
    : '\u4ee3\u7801\u751f\u6210\u5b8c\u6210\u540e\uff0c\u8fd9\u91cc\u4f1a\u663e\u793a\u9884\u89c8\u3002'
})
const selectedVisualElementLabel = computed(() =>
  selectedVisualElement.value
    ? `${selectedVisualElement.value.tagName}${selectedVisualElement.value.id ? `#${selectedVisualElement.value.id}` : ''}`
    : '',
)
const selectedVisualElementDescription = computed(() => {
  if (!selectedVisualElement.value) {
    return ''
  }

  const lines = [
    `内容：${selectedVisualElement.value.content || '（无）'}`,
    `选择器：${selectedVisualElement.value.selector}`,
  ]

  if (selectedVisualElement.value.className) {
    lines.push(`类名：${selectedVisualElement.value.className}`)
  }

  return lines.join('\n')
})
const inputHintText = computed(() => {
  if (isVisualEditing.value) {
    return '在右侧预览中点击元素即可选中，发送后会自动退出编辑模式。'
  }

  return isVueProjectMode.value
    ? 'Vue \u6a21\u5f0f\u9700\u8981\u7b49\u5f85\u540e\u7aef\u6784\u5efa\u5b8c\u6210\u540e\uff0c\u9884\u89c8\u624d\u53ef\u8bbf\u95ee\u3002'
    : '\u6309 Ctrl + Enter \u53ef\u5feb\u901f\u53d1\u9001\u3002'
})

watch(
  [isVisualEditing, previewReady, previewVersion],
  async ([editing, ready]) => {
    if (!editing || !ready) {
      return
    }

    await nextTick()
    syncVisualEditorFrame()
  },
  { flush: 'post' },
)
const hasHtmlCodeBlock = (content: string) => /```html\s*[\r\n]/i.test(content)
const hasMultiFileCodeBlocks = (content: string) =>
  /```html\s*[\r\n]/i.test(content) &&
  /```css\s*[\r\n]/i.test(content) &&
  /```(?:js|javascript)\s*[\r\n]/i.test(content)

const shouldRefreshPreviewFromResponse = (content: string) => {
  const normalizedContent = content.trim()
  if (!normalizedContent) {
    return false
  }

  if (app.value?.codeGenType === 'vue_project') {
    return (
      /\[tool\]\s*write file/i.test(normalizedContent) ||
      /\[瀹搞儱鍙跨拫鍐暏\]\s*閸愭瑥鍙嗛弬鍥︽/i.test(normalizedContent) ||
      /```(?:vue|js|jsx|ts|tsx|css|scss|less|html|json)\s*[\r\n]/i.test(normalizedContent)
    )
  }

  if (app.value?.codeGenType === 'multi_file') {
    return hasMultiFileCodeBlocks(normalizedContent)
  }

  return hasHtmlCodeBlock(normalizedContent)
}

const wait = (ms: number) => new Promise((resolve) => window.setTimeout(resolve, ms))

const rewritePreviewHtml = (html: string) => {
  const previewRoot = previewRootUrl.value
  const withBase = /<head[\s>]/i.test(html)
    ? html.replace(/<head([^>]*)>/i, `<head$1><base href="${previewRoot}">`)
    : `<head><base href="${previewRoot}"></head>${html}`

  return withBase
    .replace(/((?:src|href|poster|action)=["'])\/(?!\/)/gi, `$1${previewRoot}`)
    .replace(/url\((['"]?)\/(?!\/)/gi, `url($1${previewRoot}`)
}

const refreshPreview = async ({
  poll = false,
  preserveCurrent = false,
  silent = false,
}: {
  poll?: boolean
  preserveCurrent?: boolean
  silent?: boolean
} = {}) => {
  if (!appId.value) {
    return false
  }

  const requestId = ++previewRequestId
  const maxAttempts = poll ? PREVIEW_POLL_MAX_ATTEMPTS : 1
  let lastErrorMessage = ''

  previewRefreshing.value = true
  previewError.value = ''

  if (!preserveCurrent) {
    previewReady.value = false
    previewHtml.value = ''
  }

  try {
    for (let attempt = 1; attempt <= maxAttempts; attempt += 1) {
      try {
        const response = await fetch(`${previewRootUrl.value}?t=${Date.now()}`, {
          method: 'GET',
          credentials: 'include',
        })

        if (response.status === 404) {
          throw new Error('PREVIEW_NOT_READY')
        }

        if (!response.ok) {
          throw new Error(`Preview load failed (${response.status})`)
        }

        const html = await response.text()
        if (!html.trim()) {
          throw new Error('PREVIEW_NOT_READY')
        }

        if (requestId !== previewRequestId) {
          return false
        }

        const baseHtml = rewritePreviewHtml(html)
        previewHtml.value = buildEditablePreviewHtml(baseHtml, previewRootUrl.value)
        previewReady.value = true
        previewVersion.value = Date.now()
        previewError.value = ''
        return true
      } catch (error) {
        lastErrorMessage = error instanceof Error ? error.message : 'Preview load failed'

        if (lastErrorMessage === 'PREVIEW_NOT_READY' && attempt < maxAttempts) {
          await wait(PREVIEW_POLL_DELAY)
          continue
        }

        break
      }
    }

    if (requestId !== previewRequestId) {
      return false
    }

    if (!preserveCurrent) {
      previewReady.value = false
      previewHtml.value = ''
    }

    previewError.value =
      lastErrorMessage === 'PREVIEW_NOT_READY'
        ? isVueProjectMode.value
          ? 'Preview is still building. Try reloading in a moment.'
          : 'Preview files have not been generated yet.'
        : lastErrorMessage

    if (!silent && previewError.value) {
      message.warning(previewError.value)
    }

    return false
  } finally {
    if (requestId === previewRequestId) {
      previewRefreshing.value = false
    }
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const loadApp = async () => {
  if (!appId.value) {
    message.error('Invalid app id')
    await router.push('/')
    return
  }

  loadingApp.value = true
  try {
    const res = await getAppVoById({ id: appId.value as unknown as number })
    if (res.data.code !== 0 || !res.data.data) {
      message.error(res.data.message || 'Failed to load app info')
      await router.push('/')
      return
    }
    app.value = res.data.data
  } catch (error) {
    message.error(error instanceof Error ? error.message : 'Failed to load app info')
  } finally {
    loadingApp.value = false
  }
}

const closeStream = () => {
  if (streamAbortController) {
    streamAbortController.abort()
    streamAbortController = null
  }
}

const getChatRole = (messageType?: string): ChatMessage['role'] =>
  messageType === 'user' ? 'user' : 'assistant'

const toChatMessage = (record: API.ChatHistory): ChatMessage => ({
  id: String(record.id ?? `${record.createTime}-${record.messageType}`),
  role: getChatRole(record.messageType),
  content: record.message || '',
  createTime: record.createTime,
  renderedHtml:
    getChatRole(record.messageType) === 'assistant' ? renderMarkdown(record.message || '') : undefined,
})

const appendHistoryMessages = (records: API.ChatHistory[], mode: 'replace' | 'prepend') => {
  const sortedMessages = [...records]
    .sort((a, b) => new Date(a.createTime || 0).getTime() - new Date(b.createTime || 0).getTime())
    .map(toChatMessage)

  if (mode === 'replace') {
    messages.value = sortedMessages
    return
  }

  const existingIds = new Set(messages.value.map((item) => item.id))
  const nextMessages = sortedMessages.filter((item) => !existingIds.has(item.id))
  messages.value = [...nextMessages, ...messages.value]
}

const loadHistory = async (loadMore = false) => {
  if (!appId.value || !loginUserStore.loginUser.id) {
    return
  }

  const previousScrollHeight = messageListRef.value?.scrollHeight ?? 0
  const previousScrollTop = messageListRef.value?.scrollTop ?? 0

  if (loadMore) {
    if (!hasMoreHistory.value || loadingMoreHistory.value) {
      return
    }
    loadingMoreHistory.value = true
  } else {
    loadingHistory.value = true
    historyCursor.value = undefined
    hasMoreHistory.value = false
  }

  try {
    const res = await pageQuery({
      appId: appId.value,
      userId: loginUserStore.loginUser.id,
      pageSize: HISTORY_PAGE_SIZE,
      ...(loadMore && historyCursor.value ? { lastCreateTime: historyCursor.value } : {}),
    })

    if (res.data.code !== 0) {
      message.error(res.data.message || 'Failed to load chat history')
      return
    }

    const records = res.data.data?.records || []
    appendHistoryMessages(records, loadMore ? 'prepend' : 'replace')
    const oldestRecord = records[records.length - 1]
    historyCursor.value = oldestRecord?.createTime
    hasMoreHistory.value = records.length >= HISTORY_PAGE_SIZE

    if (!loadMore) {
      await scrollToBottom()
    } else {
      await nextTick()
      if (messageListRef.value) {
        const nextScrollHeight = messageListRef.value.scrollHeight
        messageListRef.value.scrollTop = nextScrollHeight - previousScrollHeight + previousScrollTop
      }
    }
  } catch (error) {
    message.error(error instanceof Error ? error.message : 'Failed to load chat history')
  } finally {
    if (loadMore) {
      loadingMoreHistory.value = false
    } else {
      loadingHistory.value = false
    }
  }
}

const escapeHtml = (value: string) =>
  value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

const normalizeLanguage = (language = '') => language.trim().toLowerCase() || 'text'

const highlightCode = (code: string, language = '') => {
  const lang = normalizeLanguage(language)
  const html = escapeHtml(code)

  if (['html', 'xml', 'vue'].includes(lang)) {
    return html
      .replace(/(&lt;!--[\s\S]*?--&gt;)/g, '<span class="token-comment">$1</span>')
      .replace(/(&lt;\/?)([a-zA-Z][\w:-]*)/g, '$1<span class="token-tag">$2</span>')
      .replace(
        /(\s)([a-zA-Z_:][\w:.-]*)(=)(&quot;.*?&quot;|&#39;.*?&#39;)/g,
        '$1<span class="token-attr">$2</span>$3<span class="token-string">$4</span>',
      )
  }

  if (['css', 'scss', 'less'].includes(lang)) {
    return html
      .replace(/(\/\*[\s\S]*?\*\/)/g, '<span class="token-comment">$1</span>')
      .replace(/([.#]?[a-zA-Z][\w-]*)(\s*\{)/g, '<span class="token-selector">$1</span>$2')
      .replace(/([a-zA-Z-]+)(\s*:)/g, '<span class="token-attr">$1</span>$2')
      .replace(/(:\s*)([^;{}]+)(;?)/g, '$1<span class="token-string">$2</span>$3')
  }

  if (['js', 'jsx', 'ts', 'tsx', 'javascript', 'typescript'].includes(lang)) {
    return html
      .replace(/(\/\/.*|\/\*[\s\S]*?\*\/)/g, '<span class="token-comment">$1</span>')
      .replace(/(&quot;.*?&quot;|&#39;.*?&#39;|`.*?`)/g, '<span class="token-string">$1</span>')
      .replace(
        /\b(const|let|var|function|return|if|else|for|while|class|new|import|from|export|async|await|try|catch|throw|true|false|null|undefined)\b/g,
        '<span class="token-keyword">$1</span>',
      )
      .replace(/\b(\d+(?:\.\d+)?)\b/g, '<span class="token-number">$1</span>')
  }

  return html
}

const renderInlineMarkdown = (value: string) => {
  const parts = value.split(/(`[^`]+`)/g)
  return parts
    .map((part) => {
      if (part.startsWith('`') && part.endsWith('`')) {
        return `<code>${escapeHtml(part.slice(1, -1))}</code>`
      }
      return escapeHtml(part)
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.+?)\*/g, '<em>$1</em>')
    })
    .join('')
}

const renderMarkdownText = (text: string) => {
  const lines = text.split('\n')
  const html: string[] = []
  let paragraph: string[] = []
  let listType: 'ul' | 'ol' | null = null

  const closeParagraph = () => {
    if (!paragraph.length) {
      return
    }
    html.push(`<p>${paragraph.map(renderInlineMarkdown).join('<br />')}</p>`)
    paragraph = []
  }

  const closeList = () => {
    if (!listType) {
      return
    }
    html.push(`</${listType}>`)
    listType = null
  }

  lines.forEach((line) => {
    if (!line.trim()) {
      closeParagraph()
      closeList()
      return
    }

    const headingMatch = line.match(/^(#{1,6})\s+(.+)$/)
    if (headingMatch) {
      closeParagraph()
      closeList()
      const level = Math.min(headingMatch[1].length, 4)
      html.push(`<h${level}>${renderInlineMarkdown(headingMatch[2])}</h${level}>`)
      return
    }

    const unorderedMatch = line.match(/^\s*[-*]\s+(.+)$/)
    if (unorderedMatch) {
      closeParagraph()
      if (listType !== 'ul') {
        closeList()
        listType = 'ul'
        html.push('<ul>')
      }
      html.push(`<li>${renderInlineMarkdown(unorderedMatch[1])}</li>`)
      return
    }

    const orderedMatch = line.match(/^\s*\d+\.\s+(.+)$/)
    if (orderedMatch) {
      closeParagraph()
      if (listType !== 'ol') {
        closeList()
        listType = 'ol'
        html.push('<ol>')
      }
      html.push(`<li>${renderInlineMarkdown(orderedMatch[1])}</li>`)
      return
    }

    closeList()
    paragraph.push(line)
  })

  closeParagraph()
  closeList()
  return html.join('')
}

const renderMarkdown = (content: string) => {
  const lines = content.replace(/\r\n/g, '\n').split('\n')
  const html: string[] = []
  let textBuffer: string[] = []
  let codeBuffer: string[] = []
  let inCode = false
  let language = 'text'

  const flushText = () => {
    if (!textBuffer.length) {
      return
    }
    html.push(renderMarkdownText(textBuffer.join('\n')))
    textBuffer = []
  }

  const flushCode = () => {
    const safeLanguage = normalizeLanguage(language)
    html.push(
      `<div class="md-code-block"><div class="md-code-header"><span>${escapeHtml(safeLanguage)}</span></div><pre><code class="language-${escapeHtml(safeLanguage)}">${highlightCode(codeBuffer.join('\n'), safeLanguage)}</code></pre></div>`,
    )
    codeBuffer = []
    language = 'text'
  }

  lines.forEach((line) => {
    const fenceMatch = line.match(/^```([\w#+.-]*)\s*$/)
    if (fenceMatch) {
      if (inCode) {
        flushCode()
        inCode = false
      } else {
        flushText()
        inCode = true
        language = fenceMatch[1] || 'text'
      }
      return
    }

    if (inCode) {
      codeBuffer.push(line)
      return
    }

    textBuffer.push(line)
  })

  if (inCode) {
    flushCode()
  }
  flushText()
  return html.join('')
}

const formatStructuredStreamChunk = (payload: StructuredStreamPayload) => {
  if (payload.v) {
    return payload.v
  }

  if (payload.type === 'ai_response') {
    return payload.data || ''
  }

  if (payload.type === 'tool_request') {
    return `\n\n[Tool] ${payload.name || 'Tool call'}\n\n`
  }

  if (payload.type === 'tool_executed') {
    let relativeFilePath = ''
    let content = ''

    if (payload.arguments) {
      try {
        const parsedArguments = JSON.parse(payload.arguments) as {
          relativeFilePath?: string
          content?: string
        }
        relativeFilePath = parsedArguments.relativeFilePath || ''
        content = parsedArguments.content || ''
      } catch {
        relativeFilePath = ''
      }
    }

    if (relativeFilePath) {
      const suffix = relativeFilePath.includes('.')
        ? relativeFilePath.split('.').pop() || 'text'
        : 'text'
      return `\n\n[Tool] Write file ${relativeFilePath}\n\`\`\`${suffix}\n${content}\n\`\`\`\n\n`
    }

    return payload.result ? `\n\n[Tool Result] ${payload.result}\n\n` : ''
  }

  return ''
}

const extractAssistantChunk = (rawData: string) => {
  if (!rawData) {
    return ''
  }

  try {
    const parsed = JSON.parse(rawData) as StructuredStreamPayload
    return formatStructuredStreamChunk(parsed) || rawData
  } catch {
    return rawData
  }
}

const resolveStreamErrorMessage = (rawText: string) => {
  try {
    const parsed = JSON.parse(rawText) as { message?: string }
    return parsed.message || rawText
  } catch {
    return rawText
  }
}

const parseApiErrorPayload = (rawText: string) => {
  try {
    const parsed = JSON.parse(rawText) as ApiErrorPayload
    if (typeof parsed.code === 'number' && parsed.code !== 0) {
      return parsed
    }
    return null
  } catch {
    return null
  }
}

const parseSseFrame = (frame: string) => {
  const normalizedFrame = frame.replace(/\r/g, '')
  const lines = normalizedFrame.split('\n')
  const dataLines: string[] = []
  let event = 'message'
  let hasSsePrefix = false

  lines.forEach((line) => {
    if (!line || line.startsWith(':')) {
      return
    }
    if (line.startsWith('event:')) {
      hasSsePrefix = true
      event = line.slice(6).trim() || 'message'
      return
    }
    if (line.startsWith('data:')) {
      hasSsePrefix = true
      dataLines.push(line.slice(5).trimStart())
    }
  })

  if (!hasSsePrefix) {
    return {
      event: 'message',
      data: normalizedFrame,
    }
  }

  return {
    event,
    data: dataLines.join('\n'),
  }
}

const appendAssistantContent = (assistantMessage: ChatMessage, content: string) => {
  if (!content) {
    return
  }
  assistantMessage.content += content
  assistantMessage.renderedHtml = renderMarkdown(assistantMessage.content)
}

const completeAssistantMessage = (assistantMessage: ChatMessage) => {
  if (assistantMessage.flushFrameId) {
    window.cancelAnimationFrame(assistantMessage.flushFrameId)
    assistantMessage.flushFrameId = undefined
  }

  assistantMessage.streaming = false
  sending.value = false

  if (assistantMessage.previewOnComplete) {
    void refreshPreview({
      poll: isVueProjectMode.value,
      preserveCurrent: true,
      silent: true,
    })
  }

  void scrollToBottom()
}

const flushAssistantPendingContent = (assistantMessage: ChatMessage) => {
  assistantMessage.flushFrameId = undefined

  const pending = assistantMessage.pendingContent || ''
  if (pending) {
    assistantMessage.pendingContent = ''
    appendAssistantContent(assistantMessage, pending)
    void scrollToBottom()
  }

  if (assistantMessage.pendingContent) {
    scheduleAssistantFlush(assistantMessage)
    return
  }

  if (assistantMessage.streamFinished) {
    completeAssistantMessage(assistantMessage)
  }
}

const scheduleAssistantFlush = (assistantMessage: ChatMessage) => {
  if (assistantMessage.flushFrameId) {
    return
  }

  assistantMessage.flushFrameId = window.requestAnimationFrame(() => {
    flushAssistantPendingContent(assistantMessage)
  })
}

const queueAssistantChunk = (assistantMessage: ChatMessage, rawData: string) => {
  const chunk = extractAssistantChunk(rawData)
  if (!chunk) {
    return
  }
  assistantMessage.rawContent = `${assistantMessage.rawContent || ''}${chunk}`
  assistantMessage.pendingContent = `${assistantMessage.pendingContent || ''}${chunk}`
  scheduleAssistantFlush(assistantMessage)
}

const finishAssistantStream = (assistantMessage: ChatMessage) => {
  assistantMessage.previewOnComplete = shouldRefreshPreviewFromResponse(
    assistantMessage.rawContent || assistantMessage.content,
  )
  assistantMessage.streamFinished = true
  if (assistantMessage.pendingContent) {
    scheduleAssistantFlush(assistantMessage)
    return
  }
  completeAssistantMessage(assistantMessage)
}

const failAssistantStream = (assistantMessage: ChatMessage, errorText: string) => {
  if (!assistantMessage.streaming) {
    return
  }
  assistantMessage.streamFinished = true
  assistantMessage.pendingContent ||= errorText
  scheduleAssistantFlush(assistantMessage)
}

const consumeAssistantStream = async (assistantMessage: ChatMessage, text: string) => {
  const params = new URLSearchParams({
    appId: appId.value,
    message: text,
  })
  const controller = new AbortController()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  streamAbortController = controller

  try {
    const response = await fetch(`${API_BASE_URL}/app/chat?${params.toString()}`, {
      method: 'GET',
      credentials: 'include',
      headers: {
        Accept: 'text/event-stream',
      },
      signal: controller.signal,
    })

    const responseContentType = response.headers.get('content-type') || ''
    if (!response.ok) {
      const errorText = resolveStreamErrorMessage(await response.text())
      throw new Error(errorText || `Request failed (${response.status})`)
    }

    if (responseContentType.includes('application/json')) {
      const rawText = await response.text()
      const apiError = parseApiErrorPayload(rawText)
      throw new Error(apiError?.message || resolveStreamErrorMessage(rawText) || 'Generation failed')
    }

    if (!response.body) {
      throw new Error('No readable response body was returned')
    }

    const reader = response.body.getReader()

    while (true) {
      const { done, value } = await reader.read()
      buffer += decoder.decode(value || new Uint8Array(), { stream: !done })
      buffer = buffer.replace(/\r\n/g, '\n')

      let frameBoundaryIndex = buffer.indexOf('\n\n')
      while (frameBoundaryIndex !== -1) {
        const frame = buffer.slice(0, frameBoundaryIndex)
        buffer = buffer.slice(frameBoundaryIndex + 2)

        const parsedFrame = parseSseFrame(frame)
        if (parsedFrame?.event === 'done') {
          finishAssistantStream(assistantMessage)
          return
        }
        if (parsedFrame?.data) {
          queueAssistantChunk(assistantMessage, parsedFrame.data)
        }

        frameBoundaryIndex = buffer.indexOf('\n\n')
      }

      if (done) {
        const rest = buffer.trim()
        if (rest) {
          const apiError = parseApiErrorPayload(rest)
          if (apiError) {
            throw new Error(apiError.message || 'Generation failed')
          }
          const parsedFrame = parseSseFrame(rest)
          if (parsedFrame?.data) {
            const frameError = parseApiErrorPayload(parsedFrame.data)
            if (frameError) {
              throw new Error(frameError.message || 'Generation failed')
            }
            queueAssistantChunk(assistantMessage, parsedFrame.data)
          }
        }
        finishAssistantStream(assistantMessage)
        return
      }
    }
  } catch (error) {
    if (controller.signal.aborted) {
      return
    }
    const errorMessage = error instanceof Error ? error.message : 'Streaming connection was interrupted'
    failAssistantStream(assistantMessage, errorMessage)
    message.error(errorMessage)
    sending.value = false
  } finally {
    if (streamAbortController === controller) {
      streamAbortController = null
    }
  }
}

const sendMessage = async (content = inputMessage.value) => {
  const text = content.trim()
  if (!text) {
    message.info('Describe what you want the AI to build first')
    return
  }
  if (!appId.value) {
    message.error('Invalid app id')
    return
  }
  if (sending.value) {
    message.info('The AI is still working on the current request')
    return
  }

  closeStream()
  inputMessage.value = ''
  sending.value = true

  const prompt = selectedVisualElement.value
    ? `${text}\n\n${formatVisualEditorSelectionPrompt(selectedVisualElement.value)}`
    : text
  clearSelectedElement()
  disableVisualEditing()

  const userMessage: ChatMessage = {
    id: `${Date.now()}-user`,
    role: 'user',
    content: prompt,
  }
  const assistantMessage = reactive<ChatMessage>({
    id: `${Date.now()}-assistant`,
    role: 'assistant',
    content: '',
    renderedHtml: '',
    rawContent: '',
    pendingContent: '',
    previewOnComplete: false,
    streamFinished: false,
    streaming: true,
  })

  messages.value.push(userMessage, assistantMessage)
  await scrollToBottom()
  void consumeAssistantStream(assistantMessage, prompt)
}

const deployApp = async () => {
  if (!canManage.value) {
    message.warning('Only the owner or an admin can deploy this app')
    return
  }
  if (!appId.value) {
    return
  }

  deploying.value = true
  try {
    const res = await appDeploy({ appId: appId.value as unknown as number })
    if (res.data.code !== 0 || !res.data.data) {
      message.error(res.data.message || 'Deploy failed')
      return
    }
    Modal.success({
      title: 'Deploy succeeded',
      content: res.data.data,
      okText: 'OK',
    })
    if (navigator.clipboard) {
      await navigator.clipboard.writeText(res.data.data)
      message.success('Deploy URL copied')
    }
    await loadApp()
  } catch (error) {
    message.error(error instanceof Error ? error.message : 'Deploy failed')
  } finally {
    deploying.value = false
  }
}

const cancelDeployApp = async () => {
  if (!canManage.value) {
    message.warning('Only the owner or an admin can cancel deployment')
    return
  }
  if (!appId.value) {
    return
  }

  canceling.value = true
  try {
    const res = await cancelDeploy({ appId: appId.value as unknown as number })
    if (res.data.code !== 0) {
      message.error(res.data.message || 'Cancel deploy failed')
      return
    }

    message.success(res.data.data || 'Deployment removed')
    await loadApp()
  } catch (error) {
    message.error(error instanceof Error ? error.message : 'Cancel deploy failed')
  } finally {
    canceling.value = false
  }
}

const openDeployedApp = () => {
  if (!deployedUrl.value) {
    message.info('This app has not been deployed yet')
    return
  }

  window.open(deployedUrl.value, '_blank', 'noopener,noreferrer')
}

const goEdit = () => {
  router.push(`/app/edit/${appId.value}`)
}

const initAutoMessage = async () => {
  const prompt = typeof route.query.prompt === 'string' ? route.query.prompt : ''
  if (!prompt) {
    await refreshPreview({ silent: true })
    return
  }

  const guardKey = `app-chat-auto-sent-${appId.value}-${prompt}`
  if (window.sessionStorage.getItem(guardKey)) {
    await refreshPreview({ silent: true })
    return
  }

  window.sessionStorage.setItem(guardKey, '1')
  await sendMessage(prompt)
}

onMounted(async () => {
  await loginUserStore.fetchLoginUser()
  await loadApp()
  await loadHistory()
  await initAutoMessage()
})

onBeforeUnmount(() => {
  closeStream()
  messages.value.forEach((item) => {
    if (item.flushFrameId) {
      window.cancelAnimationFrame(item.flushFrameId)
      item.flushFrameId = undefined
    }
  })
})
</script>

<template>
  <main class="chat-page">
    <header class="chat-topbar">
      <button type="button" class="chat-topbar__app" @click="goEdit">
        <img :src="logoUrl" alt="logo" />
        <span>{{ appName }}</span>
        <span class="chat-topbar__chevron">></span>
      </button>
      <a-space>
        <a-button @click="router.push(`/app/detail/${appId}`)">&#35814;&#24773;</a-button>
        <a-button v-if="isDeployed" @click="openDeployedApp">&#25171;&#24320;&#24050;&#37096;&#32626;&#24212;&#29992;</a-button>
        <a-popconfirm
          v-if="canManage && isDeployed"
          title="&#30830;&#35748;&#21462;&#28040;&#24403;&#21069;&#24212;&#29992;&#30340;&#37096;&#32626;&#21527;&#65311;"
          ok-text="&#30830;&#35748;"
          cancel-text="&#21462;&#28040;"
          @confirm="cancelDeployApp"
        >
          <a-button
            danger
            :loading="canceling"
            :disabled="deploying"
            class="deploy-btn deploy-btn--danger"
          >
            &#21462;&#28040;&#37096;&#32626;
          </a-button>
        </a-popconfirm>
        <a-button
          v-else-if="canManage"
          type="primary"
          :loading="deploying"
          :disabled="canceling"
          class="deploy-btn"
          @click="deployApp"
        >
          &#37096;&#32626;
        </a-button>
      </a-space>
    </header>

    <section class="chat-workspace">
      <aside class="chat-panel">
        <div class="chat-panel__meta">
          <div class="chat-panel__badge-row">
            <span class="chat-mode-badge">{{ generationModeLabel }}</span>
            <span class="chat-status-text">{{ streamStatusLabel }}</span>
          </div>
          <p>{{ generationModeDescription }}</p>
        </div>

        <div ref="messageListRef" class="message-list">
          <div v-if="hasMoreHistory" class="message-history-toolbar">
            <a-button type="text" size="small" :loading="loadingMoreHistory" @click="loadHistory(true)">
              &#21152;&#36733;&#26356;&#26089;&#28040;&#24687;
            </a-button>
          </div>

          <a-spin v-if="loadingApp" />
          <a-spin v-else-if="loadingHistory" />

          <div v-else-if="!messages.length" class="message-empty">
            <h3>&#36824;&#27809;&#26377;&#28040;&#24687;</h3>
            <p>&#25551;&#36848;&#20320;&#24819;&#26500;&#24314;&#30340;&#20869;&#23481;&#65292;&#23436;&#25972;&#23545;&#35805;&#20250;&#26174;&#31034;&#22312;&#36825;&#37324;&#12290;</p>
          </div>

          <template v-for="item in messages" :key="item.id">
            <div class="message-row" :class="`message-row--${item.role}`">
              <div v-if="item.role === 'assistant'" class="message-avatar">
                <img :src="logoUrl" alt="AI" />
              </div>

              <div class="message-bubble" :class="{ 'message-bubble--streaming': item.streaming }">
                <pre v-if="item.role === 'user'">{{ item.content }}</pre>
                <div
                  v-else
                  class="markdown-body"
                  v-html="item.renderedHtml || renderMarkdown(item.content || (item.streaming ? '\u751f\u6210\u4e2d...' : ''))"
                ></div>
                <span v-if="item.streaming" class="typing-cursor"></span>
              </div>

              <a-avatar
                v-if="item.role === 'user'"
                :src="app?.userVo?.userAvatar"
                :size="32"
                class="message-avatar message-avatar--user"
              >
                {{ appAuthorInitial }}
              </a-avatar>
            </div>
          </template>
        </div>

        <div class="chat-input">
          <a-alert
            v-if="selectedVisualElement"
            class="chat-input__selection"
            closable
            show-icon
            type="info"
            :message="`选中元素：${selectedVisualElementLabel}`"
            :description="selectedVisualElementDescription"
            @close="clearSelectedElement"
          />

          <a-textarea
            v-model:value="inputMessage"
            :auto-size="{ minRows: 4, maxRows: 7 }"
            :bordered="false"
            :disabled="sending"
            placeholder="&#25551;&#36848;&#20320;&#24819;&#29983;&#25104;&#30340;&#32593;&#31449;&#25110;&#24212;&#29992;..."
            @pressEnter.ctrl="sendMessage()"
          />

          <div class="chat-input__footer">
            <a-button
              class="chat-input__edit"
              :type="isVisualEditing ? 'primary' : 'default'"
              :disabled="sending || previewRefreshing || !previewReady"
              @click="toggleVisualEditing"
            >
              {{ isVisualEditing ? '退出编辑' : '进入编辑' }}
            </a-button>
            <a-button
              type="primary"
              shape="circle"
              size="large"
              :loading="sending"
              class="chat-input__send"
              @click="sendMessage()"
            >
              &#21457;&#36865;
            </a-button>
          </div>

          <p class="chat-input__hint">
            {{ inputHintText }}
          </p>
        </div>
      </aside>

      <section class="preview-panel">
        <div v-if="previewReady" class="preview-frame-wrap">
          <div v-if="sending || previewRefreshing" class="preview-loading-mask">
            <a-spin />
            <span>&#27491;&#22312;&#26356;&#26032;&#39044;&#35272;...</span>
          </div>

          <iframe
            ref="previewFrameRef"
            :key="previewVersion"
            :src="isVueProjectMode ? previewIframeSrc : undefined"
            :srcdoc="isVueProjectMode ? undefined : previewHtml"
            title="&#29983;&#25104;&#39044;&#35272;"
          ></iframe>
        </div>

        <div v-else class="preview-empty">
          <a-spin :spinning="sending || previewRefreshing" />
          <h2>{{ previewTitle }}</h2>
          <p>{{ previewDescription }}</p>
          <a-button
            v-if="previewError && !previewRefreshing"
            class="preview-empty__action"
            @click="refreshPreview({ poll: isVueProjectMode, silent: false })"
          >
            &#37325;&#26032;&#21152;&#36733;&#39044;&#35272;
          </a-button>
        </div>
      </section>
    </section>
  </main>
</template>
<style scoped>
.chat-page {
  min-height: calc(100vh - 180px);
  padding: 0 0 26px;
}

.chat-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  min-height: 68px;
  margin-bottom: 12px;
  padding: 10px 0;
}

.chat-topbar__app {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  max-width: min(100%, 420px);
  padding: 8px 14px 8px 8px;
  color: #111827;
  font-size: 18px;
  font-weight: 700;
  border: 1px solid rgba(103, 132, 166, 0.16);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.88);
  cursor: pointer;
}

.chat-topbar__app img {
  width: 42px;
  height: 42px;
  border-radius: 15px;
}

.chat-topbar__app span:nth-child(2) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-topbar__chevron {
  color: #6b7280;
}

.deploy-btn {
  min-width: 92px;
  border-radius: 10px;
  background: #111827;
  border-color: #111827;
}

.deploy-btn--danger {
  background: #fff2f0;
  border-color: #ff4d4f;
}

.chat-workspace {
  display: grid;
  grid-template-columns: minmax(360px, 520px) minmax(0, 1fr);
  gap: 18px;
  height: calc(100vh - 190px);
  min-height: 640px;
}

.chat-panel,
.preview-panel {
  height: 100%;
  min-height: 0;
  border: 1px solid rgba(102, 132, 166, 0.16);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: var(--shadow-soft);
}

.chat-panel {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-panel__meta {
  padding: 18px 20px 0;
}

.chat-panel__meta p {
  margin: 10px 0 0;
  color: #667085;
  font-size: 13px;
  line-height: 1.7;
}

.chat-panel__badge-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.chat-mode-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  color: #0f172a;
  font-size: 12px;
  font-weight: 700;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.18), rgba(59, 130, 246, 0.14));
}

.chat-status-text {
  color: #2563eb;
  font-size: 13px;
  font-weight: 600;
}

.message-list {
  flex: 1;
  min-height: 0;
  padding: 18px 20px 24px;
  overflow-y: auto;
}

.message-history-toolbar {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.message-empty {
  display: grid;
  min-height: 100%;
  place-items: center;
  align-content: center;
  gap: 10px;
  color: #667085;
  text-align: center;
}

.message-empty h3,
.message-empty p {
  margin: 0;
}

.message-empty h3 {
  color: #1f2937;
  font-size: 18px;
}

.message-row {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
}

.message-row--user {
  justify-content: flex-end;
}

.message-avatar {
  flex: 0 0 auto;
  width: 28px;
  height: 28px;
  overflow: hidden;
  border-radius: 10px;
}

.message-avatar img {
  width: 100%;
  height: 100%;
}

.message-avatar--user {
  color: #ffffff;
  font-weight: 700;
  background: linear-gradient(135deg, #2563eb, #0ea5e9);
  box-shadow: 0 10px 20px rgba(37, 99, 235, 0.18);
}

.message-bubble {
  max-width: min(88%, 460px);
  padding: 16px 18px;
  color: #1f2937;
  border: 1px solid rgba(110, 133, 160, 0.12);
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 10px 24px rgba(31, 41, 55, 0.06);
}

.message-row--user .message-bubble {
  background: #f4f5f7;
}

.message-bubble pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  line-height: 1.8;
}

.markdown-body {
  font-size: 15px;
  line-height: 1.8;
  word-break: break-word;
}

.markdown-body :deep(p) {
  margin: 0 0 12px;
}

.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4) {
  margin: 18px 0 10px;
  color: #111827;
  font-weight: 800;
  line-height: 1.35;
}

.markdown-body :deep(h1) {
  font-size: 22px;
}

.markdown-body :deep(h2) {
  font-size: 20px;
}

.markdown-body :deep(h3),
.markdown-body :deep(h4) {
  font-size: 18px;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 8px 0 14px;
  padding-left: 22px;
}

.markdown-body :deep(li) {
  margin: 4px 0;
}

.markdown-body :deep(p code),
.markdown-body :deep(li code) {
  padding: 2px 6px;
  color: #175cd3;
  font-family: Consolas, 'JetBrains Mono', 'SFMono-Regular', monospace;
  font-size: 0.92em;
  border-radius: 6px;
  background: rgba(45, 127, 249, 0.1);
}

.markdown-body :deep(.md-code-block) {
  margin: 14px 0;
  overflow: hidden;
  border: 1px solid rgba(78, 103, 135, 0.2);
  border-radius: 12px;
  background: #0f172a;
}

.markdown-body :deep(.md-code-header) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 34px;
  padding: 0 12px;
  color: #cbd5e1;
  font-size: 12px;
  font-weight: 700;
  border-bottom: 1px solid rgba(203, 213, 225, 0.14);
  background: #111827;
}

.markdown-body :deep(.md-code-block pre) {
  margin: 0;
  padding: 14px 16px;
  overflow-x: auto;
  color: #e5e7eb;
  font-family: Consolas, 'JetBrains Mono', 'SFMono-Regular', monospace;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre;
  word-break: normal;
}

.markdown-body :deep(.token-comment) {
  color: #94a3b8;
  font-style: italic;
}

.markdown-body :deep(.token-tag),
.markdown-body :deep(.token-selector) {
  color: #67e8f9;
}

.markdown-body :deep(.token-attr) {
  color: #fbbf24;
}

.markdown-body :deep(.token-string) {
  color: #86efac;
}

.markdown-body :deep(.token-keyword) {
  color: #c084fc;
}

.markdown-body :deep(.token-number) {
  color: #fca5a5;
}

.message-bubble--streaming {
  border-color: rgba(45, 127, 249, 0.24);
}

.typing-cursor {
  display: inline-block;
  width: 8px;
  height: 18px;
  margin-left: 4px;
  vertical-align: text-bottom;
  background: #2d7ff9;
  animation: blink 1s infinite;
}

.chat-input {
  flex: 0 0 auto;
  margin: 16px;
  padding: 16px;
  border: 1px solid rgba(106, 132, 160, 0.14);
  border-radius: 18px;
  background: #ffffff;
}

.chat-input :deep(textarea) {
  font-size: 16px;
  line-height: 1.8;
}

.chat-input__selection {
  margin-bottom: 12px;
}

.chat-input__selection :deep(.ant-alert-description) {
  white-space: pre-wrap;
}

.chat-input__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
}

.chat-input__edit {
  min-width: 96px;
}

.chat-input__send {
  flex: 0 0 auto;
  min-width: 72px;
  background: #111827;
  border-color: #111827;
}

.chat-input__hint {
  margin: 12px 0 0;
  color: #667085;
  font-size: 12px;
  line-height: 1.7;
}

.preview-panel {
  overflow: hidden;
  padding: 14px;
}

.preview-frame-wrap,
.preview-frame-wrap iframe {
  width: 100%;
  height: 100%;
  min-height: 0;
}

.preview-frame-wrap {
  position: relative;
}

.preview-frame-wrap iframe {
  border: 0;
  border-radius: 16px;
  background: #ffffff;
}

.preview-loading-mask {
  position: absolute;
  top: 18px;
  right: 18px;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  color: #334155;
  font-size: 13px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.1);
  backdrop-filter: blur(10px);
}

.preview-empty {
  display: grid;
  height: 100%;
  min-height: 0;
  place-items: center;
  align-content: center;
  gap: 14px;
  color: #667085;
  text-align: center;
}

.preview-empty h2 {
  margin: 0;
  color: #1f2937;
  font-size: 26px;
}

.preview-empty p {
  max-width: 520px;
  margin: 0;
  line-height: 1.8;
}

.preview-empty__action {
  margin-top: 4px;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}

@media (max-width: 1120px) {
  .chat-workspace {
    grid-template-columns: 1fr;
    height: auto;
    min-height: 0;
  }

  .chat-panel,
  .preview-panel {
    height: min(720px, calc(100vh - 160px));
    min-height: 520px;
  }
}

@media (max-width: 767px) {
  .chat-topbar {
    align-items: stretch;
    flex-direction: column;
  }

  .chat-topbar__app {
    max-width: 100%;
  }

  .chat-input__footer {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
