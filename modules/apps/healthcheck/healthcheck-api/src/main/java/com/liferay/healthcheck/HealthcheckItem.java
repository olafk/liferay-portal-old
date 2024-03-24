/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck;

import com.liferay.petra.string.StringUtil;
import com.liferay.portal.kernel.util.ArrayUtil;

/**
 * @author Olaf Kock
 */
public class HealthcheckItem {

	public HealthcheckItem(
		boolean resolved, String link, String messageKey,
		Object... messageParameters) {

		_resolved = resolved;
		_link = link;
		_messageKey = messageKey;
		_messageParameters = messageParameters;
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
	 * healthcheck's message localization key and all parameters, concatenated in
	 * a way to be usable as:
	 *
	 * @return the machine readable encoding for this healthcheck
	 */
	public String getSourceKey() {
		Object[] keys = new Object[_messageParameters.length + 1];

		ArrayUtil.combine(new Object[] {_messageKey}, _messageParameters, keys);

		return StringUtil.merge(ArrayUtil.toStringArray(keys), "-");
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

}