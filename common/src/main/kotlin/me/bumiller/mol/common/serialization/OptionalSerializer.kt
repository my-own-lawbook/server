package me.bumiller.mol.common.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import me.bumiller.mol.common.Optional
import me.bumiller.mol.common.present

/**
 * Serializer for the custom [Optional] box type.
 *
 * See also: [Blog entry by Alex Vanyo]([https://livefront.com/writing/kotlinx-serialization-de-serializing-jsons-nullable-optional-properties/])
 *
 * @param T The type inside the optional
 * @param elementSerializer The serializer for [T]
 */
class OptionalSerializer<T>(val elementSerializer: KSerializer<T>) : KSerializer<Optional<T>> {

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