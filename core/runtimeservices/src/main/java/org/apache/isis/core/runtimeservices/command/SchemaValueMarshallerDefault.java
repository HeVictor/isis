package org.apache.isis.core.runtimeservices.command;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.jaxb.JavaTimeXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.util.schema.CommonDtoUtils;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.semantics.Converter;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.applib.value.semantics.ValueSemanticsResolver;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.services.schema.SchemaValueMarshallerAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.schema.cmd.v2.ParamDto;
import org.apache.isis.schema.cmd.v2.PropertyDto;
import org.apache.isis.schema.common.v2.BlobDto;
import org.apache.isis.schema.common.v2.ClobDto;
import org.apache.isis.schema.common.v2.CollectionDto;
import org.apache.isis.schema.common.v2.EnumDto;
import org.apache.isis.schema.common.v2.TypedTupleDto;
import org.apache.isis.schema.common.v2.ValueDto;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;
import org.apache.isis.schema.ixn.v2.ActionInvocationDto;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

@Service
@Named("isis.runtimeservices.SchemaValueMarshallerDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor
public class SchemaValueMarshallerDefault
extends SchemaValueMarshallerAbstract {

    @Inject private ValueSemanticsResolver valueSemanticsResolver;
    @Inject private SpecificationLoader specLoader;

    // -- RECOVER VALUES FROM DTO

    @Override
    protected ManagedObject recoverScalarValue(
            @NonNull final ValueTypeHelper valueTypeHelper,
            @NonNull final ValueWithTypeDto valueDto) {

        val valueAsObject = CommonDtoUtils.getValueAsObject(valueDto);

        if(valueAsObject==null) {
            return ManagedObject.empty(valueTypeHelper.getElementType());
        }

        val feature = valueTypeHelper.getFeature();
        val elementSpec = valueTypeHelper.getElementType();

        final ValueTypeWrapper<?> valueWrapper = wrap(feature.getFeatureIdentifier(), elementSpec);

        if(valueDto.getComposite()!=null) {
            return ManagedObject.of(elementSpec,
                    fromTypedTuple(valueDto.getComposite()));
        }

        val recoveredValue = ManagedObject.of(elementSpec,
                valueWrapper.fromFundamentalValue(CommonDtoUtils.getValueAsObject(valueDto)));
        return recoveredValue;
    }

    // -- RECORD VALUES INTO DTO

    @Override
    public ActionInvocationDto recordActionResultScalar(
            final @NonNull ActionInvocationDto invocationDto,
            final @NonNull ObjectSpecification returnType,
            final @NonNull ManagedObject value) {
        final ValueTypeWrapper<?> valueWrapper = wrap(actionIdentifier(invocationDto), returnType);
        invocationDto.setReturned(
                recordValue(new ValueWithTypeDto(), valueWrapper, value));
        return invocationDto;
    }

    @Override
    public ActionInvocationDto recordActionResultNonScalar(
            final @NonNull ActionInvocationDto invocationDto,
            final @NonNull ObjectSpecification elementType,
            final @NonNull Can<ManagedObject> value) {
        final ValueTypeWrapper<?> valueWrapper = wrap(actionIdentifier(invocationDto), elementType);
        invocationDto.setReturned(
                recordValues(new ValueWithTypeDto(), valueWrapper, value));
        return invocationDto;
    }

    @Override
    public PropertyDto recordPropertyValue(
            final @NonNull PropertyDto propertyDto,
            final @NonNull ObjectSpecification propertyType,
            final @NonNull ManagedObject value) {
        final Identifier propertyIdentifier = propertyIdentifier(propertyDto);

        // guard against property not being a scalar
        {
            final OneToOneAssociation property =
                    (OneToOneAssociation)specLoader.loadFeatureElseFail(propertyIdentifier);

            val elementType = property.getElementType().getCorrespondingClass();
            _Assert.assertEquals(elementType, propertyType.getCorrespondingClass());
        }

        final ValueTypeWrapper<?> valueWrapper = wrap(propertyIdentifier, propertyType);
        propertyDto.setNewValue(
                recordValue(new ValueWithTypeDto(), valueWrapper, value));
        return propertyDto;
    }

    @Override
    public ParamDto recordParamScalar(
            final @NonNull Identifier paramIdentifier,
            final @NonNull ParamDto paramDto,
            final @NonNull ObjectSpecification paramType,
            final @NonNull ManagedObject value) {

        final ObjectActionParameter actionParameter =
                (ObjectActionParameter)specLoader.loadFeatureElseFail(paramIdentifier);

        _Assert.assertTrue(actionParameter.getFeatureType() == FeatureType.ACTION_PARAMETER_SCALAR);

        final ValueTypeWrapper<?> valueWrapper = wrap(paramIdentifier, paramType);

        //          ValueType valueType = valueWrapper.getValueType();
        //
        //          // this hack preserves previous behavior before we were able to serialize blobs and clobs into XML
        //          // however, we also don't want this new behavior for parameter arguments
        //          // (else these large objects could end up being persisted).
        //          if(valueType == ValueType.BLOB) valueType = ValueType.REFERENCE;
        //          if(valueType == ValueType.CLOB) valueType = ValueType.REFERENCE;
        recordValue(paramDto, valueWrapper, value);
        return paramDto;
    }

    @Override
    public ParamDto recordParamNonScalar(
            final @NonNull Identifier paramIdentifier,
            final @NonNull ParamDto paramDto,
            final @NonNull ObjectSpecification elementType,
            final @NonNull Can<ManagedObject> values) {

        final ObjectActionParameter actionParameter =
                (ObjectActionParameter)specLoader.loadFeatureElseFail(paramIdentifier);

        _Assert.assertTrue(actionParameter.getFeatureType() == FeatureType.ACTION_PARAMETER_COLLECTION);

        final ValueTypeWrapper<?> valueWrapper = wrap(paramIdentifier, elementType);
        recordValues(paramDto, valueWrapper, values);
        return paramDto;
    }

    // -- HELPER

    @Value(staticConstructor = "of")
    private static class ValueTypeWrapper<T> {
        final @NonNull ValueType valueType;
        final @NonNull ObjectSpecification spec;
        final @Nullable ValueSemanticsProvider<T> semantics;

        public T fromFundamentalValue(final Object fundamentalValue) {
            val valuePojo = supportsConversionViaEncoderDecoder()
                    ? encoderDecoder().fromEncodedString((String)fundamentalValue)
                    : converter()!=null
                        ? converter().fromDelegateValue(_Casts.uncheckedCast(fundamentalValue))
                        : fundamentalValue;
            return _Casts.uncheckedCast(valuePojo);
        }

        public Object toFundamentalValue(final T valuePojo) {
            return supportsConversionViaEncoderDecoder()
                    ? encoderDecoder().toEncodedString(valuePojo)
                    : converter()!=null
                        ? converter().toDelegateValue(valuePojo)
                        : valuePojo;
        }

        private boolean supportsConversionViaEncoderDecoder() {
            return semantics!=null
                    && semantics.getSchemaValueType() == ValueType.STRING
                    && !semantics.getCorrespondingClass().equals(String.class)
                    && encoderDecoder()!=null;
        }

        private EncoderDecoder<T> encoderDecoder() {
            return semantics!=null
                    ? semantics.getEncoderDecoder()
                    : null;
        }

        private Converter<T, ?> converter() {
            return semantics!=null
                    ? semantics.getConverter()
                    : null;
        }

        public TypedTupleDto toTypedTupleDto(final Object pojo) {
            // FIXME[ISIS-2877] implement
            return null;
        }

    }

    private ValueTypeWrapper<?> wrap(
            final @NonNull Identifier featureIdentifier,
            final @NonNull ObjectSpecification elementTypeSpec) {
        return valueSemanticsResolver.selectValueSemantics(featureIdentifier, elementTypeSpec.getCorrespondingClass())
        .getFirst()
        .map(valueSemantics->ValueTypeWrapper.of(valueSemantics.getSchemaValueType(), elementTypeSpec, valueSemantics))
        // assume reference otherwise
        .orElseGet(()->ValueTypeWrapper.of(ValueType.REFERENCE, elementTypeSpec, null));
    }

    // -- HELPER - RECORDING

    private <D extends ValueDto, T> D recordValue(
            final D valueDto,
            final ValueTypeWrapper<T> valueWrapper,
            final ManagedObject value) {

        value.getBookmark()
        .ifPresentOrElse(
                bookmark->valueDto.setReference(bookmark.toOidDto()),
                ()->recordFundamentalValue(
                        valueDto, valueWrapper, valueWrapper.toFundamentalValue((T)value.getPojo())));

        return valueDto;
    }

    private <D extends ValueDto, T> D recordFundamentalValue(
            final D valueDto,
            final ValueTypeWrapper<T> valueWrapper,
            final Object pojo) {

        val schemaValueType = valueWrapper.getValueType();

        if(valueDto instanceof ValueWithTypeDto) {
            ((ValueWithTypeDto)valueDto).setType(schemaValueType);
        }

        if(pojo==null) {
            return valueDto;
        }

        switch (schemaValueType) {
        case COLLECTION: {
            throw _Exceptions.unexpectedCodeReach();
        }
        case COMPOSITE: {
            valueDto.setComposite(valueWrapper.toTypedTupleDto(pojo));
            return valueDto;
        }
        case STRING: {
            final String argValue = (String) pojo;
            valueDto.setString(argValue);
            return valueDto;
        }
        case BYTE: {
            final Byte argValue = (Byte) pojo;
            valueDto.setByte(argValue);
            return valueDto;
        }
        case SHORT: {
            final Short argValue = (Short) pojo;
            valueDto.setShort(argValue);
            return valueDto;
        }
        case INT: {
            final Integer argValue = (Integer) pojo;
            valueDto.setInt(argValue);
            return valueDto;
        }
        case LONG: {
            final Long argValue = (Long) pojo;
            valueDto.setLong(argValue);
            return valueDto;
        }
        case CHAR: {
            final Character argValue = (Character) pojo;
            valueDto.setChar("" + argValue);
            return valueDto;
        }
        case BOOLEAN: {
            final Boolean argValue = (Boolean) pojo;
            valueDto.setBoolean(argValue);
            return valueDto;
        }
        case FLOAT: {
            final Float argValue = (Float) pojo;
            valueDto.setFloat(argValue);
            return valueDto;
        }
        case DOUBLE: {
            final Double argValue = (Double) pojo;
            valueDto.setDouble(argValue);
            return valueDto;
        }
        case BIG_INTEGER: {
            final BigInteger argValue = (BigInteger) pojo;
            valueDto.setBigInteger(argValue);
            return valueDto;
        }
        case BIG_DECIMAL: {
            final BigDecimal argValue = (BigDecimal) pojo;
            valueDto.setBigDecimal(argValue);
            return valueDto;
        }
        case LOCAL_DATE: {
            final LocalDate argValue = (LocalDate) pojo;
            valueDto.setLocalDate(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case LOCAL_TIME: {
            final LocalTime argValue = (LocalTime) pojo;
            valueDto.setLocalTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case LOCAL_DATE_TIME: {
            final LocalDateTime argValue = (LocalDateTime) pojo;
            valueDto.setLocalDateTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case OFFSET_DATE_TIME: {
            final OffsetDateTime argValue = (OffsetDateTime) pojo;
            valueDto.setOffsetDateTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case OFFSET_TIME: {
            final OffsetTime argValue = (OffsetTime) pojo;
            valueDto.setOffsetTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case ZONED_DATE_TIME: {
            final ZonedDateTime argValue = (ZonedDateTime) pojo;
            valueDto.setZonedDateTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case ENUM: {
            final Enum<?> argValue = (Enum<?>) pojo;
            final EnumDto enumDto = new EnumDto();
            valueDto.setEnum(enumDto);
            enumDto.setEnumType(argValue.getClass().getName());
            enumDto.setEnumName(argValue.name());
            return valueDto;
        }
        case REFERENCE: {
            // at this point, we know the value has no bookmark
            // so leave the DTO empty (representing a null reference)
            return valueDto;
        }
        case BLOB: {
            final Blob blob = (Blob) pojo;
            if(blob != null) {
                final BlobDto blobDto = new BlobDto();
                blobDto.setName(blob.getName());
                blobDto.setBytes(blob.getBytes());
                blobDto.setMimeType(blob.getMimeType().toString());
                valueDto.setBlob(blobDto);
            }
            return valueDto;
        }
        case CLOB: {
            final Clob clob = (Clob) pojo;
            if(clob != null) {
                final ClobDto clobDto = new ClobDto();
                clobDto.setName(clob.getName());
                clobDto.setChars(clob.getChars().toString());
                clobDto.setMimeType(clob.getMimeType().toString());
                valueDto.setClob(clobDto);
            }
            return valueDto;
        }
        case VOID: {
            return null;
        }
        default:
            throw _Exceptions.unmatchedCase(schemaValueType);
        }
    }

    private <D extends ValueWithTypeDto, T> D recordValues(
            final D valueWithTypeDto,
            final ValueTypeWrapper<T> valueWrapper,
            final Can<ManagedObject> values) {

        valueWithTypeDto.setType(ValueType.COLLECTION);
        valueWithTypeDto.setCollection(asCollectionDto(values, valueWrapper));
        valueWithTypeDto.setNull(false);
        return valueWithTypeDto;
    }

    private <T> CollectionDto asCollectionDto(
            final Can<ManagedObject> values,
            final ValueTypeWrapper<T> valueWrapper) {

        val elementValueType = valueWrapper.getValueType();
        val collectionDto = new CollectionDto();
        collectionDto.setType(elementValueType);

        values.stream()
        .forEach(element->{
            val valueDto = new ValueDto();
            collectionDto.getValue().add(valueDto);
            recordValue(valueDto, valueWrapper, element);
        });

        return collectionDto;
    }

    // -- HELPER - RECOVERY

    private Object fromTypedTuple(final TypedTupleDto typedTupleDto) {
        // FIXME[ISIS-2877] implement
        return null;
    }

    // -- DEPENDENCIES

    @Override
    protected final SpecificationLoader getSpecificationLoader() {
        return specLoader;
    }

    @Override
    protected ValueSemanticsResolver getValueSemanticsResolver() {
        return valueSemanticsResolver;
    }

}
