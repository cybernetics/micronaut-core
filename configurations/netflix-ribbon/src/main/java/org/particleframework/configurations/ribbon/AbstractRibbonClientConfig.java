/*
 * Copyright 2018 original authors
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
package org.particleframework.configurations.ribbon;

import com.netflix.client.VipAddressResolver;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.IClientConfig;
import com.netflix.client.config.IClientConfigKey;
import org.particleframework.context.env.Environment;
import org.particleframework.core.convert.ConversionService;
import org.particleframework.core.reflect.InstantiationUtils;
import org.particleframework.core.type.Argument;

import javax.inject.Inject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract implementation of the {@link IClientConfig} interface
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public abstract class AbstractRibbonClientConfig implements IClientConfig {
    public static final String PREFIX = "ribbon";


    private final Environment environment;
    private Map<IClientConfigKey, Object> customSettings = new ConcurrentHashMap<>();
    private VipAddressResolver resolver = null;


    public AbstractRibbonClientConfig(Environment environment) {
        this.environment = environment;
    }

    /**
     * Sets an optional {@link VipAddressResolver}
     * @param resolver The {@link VipAddressResolver}
     */
    @Inject
    public void setVipAddressResolver(Optional<VipAddressResolver> resolver) {
        if(resolver.isPresent()) {
            this.resolver = resolver.get();
        }
    }

    @Override
    public String getClientName() {
        return "default";
    }

    @Override
    public String getNameSpace() {
        return PREFIX;
    }

    @Override
    public void loadProperties(String clientName) {
        // no-op, unnecessary
    }

    @Override
    public void loadDefaultValues() {
        // no-op, unnecessary
    }

    @Override
    public Map<String, Object> getProperties() {
        Map map = environment.getProperty(getNameSpace(), Argument.of(Map.class, String.class, Object.class)).orElse(Collections.EMPTY_MAP);
        Map<String, Object> all = new LinkedHashMap<>(map);
        for (Map.Entry<IClientConfigKey, Object> entry : customSettings.entrySet()) {
            all.put(entry.getKey().key(), entry.getValue());
        }
        return all;
    }

    @Override
    public void setProperty(IClientConfigKey key, Object value) {
        set(key, value);
    }

    @Override
    public Object getProperty(IClientConfigKey key) {
        return get(key, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getProperty(IClientConfigKey key, Object defaultVal) {
        return get(key, defaultVal);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsProperty(IClientConfigKey key) {
        return key != null && (customSettings.containsKey(key) || environment.get(qualifyKey(key), key.type()).isPresent());
    }

    @Override
    public int getPropertyAsInteger(IClientConfigKey key, int defaultValue) {
        return get(key, Integer.class, defaultValue);
    }

    @Override
    public String getPropertyAsString(IClientConfigKey key, String defaultValue) {
        return get(key, String.class, defaultValue);
    }

    @Override
    public boolean getPropertyAsBoolean(IClientConfigKey key, boolean defaultValue) {
        return get(key, Boolean.class, defaultValue);
    }

    @Override
    public <T> T get(IClientConfigKey<T> key) {
        return get(key, null);
    }

    @Override
    public <T> T get(IClientConfigKey<T> key, T defaultValue) {
        Class<T> type = key.type();
        return get(key, type, defaultValue);
    }

    protected <T> T get(IClientConfigKey<T> key, Class<T> type, T defaultValue) {
        if(key == null) return null;
        if(customSettings.containsKey(key)) {
            return ConversionService.SHARED.convert(customSettings.get(key), type).orElse(defaultValue);
        }
        else {
            return environment.getProperty(qualifyKey(key), type, defaultValue);
        }
    }

    @Override
    public <T> IClientConfig set(IClientConfigKey<T> key, T value) {
        if(key != null) {
            if(value == null) {
                customSettings.remove(key);
            }
            else {
                customSettings.put(key, value);
            }
        }
        return this;
    }

    @Override
    public String resolveDeploymentContextbasedVipAddresses() {
        String deploymentContextBasedVipAddressesMacro = (String) getProperty(CommonClientConfigKey.DeploymentContextBasedVipAddresses);
        if (deploymentContextBasedVipAddressesMacro == null) {
            return null;
        }
        return getVipAddressResolver().resolve(deploymentContextBasedVipAddressesMacro, this);
    }

    private VipAddressResolver getVipAddressResolver() {
        if (resolver == null) {
            synchronized (this) {
                if (resolver == null) {
                    resolver = InstantiationUtils.instantiate((String) getProperty(CommonClientConfigKey.VipAddressResolverClassName), VipAddressResolver.class);
                }
            }
        }
        return resolver;
    }

    protected String qualifyKey(IClientConfigKey key) {
        return getNameSpace() + "." + key.key();
    }

}
