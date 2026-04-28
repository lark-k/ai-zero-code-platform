<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { pageQuery } from '@/api/duihualishixiangguanjiekou.ts'
import {
  appDeploy,
  cancelDeploy,
  getAppVoById,
} from '@/api/yingyongmokuaixiangguanjiekou.ts'
import logoUrl from '@/assets/logo.png'
import { useLoginUserStore } from '@/stores/loginUser.ts'

interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  createTime?: string
  rawContent?: string
  pendingContent?: string
  previewOnComplete?: boolean
  streamFinished?: boolean
  typingTimer?: number
  streaming?: boolean
}

const API_BASE_URL = 'http://localhost:8123/api'
const HISTORY_PAGE_SIZE = 20
const TYPEWRITER_INTERVAL = 28
const TYPEWRITER_CHARS_PER_TICK = 2

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
const previewVersion = ref(Date.now())
const messageListRef = ref<HTMLElement>()
let eventSource: EventSource | null = null

const appId = computed(() => String(route.params.id || ''))
const appAuthorInitial = computed(() => {
  const displayName = app.value?.userVo?.userName || app.value?.userVo?.userAccount || 'A'
  return displayName.trim().charAt(0).toUpperCase() || 'A'
})
const appName = computed(() => app.value?.appName || '未命名应用')
const isOwner = computed(
  () => Boolean(loginUserStore.loginUser.id) && app.value?.userId === loginUserStore.loginUser.id,
)
const canManage = computed(() => loginUserStore.isAdmin || isOwner.value)
const isDeployed = computed(() => Boolean(app.value?.deployKey))
const deployedUrl = computed(() => (app.value?.deployKey ? `http://localhost/${app.value.deployKey}` : ''))
const previewUrl = computed(() => `${API_BASE_URL}/static/${appId.value}?t=${previewVersion.value}`)

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

  if (app.value?.codeGenType === 'multi_file') {
    return hasMultiFileCodeBlocks(normalizedContent)
  }

  return hasHtmlCodeBlock(normalizedContent)
}

const scrollToBottom = async () => {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const loadApp = async () => {
  if (!appId.value) {
    message.error('应用 id 不正确')
    await router.push('/')
    return
  }

  loadingApp.value = true
  try {
    const res = await getAppVoById({ id: appId.value as unknown as number })
    if (res.data.code !== 0 || !res.data.data) {
      message.error(res.data.message || '获取应用信息失败')
      await router.push('/')
      return
    }
    app.value = res.data.data
  } catch (error) {
    message.error(error instanceof Error ? error.message : '获取应用信息失败，请稍后重试')
  } finally {
    loadingApp.value = false
  }
}

const closeSse = () => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
}

const getChatRole = (messageType?: string): ChatMessage['role'] =>
  messageType === 'user' ? 'user' : 'assistant'

