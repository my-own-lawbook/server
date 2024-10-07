package me.bumiller.mol.common.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.present

/**
 * Serializer for the custom [Optional] box type.
 *
 * Problem: This serializer only works as expected when **deserializing**. This is acceptable, as we usually only get optionals from REST-requests, and do not respond with optionals. This however makes testing hard. TODO: In the future, this should be fixed.
 *
 * See also: [Blog entry by Alex Vanyo]([https://livefront.com/writing/kotlinx-serialization-de-serializing-jsons-nullable-optional-properties/])
 *
 * @param T The type inside the optional
 * @param elementSerializer The serializer for [T]
 */
class OptionalSerializer<T>(private val elementSerializer: KSerializer<T>) : KSerializer<Optional<T>> {

    override val descriptor = elementSerializer.descriptor

    override fun deserialize(decoder: Decoder): Optional<T> =
        present(elementSerializer.deserialize(decoder))

    override fun serialize(encoder: Encoder, value: Optional<T>) {
        when (value) {
            is Optional.Empty -> throw SerializationException("Tried to serialize an Optional instance that has not value set. Is 'encodeDefaults false?'")
            is Optional.Present -> {
                elementSerializer.serialize(encoder, value.value)
            }
        }
    }

}