window.addEventListener('load', function(){
    //Vue.js element
    const vue = new Vue({
        el: "#app",
        data: function(){
            return{
                //counter number of offensive tweets
                offensive: 0,
                //counter number of offensive tweets
                nonOffensive: 0,
                //radio buttons time selection values
                times: [
                    {text: "Letzter Tag", value: "day", start: getDate(-1), end: getDate(0)},
                    {text: "Letzte Woche", value: "week", start: getDate(-7), end: getDate(0)},
                    {text: "Letzter Monat", value: "month", start: getDate(-30), end: getDate(0)},
                    {text: "Gesamter Zeitraum", value: "range", start: null, end: getDate(0)},
                ],

                // most popular tweets in whole db
                popularHashtags: [],

                // most popular tweets for chosen filters
                topHashtags: [],

                // -------- FILTERS -----------

                //datepicker selection, initialized with one week
                selectedDate: {
                    start:  getDate(-7),
                    end:  getDate(0)
                },
                //hashtags empty init
                hashtags: [],

                // TODO language-Filter


            }
        },
        methods:{

            addHashtag: function () {
                let tag =  $('#newhashtag').val();
                if (tag.substr(0,1) !== "#") tag = '#' + tag;
                this.hashtags.push({value: tag});
                $('#newhashtag').val('');
                },

            updateHashtags: function () {
                axios.post('/sentiment19/popularhashtags?limit=8',{})
                    .then(response => this.popularHashtags = response.data.hashtags.map(function (tag, index) {
                        return {value: tag.hashtag, key: index, popular: 1, hidden: 0, count: tag.count,
                            percent: (tag.count / response.data.total * 100).toFixed(2) + '%'
                        };
                    }))

                this.popularHashtags.forEach(function (e) {
                    if (this.hashtags.filter(tag => (tag.value === e.value)).length > 0) {
                        e.hidden = 1;
                    }
                })

            },
            /**
             * Updates the main offensive and nonOffensive tweet amount counters (and initiates Pie Chart update)
             */
            updateCounters: function(){

                axios.all([
                    axios.post('/sentiment19/stats',{offensive: 1}),
                    axios.post('/sentiment19/stats',{offensive: 0})
                  ])
                  .then(axios.spread((off, nonOff) => {
                    this.offensive = off.data.count
                    this.nonOffensive = nonOff.data.count
                    this.updatePieChart()
                  }));

            },
            /**
             * Updates bar chart, based on selected filter
             */
            updateBarChart: function(){

                axios.post('/sentiment19/popularhashtags?limit=5',this.getCurrentFilter(0))
                    .then(response => {
                        barChartNonOff.data.labels = response.data.hashtags.map(function (tag, index) {
                            return tag.hashtag;
                        })
                        barChartNonOff.data.datasets[0].data = response.data.hashtags.map(function (tag, index) {
                            return tag.count;
                        })
                        barChartNonOff.data.datasets[0].backgroundColor = response.data.hashtags.map(function (tag, index) {
                            return 'rgb(108,117,125)';
                        })
                        barChartNonOff.update()

                    })
                axios.post('/sentiment19/popularhashtags?limit=5',this.getCurrentFilter(1))
                    .then(response => {
                        barChartOff.data.labels = response.data.hashtags.map(function (tag, index) {
                            return tag.hashtag;
                        })
                        barChartOff.data.datasets[0].data = response.data.hashtags.map(function (tag, index) {
                            return tag.count;
                        })
                        barChartOff.data.datasets[0].backgroundColor = response.data.hashtags.map(function (tag, index) {
                            return 'rgb(108,117,125)';
                        })
                        barChartOff.update()

                    })
            },
            /**
             * Updates pie chart, based on selected time frame
             */
            updatePieChart: function(){

                axios.all([
                    axios.post('/sentiment19/stats',
                        this.getCurrentFilter(1)),
                    axios.post('/sentiment19/stats',
                        this.getCurrentFilter(0))
                  ])
                  .then(axios.spread((off, nonOff) => {
                    data =  [off.data.count, nonOff.data.count]
                    pieChart.data.datasets[0].data = data
                    pieChart.update()
                }));

            },
            updateTweets: function(){

                axios.all([
                    axios.post('/sentiment19/tweet',
                        this.getCurrentFilter(1)),
                    axios.post('/sentiment19/tweet',
                        this.getCurrentFilter(0))
                ])
                    .then(axios.spread((offTweet, nonOffTweet) => {
                        document.getElementById("offTweet").innerHTML = offTweet.data.html
                        document.getElementById("nonOffTweet").innerHTML = nonOffTweet.data.html
                        twttr.widgets.load()
                    }));
            },

            //Update the line chart labels based on the selected start-/enddate
            //To-Do: Change the dataset aswell -> has to be requested from backend
            updateLineChart: function(){
                axios.all([
                    axios.post('/sentiment19/timeline', this.getCurrentFilter(1)),
                    axios.post('/sentiment19/timeline', this.getCurrentFilter(0))
                ])
                    .then(axios.spread((off, nonOff) => {
                        console.log(off.data.start);
                        dateRange = getRangeOfDates(off.data.start, new Date(off.data.end));
                        lineChart.data.labels = dateRange
                        lineChart.data.datasets[0].data = off.data.timeline
                        lineChart.data.datasets[1].data = nonOff.data.timeline
                        lineChart.update()
                    }));

            },
            getCurrentFilter: function(offensive){
                return {
                    offensive: offensive,
                    start: this.selectedDate.start,
                    end: this.selectedDate.end,
                    hashtags: this.hashtags.map(function (o) {
                        return o.value
                    })
                }
            }
        },
        updated: function () {
            $(function(){
                $('[data-toggle="tooltip"]').tooltip({ trigger: "hover", html:true});
                $('.tooltip').remove();
            });
            this.updatePieChart();
            this.updateLineChart();
            this.updateTweets();
            this.updateBarChart();
        }
    });

    //Used Chart.js as it seemed easier (compared to D3.js) to quickly implement the graphs we need (atleast for now)

    //Pie chart for direct comparison off vs. nonOff tweet amount
    var ctx2 = document.getElementById('barChartNonOff').getContext('2d');

    var barChartNonOff = new Chart(ctx2, {
        // type of chart
        type: 'bar',
        data: {
            labels: [],
            datasets: [{
                backgroundColor: [],
                borderColor: 'rgb(255,255,255)',
                fill: false,
                data: []
            }
            ]
        },

        //Data and style options for the bar chart
        options: {
            legend: { display: false },
            title: {
                display: true,
                text: 'Top nonoff-Hashtags for chosen timeframe'
            },
            scales: {
                yAxes: [{
                  ticks: {
                      precision:0,
                      beginAtZero: true //y-Axis starts at 0

                  }
                }],
                xAxes: [{
                    position: 'top',
                    ticks: {
                        callback: function(value) {
                            var padding = value.length > 5 ? '...':'';
                            return value.substr(0, 5) + padding;//truncate
                        },
                        maxRotation: 90,
                        minRotation: 90}
                }]
            },
            tooltips: {
                enabled: true,
                mode: 'label',
                callbacks: {
                    title: function(tooltipItems, data) {
                        var idx = tooltipItems[0].index;
                        return data.labels[idx];//do something with title
                    },
                    label: function(tooltipItems, data) {
                        var idx = tooltipItems.index;
                        return data.datasets[0].data[idx];
                    }
                }
            },
        }

    });
    var ctx2 = document.getElementById('barChartOff').getContext('2d');
    var barChartOff = new Chart(ctx2, {
        // type of chart
        type: 'bar',
        data: {
            labels: [],
            datasets: [{
                backgroundColor: [],
                borderColor: 'rgb(255,255,255)',
                fill: false,
                data: []
            }
            ]
        },

        //Data and style options for the bar chart
        options: {
            legend: { display: false },
            title: {
                display: true,
                text: 'Top off-Hashtags for chosen timeframe',
                position: 'bottom'
            },
            scales: {
                yAxes: [{
                    ticks: {
                        reverse: true,
                        precision: 0,
                        beginAtZero: true //y-Axis starts at 0

                    }
                }],

                xAxes: [{
                    ticks: {
                        callback: function(value) {
                            var padding = value.length > 5 ? '...':'';
                            return value.substr(0, 5) + padding;//truncate
                        },

                    maxRotation: 90,
                    minRotation: 90}

                }]

            },
            tooltips: {
                enabled: true,
                mode: 'label',
                callbacks: {
                    title: function(tooltipItems, data) {
                        var idx = tooltipItems[0].index;
                        return data.labels[idx];//do something with title
                    },
                    label: function(tooltipItems, data) {
                        var idx = tooltipItems.index;
                        return data.datasets[0].data[idx];
                    }
                }
            },
        }

    });

    //Pie chart for direct comparison off vs. nonOff tweet amount
    var ctx2 = document.getElementById('pieChart').getContext('2d');

    var pieChart = new Chart(ctx2, {
        // type of chart
        type: 'pie',

        //Data and style options for the line chart
        data: {
            labels: ['offensive', 'non-offensive'],
            datasets: [{
                backgroundColor: ['rgb(255, 99, 132)','rgb(0,255,0)'],
                borderColor: 'rgb(255,255,255)',
                fill: false,
                data: [vue.offensive, vue.nonOffensive]
            },
        ]
        },

        // Configuration options go here
        options: {
            rotation: 0.5 * Math.PI
        }
    });

    //Line chart - comparing offensive/nonOffensive over specified time
    var ctx2 = document.getElementById('lineChart').getContext('2d');
    var lineChart = new Chart(ctx2, {
        // type of chart
        type: 'line',

        //Data and style options for the line chart
        data: {
            labels: [''],
            datasets: [{
                label: 'offensive',
                backgroundColor: 'rgb(255, 99, 132)',
                borderColor: 'rgb(255, 99, 132)',
                fill: false,
                data: [1,2,3,4]
                },
                {
                label: 'non-offensive',
                backgroundColor: 'rgb(0, 255, 0)',
                borderColor: 'rgb(0, 255, 0)',
                fill: false,
                data: [5,6,7,8]
                },
            ]
        },

        // Configuration options go here
        options: {
            elements: {
                line: {
                    tension: 0 // disables bezier curves
                }
            },
            scales: {
                yAxes: [{
                    ticks:{
                        beginAtZero: true //y-Axis starts at 0
                    }
                }]
            }
        }
    });

    /**
     * Initialise Tweet display, Tweet amount counters and the pie and line charts
     */
    function init(){
        vue.updateTweets()
        vue.updateCounters()
        vue.updatePieChart()
        vue.updateLineChart()
        vue.updateHashtags()
        vue.updateBarChart()

    $(document).on('load', function () {
        $(function(){
            $('[data-toggle="tooltip"]').tooltip();
        })
    })
    }

   //run init function
    init()

});

