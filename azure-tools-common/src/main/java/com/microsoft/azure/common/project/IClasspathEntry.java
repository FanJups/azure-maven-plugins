/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.common.project;

import java.io.File;
import java.util.Collection;

public interface IClasspathEntry {
	File getJarLocation();
	Collection<File> getAdditionalDependencies();
}
