package technology.polygon.polygonid_android_sdk

import com.google.protobuf.Message
import technology.polygon.polygonid_protobuf.FieldOptions.isOptional
import kotlin.reflect.KClass

inline fun <reified T : Message> T.check(): T {
    descriptorForType.fields.forEach { descriptor ->
        when {
            descriptor.javaClass == Message::class.java -> {
                val message = this.getField(descriptor) as Message
                message.validate()
            }
            !descriptor.options.hasExtension(isOptional) ||
                    descriptor.options.hasExtension(isOptional) && !descriptor.options.getExtension(
                isOptional
            ) ->
                if (!hasField(descriptor)) {
                    throw IllegalStateException("Field ${T::class.java.simpleName}.${descriptor.name} must be set.")
                }
        }
    }

    return this
}

fun Message.validate(): Message {
    this.check()
    return this
}

fun Message.isOf(types: List<KClass<*>>, throws: Boolean = true): Boolean {
    val isOf = this.whichOf(types = types) != null

    if (throws && !isOf) {
        throw IllegalStateException("Message ${this::class.java.simpleName} is not of type ${types.joinToString { it.java.simpleName }}")
    }

    return isOf
}

fun Message.whichOf(types: List<KClass<*>>): KClass<*>? {
    return types.find { it.java.isAssignableFrom(this::class.java) }
}
