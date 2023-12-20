/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.captcha.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.captcha.rest.client.dto.v1_0.Captcha;
import com.liferay.captcha.rest.client.http.HttpInvoker;
import com.liferay.captcha.rest.client.resource.v1_0.CaptchaResource;
import com.liferay.portal.kernel.encryptor.EncryptorUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Loc Pham
 */
@FeatureFlags("LPS-185150")
@RunWith(Arquillian.class)
public class CaptchaResourceTest extends BaseCaptchaResourceTestCase {

	@Override
	@Test
	public void testGetCaptchaChallenge() throws Exception {
		CaptchaResource.Builder builder = CaptchaResource.builder();

		CaptchaResource captchaResource = builder.build();

		Captcha captcha = captchaResource.getCaptchaChallenge();

		Assert.assertNotNull(captcha.getToken());

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
			EncryptorUtil.decrypt(testCompany.getKeyObj(), captcha.getToken()));

		Assert.assertNotNull(jsonObject.get("answer"));

		Assert.assertTrue(
			(GetterUtil.getLong(jsonObject.get("expiryTime")) - 1000L) >
				System.currentTimeMillis());

		String image = captcha.getImage();
		String prefix = "data:image/png;base64,";

		Assert.assertEquals(prefix, image.substring(0, prefix.length()));
		Assert.assertTrue(
			Base64.decode(image.substring(prefix.length())).length > 0);
	}

	@Override
	@Test
	public void testPostCaptchaResponse() throws Exception {
		String token = _getToken();

		CaptchaResource.Builder builder = CaptchaResource.builder();

		_assertStatus(
			token, RandomTestUtil.randomString(10), 400, builder.build());
	}

	@Test
	public void testStatelessCaptcha() throws Exception {
		String token = _getToken();

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
			EncryptorUtil.decrypt(testCompany.getKeyObj(), token));

		CaptchaResource.Builder builder = CaptchaResource.builder();

		_assertStatus(
			token, jsonObject.getString("answer"), 204, builder.build());

		_assertStatus(
			token, jsonObject.getString("answer"), 400, builder.build());
	}

	private void _assertStatus(
			String token, String answer, int statusCode,
			CaptchaResource captchaResource)
		throws Exception {

		Captcha captcha = new Captcha();

		captcha.setAnswer(answer);
		captcha.setToken(token);

		HttpInvoker.HttpResponse httpResponse =
			captchaResource.postCaptchaResponseHttpResponse(captcha);

		Assert.assertEquals(statusCode, httpResponse.getStatusCode());
	}

	private String _getToken() throws Exception {
		CaptchaResource.Builder builder = CaptchaResource.builder();

		CaptchaResource captchaResource = builder.build();

		Captcha captcha = captchaResource.getCaptchaChallenge();

		return captcha.getToken();
	}

	@Inject
	private CompanyLocalService _companyLocalService;

}