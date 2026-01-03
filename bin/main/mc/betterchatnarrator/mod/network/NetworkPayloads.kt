package mc.betterchatnarrator.mod.network

import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.nio.charset.StandardCharsets

object NetworkPayloads {
    val INIT: CustomPayload.Id<InitPayload> = CustomPayload.Id(Identifier.of("betterchatnarrator", "init"))
    val CHANNELS: CustomPayload.Id<ChannelsPayload> = CustomPayload.Id(Identifier.of("betterchatnarrator", "channels"))
    val CONFIG: CustomPayload.Id<ConfigPayload> = CustomPayload.Id(Identifier.of("betterchatnarrator", "config"))
    val MENU: CustomPayload.Id<MenuPayload> = CustomPayload.Id(Identifier.of("betterchatnarrator", "menu"))
    val SAY: CustomPayload.Id<SayPayload> = CustomPayload.Id(Identifier.of("betterchatnarrator", "say"))
}

// Client -> Server
data class InitPayload(val data: String) : CustomPayload {
    override fun getId(): CustomPayload.Id<InitPayload> = NetworkPayloads.INIT

    companion object {
        val CODEC: PacketCodec<RegistryByteBuf, InitPayload> = CustomPayload.codecOf(
            { value, buf -> buf.writeBytes(value.data.toByteArray(StandardCharsets.UTF_8)) },
            { buf ->
                val bytes = ByteArray(buf.readableBytes())
                buf.readBytes(bytes)
                InitPayload(String(bytes, StandardCharsets.UTF_8))
            }
        )
    }
}

// Server -> Client
data class ChannelsPayload(val data: String) : CustomPayload {
    override fun getId(): CustomPayload.Id<ChannelsPayload> = NetworkPayloads.CHANNELS

    companion object {
        val CODEC: PacketCodec<RegistryByteBuf, ChannelsPayload> = CustomPayload.codecOf(
            { value, buf -> buf.writeBytes(value.data.toByteArray(StandardCharsets.UTF_8)) },
            { buf ->
                val bytes = ByteArray(buf.readableBytes())
                buf.readBytes(bytes)
                ChannelsPayload(String(bytes, StandardCharsets.UTF_8))
            }
        )
    }
}

data class ConfigPayload(val data: String) : CustomPayload {
    override fun getId(): CustomPayload.Id<ConfigPayload> = NetworkPayloads.CONFIG

    companion object {
        val CODEC: PacketCodec<RegistryByteBuf, ConfigPayload> = CustomPayload.codecOf(
            { value, buf -> buf.writeBytes(value.data.toByteArray(StandardCharsets.UTF_8)) },
            { buf ->
                val bytes = ByteArray(buf.readableBytes())
                buf.readBytes(bytes)
                ConfigPayload(String(bytes, StandardCharsets.UTF_8))
            }
        )
    }
}

data class MenuPayload(val data: String) : CustomPayload {
    override fun getId(): CustomPayload.Id<MenuPayload> = NetworkPayloads.MENU

    companion object {
        val CODEC: PacketCodec<RegistryByteBuf, MenuPayload> = CustomPayload.codecOf(
            { value, buf -> buf.writeBytes(value.data.toByteArray(StandardCharsets.UTF_8)) },
            { buf ->
                val bytes = ByteArray(buf.readableBytes())
                buf.readBytes(bytes)
                MenuPayload(String(bytes, StandardCharsets.UTF_8))
            }
        )
    }
}

data class SayPayload(val data: String) : CustomPayload {
    override fun getId(): CustomPayload.Id<SayPayload> = NetworkPayloads.SAY

    companion object {
        val CODEC: PacketCodec<RegistryByteBuf, SayPayload> = CustomPayload.codecOf(
            { value, buf -> buf.writeBytes(value.data.toByteArray(StandardCharsets.UTF_8)) },
            { buf ->
                val bytes = ByteArray(buf.readableBytes())
                buf.readBytes(bytes)
                SayPayload(String(bytes, StandardCharsets.UTF_8))
            }
        )
    }
}
