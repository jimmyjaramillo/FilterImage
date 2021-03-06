var exec = require('cordova/exec');

exports.coolMethod = function(args, success, error) {
    var params = {
      Radius: args.radius ? args.radius: null,
      Latitude: args.latitude ? args.latitude: null,
      Longitude: args.longitude ? args.longitude: null,
      DataTimeStart: args.dataTimeStart ? args.dataTimeStart: null,
      DataTimeFinish: args.dataTimeFinish ? args.dataTimeFinish: null
    };
    return exec(success, error, "FilterImage", "coolMethod", [params]);
};
