package mc.betterchatnarrator.network.c2s

import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos


@JvmRecord
data class NarratorRequestMessage(val version: String) : CustomPayload {
    // should you need to send more data, add the appropriate record parameters and change your codec:
    // public static final PacketCodec<RegistryByteBuf, BlockHighlightPayload> CODEC = PacketCodec.tuple(
    //         BlockPos.PACKET_CODEC, BlockHighlightPayload::blockPos,
    //         PacketCodecs.INTEGER, BlockHighlightPayload::myInt,
    //         Uuids.PACKET_CODEC, BlockHighlightPayload::myUuid,
    //         BlockHighlightPayload::new
    // );
    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }



    companion object {
        val PACKET_ID = Identifier.of("", "")
        val ID: CustomPayload.Id<NarratorRequestMessage> = CustomPayload.Id<NarratorRequestMessage>(PACKET_ID)

        val CODEC: PacketCodec<RegistryByteBuf, NarratorRequestMessage> = PacketCodec.tuple(
            PacketCodecs.STRING, NarratorRequestMessage::version
        ) { version: String ->
            NarratorRequestMessage(version)
        }
    }
}