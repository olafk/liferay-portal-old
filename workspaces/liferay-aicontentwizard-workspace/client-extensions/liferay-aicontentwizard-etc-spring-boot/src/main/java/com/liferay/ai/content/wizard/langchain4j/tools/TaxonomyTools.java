/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.content.wizard.langchain4j.tools;

import com.liferay.ai.content.wizard.langchain4j.descriptions.TaxonomyCategoryDescriptions;
import com.liferay.ai.content.wizard.langchain4j.descriptions.TaxonomyVocabularyDescriptions;
import com.liferay.headless.admin.taxonomy.client.dto.v1_0.TaxonomyCategory;
import com.liferay.headless.admin.taxonomy.client.dto.v1_0.TaxonomyVocabulary;
import com.liferay.headless.admin.taxonomy.client.resource.v1_0.TaxonomyCategoryResource;
import com.liferay.headless.admin.taxonomy.client.resource.v1_0.TaxonomyVocabularyResource;

import dev.langchain4j.agent.tool.Tool;

import org.json.JSONObject;

import org.springframework.http.HttpHeaders;

/**
 * @author Keven Leone
 * @author Brian Wing Shun Chan
 */
public class TaxonomyTools extends BaseTools {

	public TaxonomyTools(ToolsContext toolsContext) {
		super(toolsContext);

		_taxonomyCategoryResource = TaxonomyCategoryResource.builder(
		).endpoint(
			toolsContext.liferayDXPURL
		).header(
			HttpHeaders.AUTHORIZATION, toolsContext.authorization
		).build();
		_taxonomyVocabularyResource = TaxonomyVocabularyResource.builder(
		).endpoint(
			toolsContext.liferayDXPURL
		).header(
			HttpHeaders.AUTHORIZATION, toolsContext.authorization
		).build();
	}

	@Tool("Create vocabulary")
	public void postSiteTaxonomyVocabulary(
			TaxonomyVocabularyDescriptions taxonomyVocabularyDescriptions)
		throws Exception {

		TaxonomyVocabulary taxonomyVocabulary =
			_taxonomyVocabularyResource.postSiteTaxonomyVocabulary(
				toolsContext.siteId,
				TaxonomyVocabulary.toDTO(
					new JSONObject(
					).put(
						"name", taxonomyVocabularyDescriptions.name
					).put(
						"name_i18n", taxonomyVocabularyDescriptions.name_i18n
					).toString()));

		_postTaxonomyVocabularyTaxonomyCategory(
			taxonomyVocabulary, taxonomyVocabularyDescriptions);
	}

	private void _postTaxonomyCategoryTaxonomyCategory(
			TaxonomyCategoryDescriptions[]
				childTaxonomyCategoryDescriptionsArray,
			String parentTaxonomyCategoryId)
		throws Exception {

		for (TaxonomyCategoryDescriptions childTaxonomyCategoryDescriptions :
				childTaxonomyCategoryDescriptionsArray) {

			TaxonomyCategory childTaxonomyCategory =
				_taxonomyCategoryResource.postTaxonomyCategoryTaxonomyCategory(
					parentTaxonomyCategoryId,
					_toDTO(childTaxonomyCategoryDescriptions));

			_postTaxonomyCategoryTaxonomyCategory(
				childTaxonomyCategoryDescriptions.
					childTaxonomyCategoryDescriptionsArray,
				childTaxonomyCategory.getId());
		}
	}

	private void _postTaxonomyVocabularyTaxonomyCategory(
			TaxonomyVocabulary taxonomyVocabulary,
			TaxonomyVocabularyDescriptions taxonomyVocabularyDescriptions)
		throws Exception {

		for (TaxonomyCategoryDescriptions taxonomyCategoryDescriptions :
				taxonomyVocabularyDescriptions.
					taxonomyCategoryDescriptionsArray) {

			TaxonomyCategory taxonomyCategory =
				_taxonomyCategoryResource.
					postTaxonomyVocabularyTaxonomyCategory(
						taxonomyVocabulary.getId(),
						_toDTO(taxonomyCategoryDescriptions));

			_postTaxonomyCategoryTaxonomyCategory(
				taxonomyCategoryDescriptions.
					childTaxonomyCategoryDescriptionsArray,
				taxonomyCategory.getId());
		}
	}

	private TaxonomyCategory _toDTO(
		TaxonomyCategoryDescriptions taxonomyCategoryDescriptions) {

		return TaxonomyCategory.toDTO(
			new JSONObject(
			).put(
				"name", taxonomyCategoryDescriptions.name
			).put(
				"name_i18n", taxonomyCategoryDescriptions.name_i18n
			).toString());
	}

	private final TaxonomyCategoryResource _taxonomyCategoryResource;
	private final TaxonomyVocabularyResource _taxonomyVocabularyResource;

}