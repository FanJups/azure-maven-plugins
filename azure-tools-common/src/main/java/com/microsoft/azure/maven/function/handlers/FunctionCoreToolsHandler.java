/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.maven.function.handlers;

public interface FunctionCoreToolsHandler {
    void installExtension(String deploymentStagingDirectoryPath, String baseDir) throws Exception;
}