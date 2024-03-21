/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.bestpractice.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.util.PropsValues;

import java.util.Collection;
import java.util.LinkedList;

import org.osgi.service.component.annotations.Component;

/**
 * untested implementation - just an idea for what can be checked. As I don't
 * have a GC File Store, the implementation is extremely simple.
 *
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class GoogleFileStoreConfigurationHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		LinkedList<HealthcheckItem> result = new LinkedList<>();

		if (PropsValues.DL_STORE_IMPL.equals(
				"com.liferay.portal.store.gcs.GCSStore")) {

			String key = PropsUtil.get("dl.store.gcs.aes256.key");

			// base64 encoded 256bit AES keys are 44 characters long
			// <i>could</i> test-decode to see if it's valid base64...

			if ((key == null) || (key.length() != 44)) {
				result.add(
					new HealthcheckItem(
						this, false, getClass().getName(), _LINK, _ERROR_MSG));
			}
		}

		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-bestpractice";
	}

	private static final String _ERROR_MSG = "";

	private static final String _LINK = "";

}