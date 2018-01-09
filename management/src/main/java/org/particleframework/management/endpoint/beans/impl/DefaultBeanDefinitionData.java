/*
 * Copyright 2017 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.particleframework.management.endpoint.beans.impl;

import org.particleframework.context.annotation.Requires;
import org.particleframework.inject.BeanDefinition;
import org.particleframework.management.endpoint.beans.BeanDefinitionData;
import org.particleframework.management.endpoint.beans.BeansEndpoint;

import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The default {@link BeanDefinitionData} implementation. Returns a {@link Map} with
 * 3 keys; "dependencies": A list of class names the bean depends on, "scope": The
 * scope of the bean {@link javax.inject.Scope}, "type": The bean class name.
 *
 * @author James Kleeh
 * @since 1.0
 */
@Singleton
@Requires(beans = BeansEndpoint.class)
public class DefaultBeanDefinitionData implements BeanDefinitionData<Map<String, Object>> {

    @Override
    public Map<String, Object> getData(BeanDefinition<?> beanDefinition) {
        Map<String, Object> beanData = new LinkedHashMap<>(3);
        beanData.put("dependencies", getDependencies(beanDefinition));
        beanData.put("scope", getScope(beanDefinition));
        beanData.put("type", getType(beanDefinition));

        return beanData;
    }

    protected List getDependencies(BeanDefinition<?> beanDefinition) {
        return beanDefinition.getRequiredComponents().stream().map(Class::getName).collect(Collectors.toList());
    }

    protected String getScope(BeanDefinition<?> beanDefinition) {
        return beanDefinition.getScope().map(Class::getSimpleName).map(String::toLowerCase).orElse(null);
    }

    protected String getType(BeanDefinition<?> beanDefinition) {
        return beanDefinition.getBeanType().getName();
    }
}