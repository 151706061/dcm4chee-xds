angular.module('xds.controllers').
controller('XdsBrowserCtrl', function($scope, $http, xdsConstants, xdsAdhocQuery, appLoadingIndicator, xdsConfig) {


        // force load
        $scope.xdsPatientIds = xdsConfig.patientIds;

        // set the function for checking loading status
        $scope.isLoading = appLoadingIndicator.isLoading;

        // current search string
        $scope.searchStr = ""; // 'john^^^&1.2.3.4.5&ISO';

        // adhoc query uuid
        $scope.qid = "";// xdsConstants.B_FindEntities[$scope.currentEntity];

        // currently browsed entity (for search by patient id)
        $scope.currentEntity = "";// XDS_FindDocuments";
        $scope.currentStatus = xdsConstants.STATUS_APPROVED;

        // these are overridden by the linkedbrowser
        $scope.delegates = {
            'showList' : function() {
                console.log('noop');
            },
            'clean' : function() {
                console.log('noop');
            }
        };

    });