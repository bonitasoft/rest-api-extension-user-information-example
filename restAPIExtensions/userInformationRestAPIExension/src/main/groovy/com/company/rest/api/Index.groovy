package com.company.rest.api;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.engine.identity.ContactData
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.identity.UserCriterion
import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController

import groovy.json.JsonBuilder

class Index implements RestApiController {

	private static final Logger LOGGER = LoggerFactory.getLogger(Index.class)

	@Override
	RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		// To retrieve query parameters use the request.getParameter(..) method.
		// Be careful, parameter values are always returned as String values

		// Retrieve p parameter
		def p = request.getParameter "p"
		if (p == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter p is missing"}""")
		}

		// Retrieve c parameter
		def c = request.getParameter "c"
		if (c == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter c is missing"}""")
		}


		// Convert parameters from string to int
		p = p as int
		c = c as int

		// Initialize the list to store users information
		def usersInformation = []

		// Get the list of user
		List<User> users = context.apiClient.identityAPI.getUsers(p*c, c, UserCriterion.FIRST_NAME_ASC)

		// Iterate over each user
		for (user in users) {
			// Get user extra information (including email address)
			ContactData contactData = context.apiClient.identityAPI.getUserContactData(user.id, false)

			// Create a map with current user first name, last name and email address
			def userInformation = [firstName: user.firstName, lastName: user.lastName, email: contactData.email]

			// Add current user information to the global list
			usersInformation << userInformation
		}

		// Prepare the result
		def result = [p: p, c: c, userInformation: usersInformation]

		int startIndex = p*c
		int endIndex = p*c + users.size() - 1

		// Send the result as a JSON representation
		return buildPagedResponse(responseBuilder, new JsonBuilder(result).toPrettyString(), startIndex, endIndex, context.apiClient.identityAPI.numberOfUsers)
	}

	/**
	 * Build an HTTP response.
	 *
	 * @param  responseBuilder the Rest API response builder
	 * @param  httpStatus the status of the response
	 * @param  body the response body
	 * @return a RestAPIResponse
	 */
	RestApiResponse buildResponse(RestApiResponseBuilder responseBuilder, int httpStatus, Serializable body) {
		return responseBuilder.with {
			withResponseStatus(httpStatus)
			withResponse(body)
			build()
		}
	}

	/**
	 * Returns a paged result like Bonita BPM REST APIs.
	 * Build a response with a content-range.
	 *
	 * @param  responseBuilder the Rest API response builder
	 * @param  body the response body
	 * @param  p the page index
	 * @param  c the number of result per page
	 * @param  total the total number of results
	 * @return a RestAPIResponse
	 */
	RestApiResponse buildPagedResponse(RestApiResponseBuilder responseBuilder, Serializable body, int p, int c, long total) {
		return responseBuilder.with {
			withContentRange(p,c,total)
			withResponse(body)
			build()
		}
	}

	/**
	 * Load a property file into a java.util.Properties
	 */
	Properties loadProperties(String fileName, ResourceProvider resourceProvider) {
		Properties props = new Properties()
		resourceProvider.getResourceAsStream(fileName).withStream { InputStream s ->
			props.load s
		}
		props
	}

}
