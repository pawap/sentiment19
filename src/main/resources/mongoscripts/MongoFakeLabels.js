var conn = new Mongo();
var db   = conn.getDB('testdb');

var fakeLabels = function() {
    db.tweet.find({ crdate : { $exists : true }}).forEach(function(doc) {
        db.tweet.update(
            { _id     : doc._id }
            , { $set : { offensive : (Math.random() * 3 < 1)}}
        );
    });
};

fakeLabels();