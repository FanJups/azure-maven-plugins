/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.common.project;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaProject implements IClasspathEntry {
	private String projectName;

	private File basedir;

	private File outputDir;

	private File buildDir;
	
	private File targetFile;

	private String finalName;

	private String packaging;

	private boolean isStartupProject;

	private List<IClasspathEntry> dependencies;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public File getBasedir() {
		return basedir;
	}

	public void setBasedir(File basedir) {
		this.basedir = basedir;
	}

	public File getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	public String getFinalName() {
		return finalName;
	}

	public List<IClasspathEntry> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<IClasspathEntry> dependencies) {
		this.dependencies = dependencies;
	}

	public File getBuildDir() {
		return buildDir;
	}

	public void setBuildDir(File buildDir) {
		this.buildDir = buildDir;
	}

	public String getPackaging() {
		return packaging;
	}

	public void setPackaging(String packaging) {
		this.packaging = packaging;
	}

	public boolean isStartupProject() {
		return isStartupProject;
	}

	public void setStartupProject(boolean isStartupProject) {
		this.isStartupProject = isStartupProject;
	}
	
	public File getTargetFile() {
		return targetFile;
	}

	public void setTargetFile(File targetFile) {
		this.targetFile = targetFile;
		this.finalName = targetFile.getName();
	}

	@Override
	public File getJarLocation() {
		return targetFile;
	}

	@Override
	public Collection<File> getAdditionalDependencies() {
		if (dependencies == null || dependencies.isEmpty()) {
			return Collections.emptyList();
		}
		Map<String, JavaProject> allProject = new HashMap<>();
		allProject.put(this.getFinalName(), this);
		List<JavaProject> pendingList = new ArrayList<>();
		pendingList.add(this);
		for (;;) {
			List<JavaProject> newProjects = new ArrayList<>();
			for (JavaProject proj : pendingList) {
				List<JavaProject> subProjects = proj.dependencies.stream().filter(JavaProject.class::isInstance)
						.map(JavaProject.class::cast).collect(Collectors.toList());

				for (JavaProject subProj : subProjects) {
					if (allProject.putIfAbsent(subProj.getFinalName(), subProj) == null) {
						newProjects.add(proj);
					}
				}
			}
			if (newProjects.isEmpty()) {
				break;
			}
			pendingList = newProjects;
		}

		List<File> firstLevelDependency = dependencies.stream().filter(JarLibrary.class::isInstance)
				.map(d -> d.getJarLocation()).collect(Collectors.toList());
		Set<File> deps = new HashSet<>(firstLevelDependency);

		for (JavaProject proj : allProject.values()) {
			if (proj == this) {
				continue;
			}
			deps.add(proj.getJarLocation());
			deps.addAll(proj.dependencies.stream().filter(JarLibrary.class::isInstance).map(d -> d.getJarLocation())
					.collect(Collectors.toList()));
		}
		return deps;
	}

}
