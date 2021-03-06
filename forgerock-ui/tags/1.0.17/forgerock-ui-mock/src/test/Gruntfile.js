/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 ForgeRock AS. All Rights Reserved
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

module.exports = function(grunt) {

    grunt.initConfig({
        watch: {
            sync_and_test: {
                files: [
                    '../../../forgerock-ui-commons/src/main/js/**',
                    '../../../forgerock-ui-commons/src/main/resources/**',
                    '../../../forgerock-ui-commons/src/test/qunit/**',
                    '../../../forgerock-ui-user/src/main/js/**',
                    '../../../forgerock-ui-user/src/main/resources/**',
                    '../../../forgerock-ui-user/src/test/qunit/**',
                    '../main/js/**',
                    '../main/resources/**',
                    '../test/qunit/**',
                    'resources/**',
                    'js/**'
                ],
                tasks: [ 'sync', 'qunit' ]
            }
        },
        sync: {
            commons: {
                files: [
                    {
                        cwd     : '../../../forgerock-ui-commons/src/main/js',
                        src     : ['**'], 
                        dest    : '../../target/www'
                    },
                    {
                        cwd     : '../../../forgerock-ui-commons/src/main/resources',
                        src     : ['**'], 
                        dest    : '../../target/www'
                    },
                    {
                        cwd     : '../../../forgerock-ui-commons/src/test/qunit',
                        src     : ['**'], 
                        dest    : '../../target/test/tests'
                    }
                ]
            },
            user: {
                files: [
                    {
                        cwd     : '../../../forgerock-ui-user/src/main/js',
                        src     : ['**'], 
                        dest    : '../../target/www'
                    },
                    {
                        cwd     : '../../../forgerock-ui-user/src/main/resources',
                        src     : ['**'], 
                        dest    : '../../target/www'
                    },
                    {
                        cwd     : '../../../forgerock-ui-user/src/test/qunit',
                        src     : ['**'], 
                        dest    : '../../target/test/tests'
                    }
                ]
            },
            mock: {
                files: [
                    {
                        cwd     : '../main/js',
                        src     : ['**'], 
                        dest    : '../../target/www'
                    },
                    {
                        cwd     : '../main/resources',
                        src     : ['**'], 
                        dest    : '../../target/www'
                    },
                    {
                        cwd     : 'resources',
                        src     : ['css/**', 'qunit.html'], 
                        dest    : '../../target/test'
                    },
                    {
                        cwd     : 'qunit',
                        src     : ['**'], 
                        dest    : '../../target/test/tests'
                    },
                    {
                        cwd     : 'js',
                        src     : ['**'], 
                        dest    : '../../target/test'
                    }
                ]
            }
        },
        qunit: {
            all: ['../../target/test/qunit.html']
        },
        notify_hooks: {
            options: {
                enabled: true,
                title: "ForgeRock UI QUnit Tests"
            }
        }
    });

    grunt.loadNpmTasks('grunt-contrib-qunit');

    grunt.loadNpmTasks('grunt-contrib-watch');
    
    grunt.loadNpmTasks('grunt-notify');

    grunt.loadNpmTasks('grunt-sync');

    grunt.task.run('notify_hooks');
    grunt.registerTask('default', ['watch']);

};