/**
 * Formats a given date, to be displayed better as a line chart label
 * @param {*} date date to be formated
 * @returns formatted date
 */
function formatDate(date){
    return date.getDate() + "." + (date.getMonth()+1)  + "." + date.getFullYear().toString().slice(-2)
}

/**
 * Returns range of dates, between specified start and end date, with a given step length (key)
 * @param {*} start Start date for the date range
 * @param {*} end End date for the date range
 * @returns array containing(Already formmatted) date range
 */
function getRangeOfDates(start, end) {
    var dateArr = [];
    var dateCounter = new Date(start);
    while(dateCounter <= end){
        dateArr.push(moment(dateCounter).format('DD.MM.YYYY'));
        var newDate = dateCounter.setDate(dateCounter.getDate() + 1);
        dateCounter = new Date(newDate);
    }
    return dateArr;

}



/**
 *Get today's date plus or minus a specified number of days (0 for today)
 *
 * @param {*} d  number of days to be added/subtracted of today
 * @returns today +/- specified number of days
 */
function getDate(d){
    if(d > 0){
        return moment().add(d,'days').toDate()
    }else if(d < 0){
        return moment().subtract(Math.abs(d), 'days').toDate()
    }else if(d === 0){
        return moment().toDate()
    }
}


/**
 * Function requesting an async url.
 *
 * @param theUrl of the requested Object
 */
