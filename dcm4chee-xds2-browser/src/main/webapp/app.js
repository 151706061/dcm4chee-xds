'use strict';

/* App Module */


var dcm4cheBrowserApp = angular.module('dcm4cheBrowserApp', [
  'ngRoute',
  'ngSanitize',
  'ngAnimate',
  'mgcrea.ngStrap',

  'dcm4che.appCommon',
  'dcm4che.browserLinkedView',
  
  'dcm4che.xds.common',
  'dcm4che.xds.controllers',
  'dcm4che.xds.REST',

  'dcm4che.configurationManager'

]);

dcm4cheBrowserApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
    when('/step/:stepNum', {
        templateUrl: 'xds-browser/xds-browser.html',
        controller: 'XdsBrowserCtrl'
      }).
      when('/service-manager', {
          templateUrl: 'config-browser/service-manager.html',
          controller: 'ServiceManagerCtrl'
      }).
      otherwise({
        redirectTo: '/service-manager'
      });
  }]);