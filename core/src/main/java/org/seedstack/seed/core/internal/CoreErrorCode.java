/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import org.seedstack.seed.ErrorCode;

public enum CoreErrorCode implements ErrorCode {
    UNABLE_TO_INSTANTIATE_MODULE,
    UNABLE_TO_INJECT_LOGGER,
    UNABLE_TO_FIND_CLASSLOADER,
    UNABLE_TO_LOAD_SEED_BOOTSTRAP,
    UNEXPECTED_EXCEPTION,
    UNABLE_TO_CREATE_DIAGNOSTIC_COLLECTOR,
    MISSING_SEED_ENTRY_POINT,
    MULTIPLE_SEED_ENTRY_POINTS,
    RETHROW_EXCEPTION_AFTER_DIAGNOSTIC_FAILURE,
    UNABLE_TO_INSTANTIATE_CLASS,
    ERROR_DURING_LIFECYCLE_CALLBACK,
    UNABLE_TO_CREATE_PROXY,
    UNABLE_TO_LOAD_CONFIGURATION_RESOURCE,
    STORAGE_PATH_IS_NOT_A_DIRECTORY,
    UNABLE_TO_CREATE_STORAGE_DIRECTORY,
    STORAGE_DIRECTORY_IS_NOT_WRITABLE,
    NO_LOCAL_STORAGE_CONFIGURED,
    UNABLE_TO_INJECT_CONFIGURATION_VALUE,
    TOOL_NOT_FOUND, MULTIPLE_TOOLS_WITH_IDENTICAL_NAMES, INVALID_TOOL, MISSING_CONFIGURATION_KEY
}