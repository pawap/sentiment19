//
// @author Paw
//
var conn = new Mongo();
var db   = conn.getDB(dbName);

var setupDateFields = function() {
    db.tweet.find({ language : { $in : ['de','en'] }}).forEach(function(doc) {
        db.tweet.update(
            { _id     : doc._id }
            , { $set : {year : doc.crdate.getFullYear(),  month : doc.crdate.getMonth(), day: doc.crdate.getDate() }}
        );
    });
};

setupDateFields();
