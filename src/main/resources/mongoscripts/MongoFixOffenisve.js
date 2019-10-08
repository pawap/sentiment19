//
// @author Paw
//
var conn = new Mongo();
var db   = conn.getDB(dbName);

var fixClassified = function() {
    db.tweet.find({ classified : { $exists : true }}).forEach(function(doc) {
        db.tweet.update(
            { _id     : doc._id }
            , { $set : { classified : doc.classified[0] } }
        );
    });
};

fixClassified();
