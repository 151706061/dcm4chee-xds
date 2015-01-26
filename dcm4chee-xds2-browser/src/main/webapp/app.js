'use strict';

/* App Module */


var dcm4cheBrowserApp = angular.module('dcm4cheBrowserApp', [
    'ngRoute',
    'ngSanitize',
    'ngAnimate',
    'mgcrea.ngStrap',

    'dcm4che.appCommon',
    'dcm4che.appCommon.customizations',

    'dcm4che.browserLinkedView',

    'dcm4che.xds.common',
    'dcm4che.xds.controllers',
    'dcm4che.xds.REST',

    'dcm4che.config.manager'

]);

dcm4cheBrowserApp.config(
    function ($routeProvider, customizations) {
        $routeProvider.
            when('/step/:stepNum', {
                templateUrl: 'xds-browser/xds-browser.html',
                controller: 'XdsBrowserCtrl'
            });

        if (customizations.customConfigIndexPage) {
            $routeProvider.when('/service-manager', {
                templateUrl: customizations.customConfigIndexPage
            });
        } else $routeProvider.when('/service-manager', {
            templateUrl: 'config-browser/service-manager.html',
            controller: 'ServiceManagerCtrl'
        });

        $routeProvider.otherwise({
            redirectTo: '/service-manager'
        });
    });

dcm4cheBrowserApp.controller('dcm4cheAppController', function ($scope, appConfiguration) {
    $scope.appConfiguration = appConfiguration;
});