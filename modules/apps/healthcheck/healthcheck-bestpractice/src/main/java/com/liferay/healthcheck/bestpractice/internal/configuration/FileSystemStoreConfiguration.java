/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.bestpractice.internal.configuration;

/**
 * As we don't have access to the original FileSystemStoreConfiguration class,
 * but are interested in its rootDir configuration: Here's the replacement we
 * need in order to get the value comfortably through ConfigurableUtil.
 *
 * @author Olaf Kock
 */
public interface FileSystemStoreConfiguration {

	public String rootDir();

}