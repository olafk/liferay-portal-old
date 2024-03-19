/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.bestpractice;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.util.PropsValues;

import java.text.DateFormat;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.osgi.service.component.annotations.Component;

/**
 * Some installations simplified the locales to just two letters for the
 * language. In some cases this lead to follow-up problems, as none of the
 * OOTB languages omits a country/locale.
 *
 * This check makes sure that available languages have at least 5 characters
 * (e.g. "en_GB"), and are supported by the underlying JVM
 *
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class AvailableLocaleConfigurationHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		LinkedList<HealthcheckItem> result = new LinkedList<>();

		_validateProperties(result, PropsValues.LOCALES, "locales");
		_validateProperties(
			result, PropsValues.LOCALES_ENABLED, "locales.enabled");
		_validateProperties(result, PropsValues.LOCALES_BETA, "locales.beta");

		if (result.isEmpty()) {
			Object[] info = {};

			result.add(
				new HealthcheckItem(
					this, true, getClass().getName(), _LINK, _MSG, info));
		}

		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-bestpractice";
	}

	private void _validateProperties(
		LinkedList<HealthcheckItem> result, String[] locales,
		String collectionName) {

		for (String loc : locales) {

			// some JVM naming seems to be different from ours...

			String alternativeLoc = StringUtil.replace(loc, "latin", "#Latn");

			if (loc.length() < 5) {
				Object[] info = {collectionName, loc};

				result.add(
					new HealthcheckItem(
						this, false, getClass().getName(), _LINK,
						_ERROR_MSG_LENGTH, info));
			}

			if (!(_availableLocales.contains(loc) ||
				  _availableLocales.contains(alternativeLoc))) {

				Object[] info = {collectionName, loc};

				result.add(
					new HealthcheckItem(
						this, false, getClass().getName(), _LINK,
						_ERROR_MSG_DIFF, info));
			}
		}
	}

	private static final String _ERROR_MSG_DIFF =
		"healthcheck-locale-properties-diff";

	private static final String _ERROR_MSG_LENGTH =
		"healthcheck-locale-properties-length";

	private static final String _LINK =
		"https://docs.liferay.com/portal/7.4-latest/propertiesdoc/portal." +
			"properties.html#Languages%20and%20Time%20Zones";

	private static final String _MSG = "healthcheck-locale-properties";

	private static final Set<String> _availableLocales = new TreeSet<>();

	{
		for (Locale locale : DateFormat.getAvailableLocales()) {
			_availableLocales.add(locale.toString());
		}
	}

}