function httpGetAsync(theUrl, callback)
{
    // create the requested object
    var xmlHttp = new XMLHttpRequest();

    // set the state change callback to capture when the response comes in
    xmlHttp.onreadystatechange = function()
    {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
        {
            callback(xmlHttp.responseText);
        }
    }

    // open as a GET call, pass in the url and set async = True
    xmlHttp.open("GET", theUrl, true);

    // call send with no params as they were passed in on the url string
    xmlHttp.send(null);

    return;
}

/**
* Callbackfunction for the search of a random, offensive Object (GIFs in this case).
*/
function tenorCallback_randomsearch_off(responsetext)
{
    // parse the json response
    var response_objects = JSON.parse(responsetext);

    top_10_gifs = response_objects["results"];

    // load the GIFs in the tinygif-size.
    document.getElementById("Off_gif").src = top_10_gifs[0]["media"][0]["tinygif"]["url"];

    return;

}

/**
* Callbackfunction for the search of a random, non-offensive Object (GIFs in this case).
*/
function tenorCallback_randomsearch_non_off(responsetext)
{
    // parse the json response
    var response_objects = JSON.parse(responsetext);

    top_10_gifs = response_objects["results"];

    // load the GIFs in the tinygif-size.
    document.getElementById("Non_Off_gif").src = top_10_gifs[0]["media"][0]["tinygif"]["url"];

    return;

}

/**
 * Function enabling to request random offensive GIFs.
 * (The searchterm "angry" is used to get offensive GIFs.)
 */
function grab_data_off()
{

    // Using the Tenor-API-Key and the searchterm "angry" to get offensive GIFs.
    var search_url = "https://api.tenor.com/v1/random?q=angry&key=SXCYAWE2GDPA&limit=8";

    httpGetAsync(search_url,tenorCallback_randomsearch_off);

    // data will be loaded by each call's callback
    return;
}

/**
 * Function enabling to request random non-offensive GIFs.
 * (The searchterm "happy" is used to get non-offensive GIFs.)
 */
function grab_data_non_off()
{

    // Using the Tenor-API-Key and the searchterm "happy" to get non-offensive GIFs.
    var search_url = "https://api.tenor.com/v1/random?q=happy&key=SXCYAWE2GDPA&limit=8";

    httpGetAsync(search_url,tenorCallback_randomsearch_non_off);

    // data will be loaded by each call's callback
    return;
}

// callback for anonymous id
function tenorCallback_anonid(responsetext)
{
    // pass on to grab_data (offensive & non-offensive GIFs)
    grab_data_off();
    grab_data_non_off();
}

// The url to enable the use of the Tenor-API.
var url = "https://api.tenor.com/v1/anonid?key=SXCYAWE2GDPA";

// start the flow by getting a new anonymous id and having the callback pass it to grab_data
httpGetAsync(url,tenorCallback_anonid);
