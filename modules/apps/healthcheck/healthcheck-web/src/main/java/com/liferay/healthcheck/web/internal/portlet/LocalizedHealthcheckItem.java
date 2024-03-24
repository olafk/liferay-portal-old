/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.web.internal.portlet;

/**
 * @author Olaf Kock
 */
public class LocalizedHealthcheckItem {

	public LocalizedHealthcheckItem(
		boolean resolved, String category, String message, String link,
		String sourceKey) {

		_resolved = resolved;
		_category = category;
		_message = message;
		_link = link;
		_sourceKey = sourceKey;
	}

	public String getCategory() {
		return _category;
	}

	public String getLink() {
		return _link;
	}

	public String getMessage() {
		return _message;
	}

	public String getSourceKey() {
		return _sourceKey;
	}

	public boolean isResolved() {
		return _resolved;
	}

	private final String _category;
	private final String _link;
	private final String _message;
	private final boolean _resolved;
	private final String _sourceKey;

}