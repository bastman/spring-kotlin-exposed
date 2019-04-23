package com.example.api.common.rest.serialization

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ReferenceTypeSerializer
import com.fasterxml.jackson.databind.type.ReferenceType
import com.fasterxml.jackson.databind.util.NameTransformer
import org.funktionale.option.Option

class PatchableModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        super.setupModule(context)

        addDeserializer(Patchable::class.java, PatchableDeserializer())
        //  addSerializer(Patchable::class.java, PatchableSerializer::class)
    }
}

sealed class Patchable<T> {
    class Undefined<T> : Patchable<T>()
    class Null<T> : Patchable<T>()
    data class Present<T>(val content: T) : Patchable<T>()

    @JsonValue
    fun value(): T? =
            when (this) {
                is Undefined -> null
                is Null -> null
                is Present -> content
            }

    companion object {
        // fun undefined() = Undefined()
    }
}

class PatchableDeserializer() : JsonDeserializer<Patchable<*>>(), ContextualDeserializer {

    private var valueType: Class<*>? = null

    constructor(valueType: Class<*>? = null) : this() {
        this.valueType = valueType
    }

    override fun createContextual(ctxt: DeserializationContext?, property: BeanProperty?): JsonDeserializer<*> {
        val wrapperType = property?.type

        val rawClass = wrapperType?.containedType(0)?.rawClass
        return PatchableDeserializer(rawClass)
    }

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Patchable<*> {
        val f = p!!.readValueAs(valueType)
        return Patchable.Present(f)
    }


    override fun getNullValue(ctxt: DeserializationContext?): Patchable<Any> =
            if (ctxt?.parser?.currentToken == JsonToken.VALUE_NULL)
                Patchable.Null()
            //Patchable.ofNull()
            else Patchable.Undefined()
    //Patchable.undefined()
}


class PatchableSerializer : ReferenceTypeSerializer<Patchable<*>> // since 2.9
{


    protected constructor(fullType: ReferenceType, staticTyping: Boolean,
                          vts: TypeSerializer, ser: JsonSerializer<Any>) : super(fullType, staticTyping, vts, ser) {
    }

    protected constructor(base: PatchableSerializer, property: BeanProperty,
                          vts: TypeSerializer, valueSer: JsonSerializer<*>, unwrapper: NameTransformer,
                          suppressableValue: Any, suppressNulls: Boolean) : super(base, property, vts, valueSer, unwrapper,
            suppressableValue, suppressNulls) {
    }

    override fun withResolved(prop: BeanProperty,
                              vts: TypeSerializer, valueSer: JsonSerializer<*>,
                              unwrapper: NameTransformer): ReferenceTypeSerializer<Patchable<*>> {
        return PatchableSerializer(this, prop, vts, valueSer, unwrapper,
                _suppressableValue, _suppressNulls)
    }

    override fun withContentInclusion(suppressableValue: Any,
                                      suppressNulls: Boolean): ReferenceTypeSerializer<Patchable<*>> {
        return PatchableSerializer(this, _property, _valueTypeSerializer,
                _valueSerializer, _unwrapper,
                suppressableValue, suppressNulls)
    }


    override fun _isValuePresent(value: Patchable<*>): Boolean {
        return value is Patchable.Present
    }

    override fun _getReferenced(value: Patchable<*>): Any {
        return when (value) {
            is Patchable.Present -> value.content!!
            else -> error("foo")
        }
    }

    override fun _getReferencedIfPresent(value: Patchable<*>): Any? {
        return when (value) {
            is Patchable.Present -> value.content
            is Patchable.Null -> null
            else -> null
        }
    }

    companion object {
        private val serialVersionUID = 1L
    }
}

fun <T : Any> Patchable<T>.toOption(): Option<T?> =
        when (this) {
            is Patchable.Present -> Option.Some(content)
            is Patchable.Null -> Option.Some(null)
            is Patchable.Undefined -> Option.None
        }
