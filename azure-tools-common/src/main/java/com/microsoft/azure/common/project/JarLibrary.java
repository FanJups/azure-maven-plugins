/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.common.project;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Preconditions;


public class JarLibrary implements IClasspathEntry {
    private File jarFile;

    public JarLibrary(File jarFile) {
        Preconditions.checkNotNull(jarFile);
        this.jarFile = jarFile;
    }

    public File getJarFile() {
        return jarFile;
    }

	@Override
	public File getJarLocation() {
		return jarFile;
	}

	@Override
	public Collection<File> getAdditionalDependencies() {
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return jarFile.getAbsolutePath();
	}

}
