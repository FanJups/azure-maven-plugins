/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.maven.function.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.microsoft.azure.common.logging.Log;
import com.microsoft.azure.maven.function.utils.CommandUtils;

public class CommandHandlerImpl implements CommandHandler {
	@Override
	public void runCommandWithReturnCodeCheck(final String command, final boolean showStdout,
			final String workingDirectory, final List<Long> validReturnCodes, final String errorMessage)
			throws Exception {
		final Process process = runCommand(command, showStdout, workingDirectory);

		handleExitValue(process.exitValue(), validReturnCodes, errorMessage, process.getInputStream());
	}

	@Override
	public void runCommandWithReturnCodeCheck2(String[] command, boolean showStdout, String workingDirectory,
			List<Long> validReturnCodes, String errorMessage) throws Exception {
		final Process process = runCommand2(command, showStdout, workingDirectory);

		handleExitValue(process.exitValue(), validReturnCodes, errorMessage, process.getInputStream());
	}

	protected Process runCommand2(final String[] command, final boolean showStdout, final String workingDirectory)
			throws Exception {
		Log.debug("Executing command: " + StringUtils.join(command, " "));

		final ProcessBuilder.Redirect redirect = getStdoutRedirect(showStdout);
		final ProcessBuilder processBuilder = new ProcessBuilder(command).redirectOutput(redirect)
				.redirectErrorStream(true);

		if (workingDirectory != null) {
			processBuilder.directory(new File(workingDirectory));
		}

		final Process process = processBuilder.start();

		if (showStdout) {

			processes.add(process);
			if (!inited) {
				inited = true;
				Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
					@Override
					public void run() {
						for (Process p : processes) {
							if (p.isAlive()) {
								p.destroyForcibly();
							}
						}

						System.out.println("Execute Hook.....");
					}
				}));
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty("line.separator"));
				if (builder.indexOf("\n") >= 0) {
					String result = builder.toString();
					System.out.print(result);
					builder = new StringBuilder();
				}
			}
		}

		process.waitFor();

		return process;
	}

	@Override
	public String runCommandAndGetOutput(final String command, final boolean showStdout, final String workingDirectory)
			throws Exception {
		final Process process = runCommand(command, showStdout, workingDirectory);

		return getOutputFromProcess(process);
	}

	protected String getOutputFromProcess(final Process process) throws IOException {
		try (final BufferedReader stdInput = new BufferedReader(
				new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")))) {
			final StringBuffer stdout = new StringBuffer();
			String s = null;
			while ((s = stdInput.readLine()) != null) {
				stdout.append(s);
			}
			return stdout.toString().trim();
		}
	}

	final static List<Process> processes = new ArrayList<>();
	static boolean inited = false;

	protected Process runCommand(final String command, final boolean showStdout, final String workingDirectory)
			throws Exception {
		Log.debug("Executing command: " + StringUtils.join(command, " "));

		final ProcessBuilder.Redirect redirect = getStdoutRedirect(showStdout);
		final ProcessBuilder processBuilder = new ProcessBuilder(buildCommand(command)).redirectOutput(redirect)
				.redirectErrorStream(true);

		if (workingDirectory != null) {
			processBuilder.directory(new File(workingDirectory));
		}

		final Process process = processBuilder.start();

		if (showStdout) {

			processes.add(process);
			if (!inited) {
				inited = true;
				Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
					@Override
					public void run() {
						for (Process p : processes) {
							if (p.isAlive()) {
								p.destroyForcibly();
							}
						}

						System.out.println("Execute Hook.....");
					}
				}));
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty("line.separator"));
				if (builder.indexOf("\n") >= 0) {
					String result = builder.toString();
					System.out.print(result);
					builder = new StringBuilder();
				}
			}
		}

		process.waitFor();

		return process;
	}

	protected static String[] buildCommand(final String command) {
		return CommandUtils.isWindows() ? new String[] { "cmd.exe", "/c", command }
				: new String[] { "sh", "-c", command };
	}

	protected static ProcessBuilder.Redirect getStdoutRedirect(boolean showStdout) {
		return showStdout ? ProcessBuilder.Redirect.INHERIT : ProcessBuilder.Redirect.PIPE;
	}

	protected void handleExitValue(int exitValue, final List<Long> validReturnCodes, final String errorMessage,
			final InputStream inputStream) throws Exception {
		Log.debug("Process exit value: " + exitValue);
		if (!validReturnCodes.contains(Integer.toUnsignedLong(exitValue))) {
			// input stream is a merge of standard output and standard error of the
			// sub-process
			showErrorIfAny(inputStream);
			Log.error(errorMessage);
			throw new Exception(errorMessage);
		}
	}

	protected void showErrorIfAny(final InputStream inputStream) throws Exception {
		if (inputStream != null) {
			final String input = IOUtils.toString(inputStream, "utf8");
			Log.error(StringUtils.strip(input, "\n"));
		}
	}

}
