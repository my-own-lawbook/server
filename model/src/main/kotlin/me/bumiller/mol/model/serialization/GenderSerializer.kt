package me.bumiller.mol.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.bumiller.mol.model.Gender

internal class GenderSerializer: KSerializer<Gender> {

    override val descriptor = PrimitiveSerialDescriptor("gender", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Gender {
        return when (val str = decoder.decodeString()) {
            "male" -> Gender.Male
            "female" -> Gender.Female
            "other" -> Gender.Other
            "disclosed" -> Gender.Disclosed
            else -> error("Unknown gender: '$str'")
        }
    }

    override fun serialize(encoder: Encoder, value: Gender) {
        encoder.encodeString(value.serializedName)
    }
}