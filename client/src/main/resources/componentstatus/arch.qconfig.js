function fun($scope, serviceInfo, $stateParams) {
    serviceInfo.getComponentInfo($stateParams.id).then(function (data) {
        var data = JSON.parse(data.viData);
        $scope.data = data;

        if (data.code == 200) {
            $scope.fileData = "请选择文件";
        }

        $scope.show = function (fileData) {
            $scope.fileData = fileData;
        }
    });
}
