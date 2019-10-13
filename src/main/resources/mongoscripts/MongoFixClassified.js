//
// @author Paw
//
var conn = new Mongo();
var db   = conn.getDB(dbName);

var fixClassified = function() {
    db.tweet.find({ offenisve : { $exists : true }}).forEach(function(doc) {
        db.tweet.update(
            { _id     : doc._id }
            , { $set : { offensive : doc.offenisve[0], classified : doc.classified[0] }, $unset: {offenisve: 1}}
        );
    });
};

fixClassified();
