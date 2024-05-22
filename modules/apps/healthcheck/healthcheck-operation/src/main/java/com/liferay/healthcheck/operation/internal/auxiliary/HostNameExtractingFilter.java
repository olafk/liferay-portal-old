/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal.auxiliary;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.BaseFilter;
import com.liferay.portal.kernel.util.PortalUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Keep track of all host names that were ever requested, including the scheme
 * (http/https). These hosts will be used to check for allowed redirection of
 * any host name used to access any content in this whole instance.
 *
 * This filter is (un)registered manually, because its operation depends on
 * configuration, so that it does not have an impact on request processing time
 * unless explicitly enabled.
 *
 * @see HostnameDetectorImpl
 * @author Olaf Kock
 */
public class HostNameExtractingFilter extends BaseFilter {

	public HostNameExtractingFilter(Counter counter) {
		_counter = counter;
	}

	public Set<String> getAccessedUrls(long companyId) {
		return Collections.unmodifiableSet(_getRequestedBaseUrls(companyId));
	}

	@Override
	protected Log getLog() {
		return _log;
	}

	@Override
	protected void processFilter(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, FilterChain filterChain)
		throws Exception {

		String host = httpServletRequest.getServerName();

		if ((host != null) && (host.length() > 1)) {
			_register(
				PortalUtil.getCompanyId(httpServletRequest),
				httpServletRequest.getScheme() + "://" + host);
		}

		super.processFilter(
			httpServletRequest, httpServletResponse, filterChain);

		_counter.tick();
	}

	private HashSet<String> _getRequestedBaseUrls(long companyId) {
		HashSet<String> urls = _requestedBaseUrls.get(companyId);

		if (urls == null) {
			urls = new HashSet<>();

			_requestedBaseUrls.put(companyId, urls);
		}

		return urls;
	}

	private void _register(long companyId, String baseUrl) {
		if (_log.isInfoEnabled()) {
			HashSet<String> set = _getRequestedBaseUrls(companyId);

			if (!set.contains(baseUrl)) {
				set.add(baseUrl);
				_log.info(
					StringBundler.concat(
						"new URL: ", baseUrl, " in company ", companyId));
			}
		}
		else {
			_getRequestedBaseUrls(
				companyId
			).add(
				baseUrl
			);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		HostNameExtractingFilter.class);

	private final Counter _counter;
	private final HashMap<Long, HashSet<String>> _requestedBaseUrls =
		new HashMap<>();

}