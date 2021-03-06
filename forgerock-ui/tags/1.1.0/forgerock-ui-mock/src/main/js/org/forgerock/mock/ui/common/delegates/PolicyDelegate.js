/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2014 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

/*global $, define, _ */

/**
 * @author jdabrowski
 * @author Eugenia Sergueeva
 */

define("org/forgerock/mock/ui/common/delegates/PolicyDelegate", [
    "org/forgerock/commons/ui/common/util/Constants",
    "org/forgerock/commons/ui/common/main/AbstractDelegate"
], function (constants, AbstractDelegate) {
    var obj = new AbstractDelegate("/policy");

    obj.validateProperty = function (baseEntity, args, callback) {
        /* 
         * We are calling the validateObject action here instead of validateProperty
         * because we need to pass in entire object context in order to support policies
         * which may depend upon other properties.  From the response, we look to see if the 
         * particular property we are attempting to validate was included in the list of those
         * with errors.
         */
        obj.serviceCall({
            url: "/" + baseEntity + "?_action=validateObject",
            data: JSON.stringify(args.fullObject),
            type: "POST",
            success: function (data) {
                var haveWeFailed = false;
                if (data.failedPolicyRequirements) {
                    _.each(data.failedPolicyRequirements, function (failedReq) {
                        if (failedReq.property === args.property) {
                            haveWeFailed = true;
                            callback({
                                "result": false,
                                "failedPolicyRequirements": [failedReq]
                            });
                        }
                    });
                }

                if (!haveWeFailed) {
                    callback({ "result": true });
                }
            }
        });
    };

    return obj;
});



