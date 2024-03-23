/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck;

/**
 * @author Olaf Kock
 */
public class HealthcheckItem {

	public HealthcheckItem(
		boolean resolved, String sourceKey, String link, String messageKey,
		Object... messageParameters) {

		_resolved = resolved;
		_sourceKey = sourceKey;
		_link = link;
		_messageKey = messageKey;
		_messageParameters = messageParameters;
	}

	public HealthcheckItem(Object cause, Throwable throwable) {
		String className = cause.getClass(
		).getName();

		_resolved = false;
		_sourceKey = className + "-exception";
		_link = null;
		_messageKey = "an-exception-occurred-for-x-x-x";
		_messageParameters = new Object[] {
			className,
			throwable.getClass(
			).getName(),
			throwable.getMessage()
		};
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
	 * A message key that describes what this healthcheck checks for
	 *
	 * @return human readable message
	 */
	public String getMessageKey() {
		return _messageKey;
	}

	/**
	 * Parameters that are used to look up the human readable message,
	 * with the messageKey.
	 * @return an array of parameters to be combined with the localized message
	 */
	public Object[] getMessageParameters() {
		return _messageParameters;
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
	public String getSourceKey() {
		return _sourceKey + "-" + _resolved;
	}

	/**
	 * signals if the healthcheck result is healthy or not
	 *
	 * @return true if healthy
	 */
	public boolean isResolved() {
		return _resolved;
	}

	private final String _link;
	private final String _messageKey;
	private final Object[] _messageParameters;
	private final boolean _resolved;
	private final String _sourceKey;

}