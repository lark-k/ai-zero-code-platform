import { defineStore } from 'pinia'
import { reactive } from 'vue'

export interface ChatSessionMessage {
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

export interface AppChatSessionState {
  messages: ChatSessionMessage[]
  sending: boolean
  pendingHistorySync: boolean
  pendingPreviewRefresh: boolean
  historyCursor?: string
  hasMoreHistory: boolean
}

const createSessionState = (): AppChatSessionState => ({
  messages: [],
  sending: false,
  pendingHistorySync: false,
  pendingPreviewRefresh: false,
  historyCursor: undefined,
  hasMoreHistory: false,
})

export const useAppChatSessionsStore = defineStore('appChatSessions', () => {
  const sessions = reactive<Record<string, AppChatSessionState>>({})
  const emptySession = reactive(createSessionState())

  const ensureSession = (appId: string) => {
    const normalizedAppId = appId.trim()
    if (!normalizedAppId) {
      return emptySession
    }

    if (!sessions[normalizedAppId]) {
      sessions[normalizedAppId] = createSessionState()
    }

    return sessions[normalizedAppId]
  }

  return {
    sessions,
    ensureSession,
  }
})
