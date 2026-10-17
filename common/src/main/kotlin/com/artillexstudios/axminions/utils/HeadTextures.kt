package com.artillexstudios.axminions.utils

import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section
import com.artillexstudios.axapi.utils.ItemBuilder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.util.Base64

object HeadTextures {
    /**
     * Turns the texture of a head into the base64 value the game reads.
     * It can already be that value, or the url of the texture (http://textures.minecraft.net/texture/...).
     */
    fun encode(texture: String): String {
        val value = texture.trim()
        if (!value.startsWith("http://", true) && !value.startsWith("https://", true)) {
            return value
        }

        val json = "{\"textures\":{\"SKIN\":{\"url\":\"$value\"}}}"
        return Base64.getEncoder().encodeToString(json.toByteArray(Charsets.UTF_8))
    }

    /**
     * Creates the item of the section like [ItemBuilder.create] does, but the texture of a head can be an url too.
     */
    fun item(section: Section, vararg resolvers: TagResolver): ItemBuilder {
        val builder = ItemBuilder.create(section, *resolvers)
        val texture = section.getString("texture") ?: return builder
        val encoded = encode(texture)

        return if (encoded == texture) builder else builder.setTextureValue(encoded)
    }
}
