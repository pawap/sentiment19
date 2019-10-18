//
// @author Paw
//
var conn = new Mongo();
var db   = conn.getDB(dbName);

var i = 1;
var incrementDates = function() {
    db.tweet.find({ crdate : { $exists : true }}).forEach(function(doc) {
        db.tweet.update(
            { _id     : doc._id },
            { $set : { crdate : new Date(doc.crdate.getTime() + (86400000 * (i/20))) }}
        );
        i = Math.floor(i + (Math.random() * 2));
    });
};

incrementDates();
