package com.company.rest.api;

import javax.servlet.http.HttpServletRequest

import org.bonitasoft.engine.identity.ContactData
import org.bonitasoft.engine.identity.User
import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import com.bonitasoft.engine.api.APIClient
import com.bonitasoft.engine.api.IdentityAPI
import com.bonitasoft.web.extension.rest.RestAPIContext

import groovy.json.JsonSlurper
import spock.lang.Specification

/**
 * @see http://spockframework.github.io/spock/docs/1.0/index.html
 */
class IndexTest extends Specification {

	// Declare mocks here
	// Mocks are used to simulate external dependencies behavior
	def httpRequest = Mock(HttpServletRequest)
	def resourceProvider = Mock(ResourceProvider)
	def context = Mock(RestAPIContext)
	def apiClient = Mock(APIClient)
	def identityAPI = Mock(IdentityAPI)
	def april = Mock(User)
	def william = Mock(User)
	def walter = Mock(User)
	def contactData = Mock(ContactData)

	/**
	 * You can configure mocks before each tests in the setup method
	 */
	def setup(){
		context.apiClient >> apiClient
		apiClient.identityAPI >> identityAPI

		identityAPI.getUsers(0, 2, _) >> [april, william]
		identityAPI.getUsers(1, 2, _) >> [william, walter]
		identityAPI.getUsers(2, 2, _) >> [walter]

		april.firstName >> "April"
		april.lastName >> "Sanchez"
		william.firstName >> "William"
		william.lastName >> "Jobs"
		walter.firstName >> "Walter"
		walter.lastName >> "Bates"

		identityAPI.getUserContactData(*_) >> contactData
		contactData.email >> "test@email"
	}

	def should_return_a_json_representation_as_result() {
		given: "a RestAPIController"
		def index = new Index()
		// Simulate a request with a value for each parameter
		httpRequest.getParameter("p") >> "0"
		httpRequest.getParameter("c") >> "2"

		when: "Invoking the REST API"
		def apiResponse = index.doHandle(httpRequest, new RestApiResponseBuilder(), context)

		then: "A JSON representation is returned in response body"
		def jsonResponse = new JsonSlurper().parseText(apiResponse.response)
		// Validate returned response
		apiResponse.httpStatus == 200
		jsonResponse.p == 0
		jsonResponse.c == 2
		jsonResponse.userInformation.equals([
			[firstName:"April", lastName: "Sanchez", email: "test@email"],
			[firstName:"William", lastName: "Jobs", email: "test@email"]
		]);
	}

	def should_return_an_error_response_if_p_is_not_set() {
		given: "a request without p"
		def index = new Index()
		httpRequest.getParameter("p") >> null
		// Other parameters return a valid value
		httpRequest.getParameter("c") >> "aValue2"

		when: "Invoking the REST API"
		def apiResponse = index.doHandle(httpRequest, new RestApiResponseBuilder(), context)

		then: "A JSON response is returned with a HTTP Bad Request Status (400) and an error message in body"
		def jsonResponse = new JsonSlurper().parseText(apiResponse.response)
		// Validate returned response
		apiResponse.httpStatus == 400
		jsonResponse.error == "the parameter p is missing"
	}

	def should_return_an_error_response_if_c_is_not_set() {
		given: "a request without c"
		def index = new Index()
		httpRequest.getParameter("c") >> null
		// Other parameters return a valid value
		httpRequest.getParameter("p") >> "aValue1"

		when: "Invoking the REST API"
		def apiResponse = index.doHandle(httpRequest, new RestApiResponseBuilder(), context)

		then: "A JSON response is returned with a HTTP Bad Request Status (400) and an error message in body"
		def jsonResponse = new JsonSlurper().parseText(apiResponse.response)
		// Validate returned response
		apiResponse.httpStatus == 400
		jsonResponse.error == "the parameter c is missing"
	}

}