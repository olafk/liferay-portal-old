<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>
<div class="container-fluid container-fluid-max-xl sheet" style="">
	<p>
		<b><liferay-ui:message key="healthcheck-web.caption" /></b>
	</p>

	<%
	List<HealthcheckItem> checks = (List<HealthcheckItem>)renderRequest.getAttribute("checks");
	int ignoredChecks = (int)renderRequest.getAttribute("ignoredChecks");
	Set<String> theIgnoredChecks = (Set<String>)renderRequest.getAttribute("the-ignored-checks");
	%>

	<div class="align-items-lg-start align-items-md-start align-items-sm-start align-items-start flex-lg-row flex-md-row flex-row flex-sm-row row">
		<div class="col col-12 col-lg-3 col-md-4 col-sm-12"></div>
		<div class="col col-6 col-lg-2 col-md-2 col-sm-4" style="text-align: center;">
			<div style="font-size: 48px;">
				<clay:icon
					symbol="exclamation-circle"
				/>

				<br />
				<%= (int)renderRequest.getAttribute("failedChecks") %>
			</div>

			<liferay-ui:message key="failed" />
		</div>

		<div class="col col-6 col-lg-2 col-md-2 col-sm-4" style="text-align: center;">
			<div style="font-size: 48px;">
				<clay:icon
					symbol="check-circle"
				/>

				<br />
				<%= (int)renderRequest.getAttribute("succeededChecks") %>
			</div>

			<liferay-ui:message key="succeeded" />
		</div>

		<div class="col col-6 col-lg-2 col-md-2 col-sm-4" style="text-align: center;">
			<div style="font-size: 48px;">
				<clay:icon
					symbol="folder"
				/>

				<br />
				<%= ignoredChecks %>
			</div>

			<liferay-ui:message key="ignored" />
		</div>

		<div class="col col-12 col-lg-3 col-md-4 col-sm-12"></div>
	</div>

	<table>

		<%
		for (HealthcheckItem check : checks) {
			String style = check.isResolved() ? "" : "font-weight:bold;";
			String symbol = check.isResolved() ? "check-circle" : "exclamation-circle";
		%>

		<tr style="border: 1px solid grey; <%= style %>">
			<td style="min-width: 3em; text-align: center;"><clay:icon
					symbol="<%= symbol %>" /></td>
			<td><%= check.getCategory(themeDisplay.getLocale()) %></td>
			<td style="overflow-wrap: break-word;"><%= check.getMessage(themeDisplay.getLocale()) %></td>
			<td style="padding: 2px; word-wrap: normal;">

				<%
				if (check.getLink() != null) {
					out.write("(<a href=\"" + check.getLink() + "\" target=\"_blank\">hint</a>)");
				}
				else {
					out.write("(no&nbsp;hint)");
				}
				%>

			</td>
			<td style="padding: 2px;"><aui:button-row>
					<portlet:actionURL name="ignoreMessage" var="ignoreAction">
						<portlet:param name="ignore" value="<%= check.getKey() %>" />
					</portlet:actionURL>

					<aui:button onClick="<%= ignoreAction %>" value="ignore" />
				</aui:button-row></td>
		</tr>

		<%
		}
		%>

	</table>

	<c:if test="<%= ignoredChecks > 0 %>">

	<div style="margin-top: 2rem;">
		<portlet:actionURL name="resetIgnore" var="resetIgnoreAction" />

		<aui:button onClick="<%= resetIgnoreAction %>" value="reset-ignore" />
	</div>
	<!--
Ignored <%= ignoredChecks %> healthcheck(s):

<%
for (String theCheck : theIgnoredChecks) {
	out.write(theCheck);
	out.write("\n");
}
%>-->

	</c:if>
</div>