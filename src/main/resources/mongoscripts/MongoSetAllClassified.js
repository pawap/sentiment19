//
// @author Paw
//
var conn = new Mongo();
var db   = conn.getDB(dbName);

var clearClassified = function() {
    db.tweet.find({ }).forEach(function(doc) {
        db.tweet.update(
            { _id     : doc._id },
            { $set : { classified : new Date() }}
        );
    });
};

clearClassified();
