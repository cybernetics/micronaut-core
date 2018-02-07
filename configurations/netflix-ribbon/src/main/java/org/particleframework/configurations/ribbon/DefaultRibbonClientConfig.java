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

import org.particleframework.context.annotation.ConfigurationProperties;
import org.particleframework.context.annotation.Primary;
import org.particleframework.context.env.Environment;

import javax.inject.Singleton;

/**
 * The default configuration for Ribbon that delegates to the {@link Environment} to resolve properties
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@Singleton
@Primary
@ConfigurationProperties(DefaultRibbonClientConfig.PREFIX)
public class DefaultRibbonClientConfig extends AbstractRibbonClientConfig {


    public DefaultRibbonClientConfig(Environment environment) {
        super(environment);
    }


}