const toChatMessage = (record: API.ChatHistory): ChatMessage => ({
  id: String(record.id ?? `${record.createTime}-${record.messageType}`),
  role: getChatRole(record.messageType),
  content: record.message || '',
  createTime: record.createTime,
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
      message.error(res.data.message || '加载历史对话失败')
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
    message.error(error instanceof Error ? error.message : '加载历史对话失败，请稍后重试')
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
  let html = escapeHtml(code)

  if (['html', 'xml', 'vue'].includes(lang)) {
    return html
      .replace(/(&lt;!--[\s\S]*?--&gt;)/g, '<span class="token-comment">$1</span>')
      .replace(/(&lt;\/?)([a-zA-Z][\w:-]*)/g, '$1<span class="token-tag">$2</span>')
      .replace(/(\s)([a-zA-Z_:][\w:.-]*)(=)(&quot;.*?&quot;|&#39;.*?&#39;)/g, '$1<span class="token-attr">$2</span>$3<span class="token-string">$4</span>')
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
      .replace(/\b(const|let|var|function|return|if|else|for|while|class|new|import|from|export|async|await|try|catch|throw|true|false|null|undefined)\b/g, '<span class="token-keyword">$1</span>')
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

const extractAssistantChunk = (rawData: string) => {
  if (!rawData) {
    return ''
  }

  try {
    const parsed = JSON.parse(rawData) as { v?: string }
    return parsed.v ?? rawData
  } catch {
    return rawData
  }
}

const stopTypewriter = (assistantMessage: ChatMessage) => {
  if (assistantMessage.typingTimer) {
    window.clearInterval(assistantMessage.typingTimer)
    assistantMessage.typingTimer = undefined
  }
}

const completeAssistantMessage = (assistantMessage: ChatMessage) => {
  stopTypewriter(assistantMessage)
  assistantMessage.streaming = false
  sending.value = false
  if (assistantMessage.previewOnComplete) {
    previewReady.value = true
    previewVersion.value = Date.now()
  }
  void scrollToBottom()
}

const startTypewriter = (assistantMessage: ChatMessage) => {
  if (assistantMessage.typingTimer) {
    return
  }

  assistantMessage.typingTimer = window.setInterval(() => {
    const pending = assistantMessage.pendingContent || ''
    if (!pending) {
      stopTypewriter(assistantMessage)
      if (assistantMessage.streamFinished) {
        completeAssistantMessage(assistantMessage)
      }
      return
    }

    assistantMessage.content += pending.slice(0, TYPEWRITER_CHARS_PER_TICK)
    assistantMessage.pendingContent = pending.slice(TYPEWRITER_CHARS_PER_TICK)
    void scrollToBottom()

    if (!assistantMessage.pendingContent && assistantMessage.streamFinished) {
      completeAssistantMessage(assistantMessage)
    }
  }, TYPEWRITER_INTERVAL)
}

const queueAssistantChunk = (assistantMessage: ChatMessage, rawData: string) => {
  const chunk = extractAssistantChunk(rawData)
  if (!chunk) {
    return
  }
  assistantMessage.rawContent = `${assistantMessage.rawContent || ''}${chunk}`
  assistantMessage.pendingContent = `${assistantMessage.pendingContent || ''}${chunk}`
  startTypewriter(assistantMessage)
}

const sendMessage = async (content = inputMessage.value) => {
  const text = content.trim()
  if (!text) {
    message.info('请输入你想让 AI 完成的内容')
    return
  }
  if (!appId.value) {
    message.error('应用 id 不正确')
    return
  }
  if (sending.value) {
    message.info('AI 正在生成中，请稍后再发送')
    return
  }

  closeSse()
  inputMessage.value = ''
  sending.value = true

  const userMessage: ChatMessage = {
    id: `${Date.now()}-user`,
    role: 'user',
    content: text,
  }
  const assistantMessage = reactive<ChatMessage>({
    id: `${Date.now()}-assistant`,
    role: 'assistant',
    content: '',
    rawContent: '',
    pendingContent: '',
    previewOnComplete: false,
    streamFinished: false,
    streaming: true,
  })
  messages.value.push(userMessage, assistantMessage)
  await scrollToBottom()

  const params = new URLSearchParams({
    appId: appId.value,
    message: text,
  })
  eventSource = new EventSource(`${API_BASE_URL}/app/chat?${params.toString()}`, {
    withCredentials: true,
  })

  eventSource.onmessage = (event) => {
    queueAssistantChunk(assistantMessage, event.data)
  }

  eventSource.addEventListener('done', () => {
    assistantMessage.previewOnComplete = shouldRefreshPreviewFromResponse(
      assistantMessage.rawContent || assistantMessage.content,
    )
    assistantMessage.streamFinished = true
    closeSse()
    startTypewriter(assistantMessage)
  })

  eventSource.onerror = () => {
    if (assistantMessage.streaming) {
      assistantMessage.streamFinished = true
      assistantMessage.pendingContent ||= '生成连接已中断，请稍后重试。'
      startTypewriter(assistantMessage)
      message.error('AI 生成连接中断')
    }
    sending.value = false
    closeSse()
  }
}

const deployApp = async () => {
  if (!canManage.value) {
    message.warning('只有应用作者或管理员可以部署该应用')
    return
  }
  if (!appId.value) {
    return
  }
  deploying.value = true
  try {
    const res = await appDeploy({ appId: appId.value as unknown as number })
    if (res.data.code !== 0 || !res.data.data) {
      message.error(res.data.message || '部署失败')
      return
    }
    Modal.success({
      title: '部署成功',
      content: res.data.data,
      okText: '知道了',
    })
    if (navigator.clipboard) {
      await navigator.clipboard.writeText(res.data.data)
      message.success('部署地址已复制')
    }
    await loadApp()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '部署失败，请稍后重试')
  } finally {
    deploying.value = false
  }
}

const cancelDeployApp = async () => {
  if (!canManage.value) {
    message.warning('只有应用作者或管理员可以取消部署该应用')
    return
  }
  if (!appId.value) {
    return
  }

  canceling.value = true
  try {
    const res = await cancelDeploy({ appId: appId.value as unknown as number })
    if (res.data.code !== 0) {
      message.error(res.data.message || '取消部署失败')
      return
    }

    message.success(res.data.data || '取消部署成功')
    await loadApp()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '取消部署失败，请稍后重试')
  } finally {
    canceling.value = false
  }
}

const openDeployedApp = () => {
  if (!deployedUrl.value) {
    message.info('当前应用尚未部署')
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
    previewReady.value = true
    return
  }

  const guardKey = `app-chat-auto-sent-${appId.value}-${prompt}`
  if (window.sessionStorage.getItem(guardKey)) {
    previewReady.value = true
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
  closeSse()
  messages.value.forEach(stopTypewriter)
})
</script>

<template>
  <main class="chat-page">
    <header class="chat-topbar">
      <button type="button" class="chat-topbar__app" @click="goEdit">
        <img :src="logoUrl" alt="logo" />
        <span>{{ appName }}</span>
        <span class="chat-topbar__chevron">⌄</span>
      </button>
      <a-space>
        <a-button @click="router.push(`/app/detail/${appId}`)">详情</a-button>
        <a-button v-if="isDeployed" @click="openDeployedApp">访问已部署</a-button>
        <a-popconfirm
          v-if="canManage && isDeployed"
          title="确定取消部署该应用吗？"
          ok-text="确定"
          cancel-text="取消"
          @confirm="cancelDeployApp"
        >
          <a-button
            danger
            :loading="canceling"
            :disabled="deploying"
            class="deploy-btn deploy-btn--danger"
          >
            取消部署
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
          部署
        </a-button>
      </a-space>
    </header>

    <section class="chat-workspace">
      <aside class="chat-panel">
        <div ref="messageListRef" class="message-list">
          <div v-if="hasMoreHistory" class="message-history-toolbar">
            <a-button
              type="text"
              size="small"
              :loading="loadingMoreHistory"
              @click="loadHistory(true)"
            >
              查看更早的对话
            </a-button>
          </div>
          <a-spin v-if="loadingApp" />
          <a-spin v-else-if="loadingHistory" />
          <div v-else-if="!messages.length" class="message-empty">
            <h3>还没有历史对话</h3>
            <p>从下方输入你的需求后，这里会展示你和 AI 的完整生成记录。</p>
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
                  v-html="renderMarkdown(item.content || (item.streaming ? '正在生成...' : ''))"
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
          <a-textarea
            v-model:value="inputMessage"
            :auto-size="{ minRows: 4, maxRows: 7 }"
            :bordered="false"
            placeholder="请描述你想生成的网站，越详细效果越好哦"
            :disabled="sending"
            @pressEnter.ctrl="sendMessage()"
          />
          <div class="chat-input__footer">
            <div class="chat-input__tools">
              <a-button>上传</a-button>
              <a-button>编辑</a-button>
              <a-button disabled>优化</a-button>
            </div>
            <a-button
              type="primary"
              shape="circle"
              size="large"
              :loading="sending"
              class="chat-input__send"
              @click="sendMessage()"
            >
              ↑
            </a-button>
          </div>
        </div>
      </aside>

      <section class="preview-panel">
        <div v-if="previewReady" class="preview-frame-wrap">
          <div v-if="sending" class="preview-loading-mask">
            <a-spin />
            <span>Updating preview...</span>
          </div>
          <iframe :key="previewVersion" :src="previewUrl" title="生成后的网页展示"></iframe>
        </div>
        <div v-else class="preview-empty">
          <a-spin :spinning="sending" />
          <h2>{{ sending ? 'AI 正在生成网页文件' : '等待生成结果' }}</h2>
          <p>流式回复结束后，会自动在这里展示本地预览。</p>
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

.message-list {
  flex: 1;
  min-height: 0;
  padding: 24px 20px;
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

.chat-input__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
}

.chat-input__tools {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chat-input__tools :deep(.ant-btn) {
  border-radius: 999px;
}

.chat-input__send {
  flex: 0 0 auto;
  background: #9aa1a9;
  border-color: #9aa1a9;
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
  margin: 0;
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
