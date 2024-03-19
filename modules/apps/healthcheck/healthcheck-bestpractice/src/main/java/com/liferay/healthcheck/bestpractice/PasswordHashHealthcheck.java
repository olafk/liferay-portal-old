/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.bestpractice;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.bestpractice.configuration.HealthcheckBestPracticeConfiguration;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Olaf Kock
 */
@Component(
	configurationPid = "com.liferay.healthcheck.bestpractice.configuration.HealthcheckBestPracticeConfiguration",
	service = Healthcheck.class
)
public class PasswordHashHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		String hashingAlgorithm = PropsUtil.get(
			"passwords.encryption.algorithm");

		if ((hashingAlgorithm != null) &&
			hashingAlgorithm.startsWith("PBKDF2WithHmacSHA1")) {

			int roundsPos = hashingAlgorithm.lastIndexOf('/');

			int rounds = GetterUtil.getInteger(
				hashingAlgorithm.substring(roundsPos + 1));

			Object[] info = {rounds, _owaspHashingRecommendation};

			return Arrays.asList(
				new HealthcheckItem(
					this, rounds >= _owaspHashingRecommendation,
					getClass().getName(), _LINK, _MSG, info));
		}

		return Collections.emptyList();
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-bestpractice";
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_owaspHashingRecommendation = ConfigurableUtil.createConfigurable(
			HealthcheckBestPracticeConfiguration.class, properties
		).owaspHashingRecommendation();
	}

	private static final String _LINK =
		"https://learn.liferay.com/reference/latest/en/dxp/propertiesdoc" +
			"/portal.properties.html#Passwords";

	private static final String _MSG =
		"healthcheck-password-hashing-rounds-owasp-recommendation";

	private volatile Long _owaspHashingRecommendation;

}