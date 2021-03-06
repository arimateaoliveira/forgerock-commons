/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2014 ForgeRock AS.
 */

package org.forgerock.authz.modules.oauth2;

import org.forgerock.json.fluent.JsonValue;
import org.mockito.ArgumentCaptor;
import org.restlet.representation.Representation;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class RestOAuth2AccessTokenValidatorTest {

    private RestOAuth2AccessTokenValidator accessTokenValidator;

    private RestResourceFactory restResourceFactory;
    private JsonParser jsonParser;

    private RestResource tokenInfoRequest;
    private RestResource userProfileRequest;

    @BeforeMethod
    public void setUp() {
        JsonValue config = JsonValue.json(JsonValue.object(
            JsonValue.field("token-info-endpoint", "TOKEN_INFO"),
            JsonValue.field("user-info-endpoint", "USER-PROFILE")
        ));
        restResourceFactory = mock(RestResourceFactory.class);
        jsonParser = mock(JsonParser.class);
        accessTokenValidator = new RestOAuth2AccessTokenValidator(config, restResourceFactory, jsonParser);

        tokenInfoRequest = mock(RestResource.class);
        userProfileRequest = mock(RestResource.class);
        given(restResourceFactory.resource(anyString()))
                .willReturn(tokenInfoRequest)
                .willReturn(userProfileRequest);
    }

    @Test
    public void shouldReturnInvalidAccessTokenResponse() throws OAuth2Exception, IOException {

        //Given
        String accessToken = "ACCESS_TOKEN";
        Representation response = mock(Representation.class);
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("error", "ERROR");
        JsonValue tokenInfoResponse = new JsonValue(jsonMap);

        given(tokenInfoRequest.get()).willReturn(response);
        given(response.getText()).willReturn("TOKEN_INFO");
        given(jsonParser.parse("TOKEN_INFO")).willReturn(tokenInfoResponse);

        //When
        final AccessTokenValidationResponse validate = accessTokenValidator.validate(accessToken);

        //Then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(restResourceFactory).resource(captor.capture());
        assertTrue(captor.getValue().contains("ACCESS_TOKEN"));
        assertFalse(validate.isTokenValid());
        assertTrue(validate.getProfileInformation().isEmpty());
        assertTrue(validate.getTokenScopes().isEmpty());
    }

    @Test
    public void shouldReturnInvalidAccessTokenResponseWithScope() throws OAuth2Exception, IOException {

        //Given
        String accessToken = "ACCESS_TOKEN";
        Representation response = mock(Representation.class);
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("expires_in", -1);
        jsonMap.put("scope", "A B C");
        JsonValue tokenInfoResponse = new JsonValue(jsonMap);

        given(tokenInfoRequest.get()).willReturn(response);
        given(response.getText()).willReturn("TOKEN_INFO");
        given(jsonParser.parse("TOKEN_INFO")).willReturn(tokenInfoResponse);

        //When
        final AccessTokenValidationResponse validate = accessTokenValidator.validate(accessToken);

        //Then
        assertFalse(validate.isTokenValid());
        assertTrue(validate.getProfileInformation().isEmpty());
        assertEquals(validate.getTokenScopes().size(), 3);
    }

    @Test
    public void shouldReturnValidAccessTokenResponseWithScopeAndUserProfile() throws OAuth2Exception, IOException {

        //Given
        String accessToken = "ACCESS_TOKEN";
        Representation accessTokenResponse = mock(Representation.class);
        Map<String, Object> tokenInfoMap = new HashMap<String, Object>();
        tokenInfoMap.put("expires_in", 1);
        tokenInfoMap.put("scope", "A B C");
        JsonValue tokenInfoResponse = new JsonValue(tokenInfoMap);

        given(tokenInfoRequest.get()).willReturn(accessTokenResponse);
        given(accessTokenResponse.getText()).willReturn("TOKEN_INFO");
        given(jsonParser.parse("TOKEN_INFO")).willReturn(tokenInfoResponse);

        Representation userProfileResponse = mock(Representation.class);
        Map<String, Object> userProfileMap = new HashMap<String, Object>();
        userProfileMap.put("KEY1", "VAL");
        userProfileMap.put("KEY2", "VAL");
        JsonValue profileResponse = new JsonValue(userProfileMap);

        given(userProfileRequest.get()).willReturn(userProfileResponse);
        given(userProfileResponse.getText()).willReturn("USER_PROFILE");
        given(jsonParser.parse("USER_PROFILE")).willReturn(profileResponse);

        //When
        final AccessTokenValidationResponse validate = accessTokenValidator.validate(accessToken);

        //Then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(userProfileRequest).addHeader(eq("Authorization"), captor.capture());
        assertTrue(captor.getValue().equals("Bearer ACCESS_TOKEN"));
        assertTrue(validate.isTokenValid());
        assertEquals(validate.getProfileInformation().size(), 2);
        assertEquals(validate.getTokenScopes().size(), 3);
    }

    /**
     * The userInfo endpoint configuration is optional. If it is not specified, then no attempt should be made to fetch
     * any user profile information.
     */
    @Test
    public void shouldNotFetchUserProfileIfEndPointNotConfigured() throws Exception {
        // Given
        JsonValue config = JsonValue.json(JsonValue.object(
                JsonValue.field("token-info-endpoint", "TOKEN_INFO")
                // No user info endpoint
        ));
        accessTokenValidator = new RestOAuth2AccessTokenValidator(config, restResourceFactory, jsonParser);
        String accessToken = "ACCESS_TOKEN";
        Representation accessTokenResponse = mock(Representation.class);
        Map<String, Object> tokenInfoMap = new HashMap<String, Object>();
        tokenInfoMap.put("expires_in", 1);
        tokenInfoMap.put("scope", "A B C");
        JsonValue tokenInfoResponse = new JsonValue(tokenInfoMap);

        given(tokenInfoRequest.get()).willReturn(accessTokenResponse);
        given(accessTokenResponse.getText()).willReturn("TOKEN_INFO");
        given(jsonParser.parse("TOKEN_INFO")).willReturn(tokenInfoResponse);


        // When
        AccessTokenValidationResponse response = accessTokenValidator.validate(accessToken);

        // Then
        verifyZeroInteractions(userProfileRequest);
        assertEquals(response.getProfileInformation(), Collections.emptyMap());
    }
}
