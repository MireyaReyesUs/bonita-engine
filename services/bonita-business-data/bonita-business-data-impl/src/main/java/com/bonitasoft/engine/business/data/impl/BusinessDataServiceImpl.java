package com.bonitasoft.engine.business.data.impl;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.JavaMethodInvoker;
import org.bonitasoft.engine.commons.exceptions.SReflectException;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.bdm.model.field.RelationField.Type;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.BusinessDataService;
import com.bonitasoft.engine.business.data.JsonBusinessDataSerializer;
import com.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryException;

public class BusinessDataServiceImpl implements BusinessDataService {

    private final BusinessDataRepository businessDataRepository;
    private final JsonBusinessDataSerializer jsonBusinessDataSerializer;

    public BusinessDataServiceImpl(final BusinessDataRepository businessDataRepository, final JsonBusinessDataSerializer jsonBusinessDataSerializer) {
        this.businessDataRepository = businessDataRepository;
        this.jsonBusinessDataSerializer = jsonBusinessDataSerializer;
    }

    @Override
    public boolean isBusinessData(final Object data) {
        return isEntity(data) || isListOfEntities(data);
    }

    private boolean isListOfEntities(final Object data) {
        if (data == null) {
            return false;
        }
        if (!List.class.isAssignableFrom(data.getClass())) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        final List dataList = (List) data;
        if (dataList.isEmpty()) {
            return true;
        }
        return isEntity(dataList.get(0));

    }

    private boolean isEntity(final Object data) {
        if (data == null) {
            return false;
        }
        return Entity.class.isAssignableFrom(data.getClass());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object callJavaOperation(final Object businessObject, final Object valueToSetObjectWith, final String methodName, final String parameterType)
            throws SBusinessDataNotFoundException, SBusinessDataRepositoryException {
        if (businessObject == null) {
            throw new SBusinessDataNotFoundException("business data is null");
        }
        if (isEntity(businessObject)) {
            return callJavaOperationOnEntity((Entity) businessObject, valueToSetObjectWith, methodName, parameterType);
        }
        if (isListOfEntities(businessObject)) {
            return callJavaOperationOnEntityList((List<Entity>) businessObject, valueToSetObjectWith, methodName, parameterType);
        }
        throw new SBusinessDataRepositoryException("not a business data");
    }

    private Object callJavaOperationOnEntityList(final List<Entity> businessObject, final Object valueToSetObjectWith, final String methodName,
            final String parameterType)
            throws SBusinessDataRepositoryException, SBusinessDataNotFoundException {
        try {
            invokeJavaMethod(businessObject, methodName, parameterType, valueToSetObjectWith);
            return businessObject;
        } catch (final Exception e) {
            throw new SBusinessDataRepositoryException(e);
        }
    }

    private Object callJavaOperationOnEntity(final Entity businessObject, final Object valueToSetObjectWith, final String methodName, final String parameterType)
            throws SBusinessDataRepositoryException, SBusinessDataNotFoundException {

        final Entity jpaEntity;
        if (businessObject.getPersistenceId() == null) {
            jpaEntity = copyForServer(businessObject);
        } else {
            jpaEntity = businessDataRepository.findById(businessObject.getClass(), businessObject.getPersistenceId());
        }
        final Object valueToSet = loadValueToSet(businessObject, valueToSetObjectWith, methodName);
        try {
            invokeJavaMethod(jpaEntity, methodName, parameterType, valueToSet);
            return copyForClient(jpaEntity);
        } catch (final Exception e) {
            throw new SBusinessDataRepositoryException(e);
        }
    }

    protected void invokeJavaMethod(final Object objectToSet, final String methodName, final String parameterType, final Object valueToSet)
            throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final JavaMethodInvoker methodInvoker = new JavaMethodInvoker();
        methodInvoker.invokeJavaMethod(parameterType, valueToSet, objectToSet, methodName, parameterType);
    }

    private Object copyForClient(final Entity jpaEntity) {
        // TODO Auto-generated method stub
        return jpaEntity;
    }

    @SuppressWarnings("unchecked")
    private Object loadValueToSet(final Entity businessObject, final Object valueToSetObjectWith, final String methodName)
            throws SBusinessDataNotFoundException, SBusinessDataRepositoryException {
        Object valueToSet;
        if (isEntity(valueToSetObjectWith)) {
            final Type relationType = getRelationType(businessObject, methodName);
            valueToSet = getPersistedValue((Entity) valueToSetObjectWith, relationType);
        }
        else if (isListOfEntities(valueToSetObjectWith)) {
            final Type relationType = getRelationType(businessObject, methodName);
            valueToSet = getPersistedValues((List<Entity>) valueToSetObjectWith, relationType);
        } else {
            valueToSet = valueToSetObjectWith;
        }
        return valueToSet;
    }

    private List<Long> getPrimaryKeys(final List<Entity> entities) throws SBusinessDataNotFoundException {
        List<Long> primaryKeys;
        primaryKeys = new ArrayList<Long>();
        for (final Entity entity : entities) {
            if (entity.getPersistenceId() == null) {
                throw new SBusinessDataNotFoundException("persistenceId of business data is null");
            }
            primaryKeys.add(entity.getPersistenceId());
        }
        return primaryKeys;
    }

