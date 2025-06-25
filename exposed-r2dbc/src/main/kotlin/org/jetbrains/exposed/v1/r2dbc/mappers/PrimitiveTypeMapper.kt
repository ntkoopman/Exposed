package org.jetbrains.exposed.v1.r2dbc.mappers

import io.r2dbc.postgresql.codec.Json
import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.vendors.DatabaseDialect
import kotlin.reflect.KClass

/**
 * Mapper for primitive types (Int, Long, Float, Double, etc.).
 */
class PrimitiveTypeMapper : TypeMapper {
    @Suppress("MagicNumber")
    override val priority = 0.3

    override val columnTypes: List<KClass<out IColumnType<*>>>
        get() = listOf(
            ByteColumnType::class,
            UByteColumnType::class,
            ShortColumnType::class,
            UShortColumnType::class,
            IntegerColumnType::class,
            UIntegerColumnType::class,
            LongColumnType::class,
            ULongColumnType::class,
            FloatColumnType::class,
            DoubleColumnType::class,
            DecimalColumnType::class,
            BooleanColumnType::class,
            CharacterColumnType::class,
            UUIDColumnType::class
        )

    override fun setValue(
        statement: Statement,
        dialect: DatabaseDialect,
        typeMapping: R2dbcTypeMapping,
        columnType: IColumnType<*>,
        value: Any?,
        index: Int
    ): Boolean {
        if (value != null) {
            statement.bind(index - 1, value)
            return true
        }

        val columnValueType = when (columnType) {
            is ByteColumnType -> java.lang.Byte::class.java
            is UByteColumnType -> java.lang.Short::class.java
            is ShortColumnType -> java.lang.Short::class.java
            is UShortColumnType -> java.lang.Integer::class.java
            is IntegerColumnType -> java.lang.Integer::class.java
            is UIntegerColumnType -> java.lang.Long::class.java
            is LongColumnType -> java.lang.Long::class.java
            is ULongColumnType -> java.lang.Long::class.java
            is FloatColumnType -> java.lang.Float::class.java
            is DoubleColumnType -> java.lang.Double::class.java
            is DecimalColumnType -> java.math.BigDecimal::class.java
            is UUIDColumnType -> java.util.UUID::class.java
            is CharacterColumnType -> java.lang.String::class.java
            is BooleanColumnType -> java.lang.Boolean::class.java
            else -> return false
        }
        statement.bindNull(index - 1, columnValueType)
        return true
    }

    @Suppress("UNCHECKED_CAST", "SwallowedException")
    override fun <T> getValue(row: Row, type: Class<T>, index: Int, dialect: DatabaseDialect, columnType: IColumnType<*>): ValueContainer<T?> {
        try {
            return PresentValueContainer(row.get(index - 1, type))
        } catch (e: IllegalArgumentException) {
            val value = row.get(index - 1)
            return when (value) {
                // It will return always the string, event if it doesn't match `type`
                // But Json could be fetched even with BooleanColumnType that expects that String could be returned
                is Json -> PresentValueContainer(value.asString() as T)
                else -> NoValueContainer()
            }
        }
    }
}
