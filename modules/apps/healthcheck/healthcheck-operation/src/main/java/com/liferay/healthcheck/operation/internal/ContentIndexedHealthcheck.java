/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.TermQuery;
import com.liferay.portal.kernel.search.generic.TermQueryImpl;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.CountSearchRequest;
import com.liferay.portal.search.engine.adapter.search.CountSearchResponse;
import com.liferay.portal.search.index.IndexNameBuilder;

import java.util.Arrays;
import java.util.Collection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * A very basic check for an existing full-text-index (elasticsearch). This
 * check only checks the number of users retrieved through database and through
 * index to be identical as a smoke test.
 *
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class ContentIndexedHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		CountSearchRequest countSearchRequest = new CountSearchRequest();

		countSearchRequest.setIndexNames(
			_indexNameBuilder.getIndexName(companyId));

		TermQuery termQuery = new TermQueryImpl(
			Field.ENTRY_CLASS_NAME, User.class.getName());

		countSearchRequest.setQuery(termQuery);

		CountSearchResponse countSearchResponse = _searchEngineAdapter.execute(
			countSearchRequest);

		long indexCount = countSearchResponse.getCount();

		long dbCount = _userLocalService.getCompanyUsersCount(companyId);

		boolean exists = false;

		if (indexCount >= dbCount) {
			exists = true;
		}

		return Arrays.asList(
			new HealthcheckItem(
				this, exists, getClass().getName(), _LINK,
				exists ? _MSG : _ERROR_MSG, dbCount, indexCount));
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	private static final String _ERROR_MSG =
		"healthcheck-content-indexed-error";

	private static final String _LINK = new StringBundler(
	).append(
		"/group/control_panel/manage?p_p_id=&"
	).append(
		ContentIndexedHealthcheck._SEARCH_ADMIN_PORTLET
	).append(
		"&_"
	).append(
		ContentIndexedHealthcheck._SEARCH_ADMIN_PORTLET
	).append(
		"_tabs1=index-actions"
	).toString();

	private static final String _MSG = "healthcheck-content-indexed";

	private static final String _SEARCH_ADMIN_PORTLET =
		"com_liferay_portal_search_admin_web_portlet_SearchAdminPortlet";

	@Reference
	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

	@Reference
	private UserLocalService _userLocalService;

}