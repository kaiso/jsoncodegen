package de.lisaplus.atlas.model

import com.sun.org.apache.xpath.internal.operations.Or

/**
 * Type used in meta model
 * Created by eiko on 01.06.17.
 */
abstract class BaseType {
    boolean isArray=false
    final static String WRONG_TYPE = 'CODEGEN-ERROR: Used type needs to derived from de.lisaplus.atlas.model.BaseType: '
    final static String UNKNOWN_TYPE = 'CODEGEN-ERROR: Unknown type: '
    final static String UNSUPPORTED_TYPE = 'CODEGEN-ERROR: Unsupported type: '

    /**
     * This attribute is only used in case of XSD imports to avoid the loose of additional type information, f.e. double, float
     */
    String originalType=null

    abstract String name()

    /**
     * Reuse existing copies or create new ones if they are missing. If the object is immutable, then the object itself
     * is being returned.
     * @param type The type to process, not null!
     * @param typeCopies The types, which were already copied (mapping of type name to the copy of the corresponding Type)
     * @return The copy of the BaseType
     */
    static <T extends BaseType> T copyOf(T type, Map<String, Type> typeCopies) {
        Objects.requireNonNull(type)
        switch(type) {
            // handle immutable by just returning them
            case BooleanType:
            case VoidType:
            case UnsupportedType:
            case UUIDType:
            case BooleanType:
            case DateType:
            case DateTimeType:
                return type
            case StringType:
                StringType copyS = new StringType()
                // Assume immutable!
                copyS.minLength = type.minLength
                copyS.maxLength = type.maxLength
                copyS.pattern = type.pattern
                return copyS
            case IntType:
                IntType copyI = new IntType()
                // Assume immutable!
                copyI.min = type.min
                copyI.max = type.max
                copyI.exclusiveMin = type.exclusiveMin
                copyI.exclusiveMax = type.exclusiveMax
                return copyI
            case NumberType:
                NumberType copyN = new NumberType()
                // Assume immutable!
                copyN.min = type.min
                copyN.max = type.max
                copyN.exclusiveMin = type.exclusiveMin
                copyN.exclusiveMax = type.exclusiveMax
                return copyN
            case RefType:
                RefType copyR = new RefType()
                // Assume immutable!
                copyR.typeName = type.typeName
                // if (type.type != null) println "RefType triggers copy of type ${type.type.name}"
                copyR.type = type.type == null ? null : Type.copyOf(type.type, typeCopies)
                return copyR
            case ComplexType:
                ComplexType copyC = new ComplexType()
                // if (type.type != null) println "ComplexType triggers copy of type ${type.type.name}"
                copyC.type = type.type == null ? null : Type.copyOf(type.type, typeCopies)
                return copyC
            default:
                throw new RuntimeException("Add handling for type ${type.class}")
        }
    }
}

class VoidType extends BaseType {
    String name () {
        return NAME
    }
    final static NAME='VOID'
}

/**
 * this type is choosen in too complex schemas
 * for instance: use of patternPoperties
 */
class UnsupportedType extends BaseType {
    String name () {
        return NAME
    }
    final static NAME='UNSUPPORTED'
}

class StringType extends BaseType {
    def maxLength
    def minLength
    def pattern

    String name () {
        return NAME
    }
    final static NAME='STRING'
}

class UUIDType extends BaseType {
    String name () {
        return NAME
    }
    final static NAME='UUID'
}

class BooleanType extends BaseType {
    String name () {
        return NAME
    }
    final static NAME='BOOLEAN'
}
