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

package org.forgerock.authz;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.EncoderConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.parsing.Parser;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

@Test(testName = "BasicHttpServletTestCases")
public class BasicHttpServletTestCases {

    @BeforeClass
    public void setUp() {
        RestAssured.port = Integer.parseInt(System.getProperty("AUTHZ_PORT"));
        RestAssured.baseURI = "http://" + System.getProperty("AUTHZ_HOST");
        RestAssured.basePath = System.getProperty("AUTHZ_URI");
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.config = RestAssuredConfig.newConfig()
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8"));
    }

    @Test
    public void usersAllowed() {

        given().
            expect().
                statusCode(200).
            when().
                get("/basic/servlet/users");
    }

    @Test
    public void rolesNotAllowed() {

        given().
            expect().
                statusCode(403).
                body("code", equalTo(403)).
                body("reason", equalTo("Forbidden")).
                body("message", equalTo("Not authorized for endpoint: roles")).
                body("detail", hasEntry("internalCode", 123)).
            when().
                get("/basic/servlet/roles");
    }
}
