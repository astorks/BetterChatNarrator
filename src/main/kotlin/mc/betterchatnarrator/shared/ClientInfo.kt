package mc.betterchatnarrator.shared

data class ClientInfo(val version: String) {
    companion object {
        fun deserialize(data: String): ClientInfo {
            val dataSplit = data.split('|')
            val version = if(dataSplit.isNotEmpty()) dataSplit[0] else ""
            return ClientInfo(version)
        }

        fun serialize(clientInfo: ClientInfo): String {
            return clientInfo.version
        }
    }
}