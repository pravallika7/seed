/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.common.collect.Lists;
import org.seedstack.seed.core.internal.cli.CliPlugin;
import org.seedstack.seed.core.internal.configuration.ConfigurationPlugin;
import org.seedstack.seed.spi.SeedTool;

import java.util.Collection;

public abstract class AbstractSeedTool extends AbstractSeedPlugin implements SeedTool {
    @Override
    public String name() {
        return toolName() + "-tool";
    }

    @Override
    public Collection<Class<?>> pluginsToLoad() {
        return Lists.newArrayList(CorePlugin.class, ConfigurationPlugin.class, CliPlugin.class);
    }

    public abstract String toolName();
}
