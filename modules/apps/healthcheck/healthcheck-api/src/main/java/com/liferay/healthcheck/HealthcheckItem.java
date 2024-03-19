/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Olaf Kock
 */
public class HealthcheckItem {

	public HealthcheckItem(
		Healthcheck healthcheck, boolean resolved, String source, String link,
		String message, Object... info) {

		_healthcheck = healthcheck;
		_resolved = resolved;
		_source = source;
		_link = link;
		_message = message;
		_info = info;
	}

	public HealthcheckItem(Healthcheck healthcheck, Throwable throwable) {
		String healthcheckClassName = healthcheck.getClass(
		).getName();

		_healthcheck = healthcheck;

		_resolved = false;
		_source = healthcheckClassName + "-exception";
		_link = null;
		_message = "an-exception-occurred-for-x-x-x";
		_info = new Object[] {
			healthcheckClassName,
			throwable.getClass(
			).getName(),
			throwable.getMessage()
		};
	}

	public String getCategory() {
		return _healthcheck.getCategory();
	}

	/**
	 * A human readable (localized) category for the kind of healthcheck that was
	 * executed
	 *
	 * @return this health check's category
	 */
	public String getCategory(Locale locale) {
		return _lookup(locale, _healthcheck.getCategory());
	}

	/**
	 * A machine readable key that can be used to refer to a particular healthcheck
	 * or its result. This was introduced to be able to ignore certain healthchecks,
	 * in case their test does not apply to a certain environment (example:
	 * Elasticsearch Sidecar is ok in local demo systems). Default content: The
	 * healthcheck's fully qualified classname, optionally extended by extra
	 * information (each healthcheck might execute several checks) and the
	 *
	 * @return the machine readable encoding for this healthcheck
	 */
	public String getKey() {
		return _source + "-" + _resolved;
	}

	/**
	 * A link(URL) that can contain further information on the tested condition
	 *
	 * @return a link URL
	 */
	public String getLink() {
		return _link;
	}

	/**
	 * An informative message on the tested condition, in the language that a
	 * healthcheck has been executed.
	 *
	 * @return human readable message
	 */
	public String getMessage() {
		return _message;
	}

	public String getMessage(Locale locale) {
		return _lookup(locale, _message, _info);
	}

	/**
	 * signals if the healthcheck result is healthy or not
	 *
	 * @return true if healthy
	 */
	public boolean isResolved() {
		return _resolved;
	}

	private String _lookup(Locale locale, String key, Object... parameters) {
		ResourceBundle bundle = ResourceBundleUtil.getBundle(
			locale,
			_healthcheck.getClass(
			).getClassLoader());

		String result = ResourceBundleUtil.getString(bundle, key, parameters);

		if (result == null) {
			bundle = ResourceBundleUtil.getBundle(
				locale, Healthcheck.class.getClassLoader());

			result = ResourceBundleUtil.getString(bundle, key, parameters);

			if (result == null) {
				result = LanguageUtil.format(locale, key, parameters);

				if (result == null) {
					result = key;
				}
			}
		}

		return result;
	}

	private final Healthcheck _healthcheck;
	private final Object[] _info;
	private final String _link;
	private final String _message;
	private final boolean _resolved;
	private final String _source;

}