    private Object getPersistedValues(final List<Entity> entities, Type type) throws SBusinessDataNotFoundException {
        if (entities.isEmpty()) {
            return new ArrayList<Entity>();
        }
        if (Type.AGGREGATION.equals(type)) {
            return businessDataRepository.findByIds(entities.get(0).getClass(), getPrimaryKeys(entities));
        }
        else {
            return copyForServer(entities);
        }
    }

    private Entity getPersistedValue(final Entity entity, final Type type) throws SBusinessDataNotFoundException {
        if (Type.AGGREGATION.equals(type)) {
            return businessDataRepository.findById(entity.getClass(), entity.getPersistenceId());
        }
        else {
            return copyForServer(entity);
        }
    }

    private List<Entity> copyForServer(List<Entity> entities) {
        return entities;
    }

    private Entity copyForServer(final Entity entity) {
        return entity;
    }

    private Type getRelationType(final Entity businessObject, final String methodName) throws SBusinessDataRepositoryException {
        final String fieldName = ClassReflector.getFieldName(methodName);
        Annotation[] annotations;
        try {
            annotations = businessObject.getClass().getDeclaredField(fieldName).getAnnotations();
        } catch (final NoSuchFieldException e) {
            return null;
        } catch (final SecurityException e) {
            throw new SBusinessDataRepositoryException(e);
        }
        for (final Annotation annotation : annotations) {
            final Set<Class<? extends Annotation>> annotationKeySet = getAnnotationKeySet();
            if (annotationKeySet.contains(annotation.annotationType())) {
                try {
                    final Method cascade = annotation.getClass().getMethod("cascade");
                    CascadeType[] cascadeTypes = (CascadeType[]) cascade.invoke(annotation);
                    if (CascadeType.MERGE.equals(cascadeTypes[0])) {
                        return Type.AGGREGATION;
                    }
                    if (CascadeType.ALL.equals(cascadeTypes[0])) {
                        return Type.COMPOSITION;
                    }
                } catch (Exception e) {
                    throw new SBusinessDataRepositoryException(e);
                }
            }
        }
        return null;
    }

    private Set<Class<? extends Annotation>> getAnnotationKeySet() {
        //FIXME use custom annotation on methods
        final Set<Class<? extends Annotation>> annotationKeySet = new HashSet<Class<? extends Annotation>>();
        annotationKeySet.add(OneToOne.class);
        annotationKeySet.add(OneToMany.class);
        annotationKeySet.add(ManyToMany.class);
        annotationKeySet.add(ManyToOne.class);
        return annotationKeySet;
    }

    @Override
    public Serializable getJsonEntity(final String entityClassName, final Long identifier, final String businessDataURIPattern)
            throws SBusinessDataNotFoundException, SBusinessDataRepositoryException {
        final Class<? extends Entity> entityClass = loadClass(entityClassName);
        final Entity entity = businessDataRepository.findById(entityClass, identifier);
        try {
            return jsonBusinessDataSerializer.serializeEntity(entity, businessDataURIPattern);
        } catch (final Exception e) {
            throw new SBusinessDataRepositoryException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Serializable getJsonChildEntity(final String entityClassName, final Long identifier, final String childFieldName, final String businessDataURIPattern)
            throws SBusinessDataNotFoundException, SBusinessDataRepositoryException {
        final Class<? extends Entity> entityClass = loadClass(entityClassName);
        final Object entity = businessDataRepository.findById(entityClass, identifier);

        Object childEntity;
        java.lang.reflect.Type getterReturnType;
        try {
            final String getterName = ClassReflector.getGetterName(childFieldName);
            childEntity = ClassReflector.invokeGetter(entity, getterName);
            getterReturnType = ClassReflector.getGetterReturnType(entityClass, getterName);
        } catch (final SReflectException e) {
            throw new SBusinessDataRepositoryException(e);
        }

        if (childEntity == null) {
            return JsonBusinessDataSerializer.EMPTY_OBJECT;
        }
        if (childEntity instanceof Entity) {
            final Entity unwrap = businessDataRepository.unwrap((Entity) childEntity);
            try {
                return jsonBusinessDataSerializer.serializeEntity(unwrap, businessDataURIPattern);
            } catch (final Exception e) {
                throw new SBusinessDataRepositoryException(e);
            }
        } else if (childEntity instanceof List) {
            final Class<?> type = (Class<?>) ((ParameterizedType) getterReturnType).getActualTypeArguments()[0];
            if (Entity.class.isAssignableFrom(type)) {
                try {
                    return jsonBusinessDataSerializer.serializeEntity((List<Entity>) childEntity, businessDataURIPattern);
                } catch (final Exception e) {
                    throw new SBusinessDataRepositoryException(e);
                }
            }
        }
        return null;

    }

    @SuppressWarnings("unchecked")
    protected Class<? extends Entity> loadClass(final String returnType) throws SBusinessDataRepositoryException {
        try {
            return (Class<? extends Entity>) Thread.currentThread().getContextClassLoader().loadClass(returnType);
        } catch (final ClassNotFoundException e) {
            throw new SBusinessDataRepositoryException(e);
        }
    }

}