/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.scim.rest.resource.handler;

import com.liferay.scim.rest.dto.v1_0.QueryAttributes;
import com.liferay.scim.rest.dto.v1_0.User;

import javax.ws.rs.core.Response;

/**
 * @author Stian Sigvartsen
 */
public interface UserResourceHandler {

	public Response deleteV2User(String id) throws Exception;

	public Object getV2User(Integer count, Integer startIndex) throws Exception;

	public Object getV2UserById(String id) throws Exception;

	public Response postV2User(User user) throws Exception;

	public Response postV2UserSearch(QueryAttributes queryAttributes)
		throws Exception;

	public Response putV2User(String id, User user) throws Exception;

}