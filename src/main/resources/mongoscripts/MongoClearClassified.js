var conn = new Mongo();
var db   = conn.getDB(dbName);

var clearClassified = function() {
    db.tweet.find({ classified : { $exists : true }}).forEach(function(doc) {
        db.tweet.update(
            { _id     : doc._id }
            , { $unset : { classified : 1 }}
        );
    });
};

clearClassified();
