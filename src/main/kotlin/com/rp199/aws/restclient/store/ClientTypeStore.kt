package com.rp199.aws.restclient.store


class ClientTypeStore {
    companion object {
        private val CONTEXT = ThreadLocal<ClientType>()

        fun setClientType(clientType: ClientType) {
            CONTEXT.set(clientType)
        }

        fun getClientType(): ClientType? {
            return CONTEXT.get()
        }

        fun clear() {
            CONTEXT.remove()
        }
    }
}
