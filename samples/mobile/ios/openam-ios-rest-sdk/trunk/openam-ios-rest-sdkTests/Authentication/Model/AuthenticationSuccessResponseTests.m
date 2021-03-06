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
 * Copyright 2013 ForgeRock, AS.
 */

#import <XCTest/XCTest.h>
#import "AuthenticationSuccessResponse.h"
#import "JsonTestHelper.h"

@interface AuthenticationSuccessResponseTests : XCTestCase

@end

@implementation AuthenticationSuccessResponseTests

- (void)setUp
{
    [super setUp];
    // Put setup code here; it will be run once, before the first test case.
}

- (void)tearDown
{
    // Put teardown code here; it will be run once, after the last test case.
    [super tearDown];
}

- (void)testInitWithData {
    
    NSString *json = @"{\"tokenId\": \"TOKEN_ID\", \"successUrl\": \"SUCCESS_URL\"}";
    
    AuthenticationSuccessResponse *successResponse = [[AuthenticationSuccessResponse alloc] initWithData:[JsonTestHelper convertJsonStringToDictionary:json]];
    
    XCTAssertTrue([successResponse.tokenId isEqualToString:@"TOKEN_ID"]);
    XCTAssertTrue([successResponse.successUrl isEqualToString:@"SUCCESS_URL"]);
    XCTAssertTrue([successResponse.asData count] == 2);
}

@end
