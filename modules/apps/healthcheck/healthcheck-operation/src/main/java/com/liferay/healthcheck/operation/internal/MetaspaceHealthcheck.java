/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;

/**
 * DXP's default metaspace is 768M, and it might not run with less than this.
 * Make sure that sufficient Metaspace is configured.
 *
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class MetaspaceHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		for (MemoryPoolMXBean memoryMXBean :
				ManagementFactory.getMemoryPoolMXBeans()) {

			if (Objects.equals(memoryMXBean.getName(), "Metaspace")) {
				long maxMetaspace = memoryMXBean.getUsage(
				).getMax();

				return Arrays.asList(
					new HealthcheckItem(
						(maxMetaspace >= (768 * 1024 * 1024)) ||
						(maxMetaspace == -1),
						_LINK, _MSG, maxMetaspace));
			}
		}

		return Arrays.asList(new HealthcheckItem(false, _LINK, _MSG, "---"));
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	private static final String _LINK = null;

	private static final String _MSG =
		"max-metaspace-should-be-at-least-768M-and-currently-is-set-to-x";

}