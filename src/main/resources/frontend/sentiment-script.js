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
                //pie chart time selection value
                selectPieTime: '',
                pieOff: 0,
                pieNonOff: 0,
                //time selection values
                times: [
                    {text: "Letzter Tag", value: "day"},
                    {text: "Letzte Woche", value: "week"},
                    {text: "Letzter Monat", value: "month"},
                ],
                //datepicker selection, initialized with one week
                selectedDate: {
                    start:  getDate(-7),
                    end:  getDate(0)
                },
            }
        },
        methods:{

            //Update Counters
            updateCounters: function(){

                axios.all([
                    axios.get('http://basecamp-demos.informatik.uni-hamburg.de:8080/sentiment19/stats',{params: {offensive: 1}}),
                    axios.get('http://basecamp-demos.informatik.uni-hamburg.de:8080/sentiment19/stats',{params: {offensive: 0}})
                  ])
                  .then(axios.spread((off, nonOff) => {
                    this.offensive = off.data.count
                    this.nonOffensive = nonOff.data.count
                    this.updatePieChart()
                  }));

            },
            //Update Pie Chart based on selected time frame
            //To-Do: Request Dataset
            updatePieChart: function(){

                function stringDate(d){
                    return d.getFullYear() + "-" + (d.getMonth()+1) + "-" + d.getDate()
                }

                let data

                if(this.selectPieTime === ''){
                    data = [this.offensive, this.nonOffensive]
                    pieChart.data.datasets[0].data = data
                    pieChart.update()
                }else if(this.selectPieTime === 'day'){
                    axios.all([
                        axios.get('http://basecamp-demos.informatik.uni-hamburg.de:8080/sentiment19/stats',
                        {params: {offensive: 1, startdate: stringDate(getDate(0)),  enddate: stringDate(getDate(-1))}}),
                        axios.get('http://basecamp-demos.informatik.uni-hamburg.de:8080/sentiment19/stats',
                        {params: {offensive: 0, startdate: stringDate(getDate(0)),  enddate: stringDate(getDate(-1))}})
                      ])
                      .then(axios.spread((off, nonOff) => {
                        data =  [off.data.count, nonOff.data.count]
                        pieChart.data.datasets[0].data = data
                        pieChart.update()
                    }));
                }else if(this.selectPieTime === 'week'){
                    axios.all([
                        axios.get('http://basecamp-demos.informatik.uni-hamburg.de:8080/sentiment19/stats',
                        {params: {offensive: 1, startdate: stringDate(getDate(0)),  enddate: stringDate(getDate(-7))}}),
                        axios.get('http://basecamp-demos.informatik.uni-hamburg.de:8080/sentiment19/stats',
                        {params: {offensive: 0, startdate: stringDate(getDate(0)),  enddate: stringDate(getDate(-7))}})
                      ])
                      .then(axios.spread((off, nonOff) => {
                        data =  [off.data.count, nonOff.data.count]
                        pieChart.data.datasets[0].data = data
                        pieChart.update()
                    }));
                }else if(this.selectPieTime === 'month'){
                    axios.all([
                        axios.get('http://basecamp-demos.informatik.uni-hamburg.de:8080/sentiment19/stats',
                        {params: {offensive: 1, startdate: stringDate(getDate(0)),  enddate: stringDate(getDate(-30))}}),
                        axios.get('http://basecamp-demos.informatik.uni-hamburg.de:8080/sentiment19/stats',
                        {params: {offensive: 0, startdate: stringDate(getDate(0)),  enddate: stringDate(getDate(-30))}})
                      ])
                      .then(axios.spread((off, nonOff) => {
                        data =  [off.data.count, nonOff.data.count]
                        pieChart.data.datasets[0].data = data
                        pieChart.update()
                    }));
                }
            },

            //Update the line chart labels based on the selected start-/enddate
            //To-Do: Change the dataset aswell -> has to be requested from backend
            updateLineChart: function(){
                dateRange = getRangeOfDates(moment(this.selectedDate.start), moment(this.selectedDate.end), 'day')
                lineChart.data.labels=dateRange
                offData = getDataRange("off", dateRange[0], dateRange[dateRange.length-1], dateRange.length)
                nonOffData = getDataRange("nonOff", dateRange[0], dateRange[dateRange.length-1], dateRange.length)
                lineChart.data.datasets[0].data = offData
                lineChart.data.datasets[1].data = nonOffData
                lineChart.update()
            },
        }
    })

    //Used Chart.js as it seemed easier (compared to D3.js) to quickly implement the graphs we need (atleast for now)

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
            data: [3, 7, 10, 10]
        },
        {
            label: 'non-offensive',
            backgroundColor: 'rgb(0, 255, 0)',
            borderColor: 'rgb(0, 255, 0)',
            fill: false,
            data: [2, 5, 8, 10]
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

    //initialise Tweets, Line/Pie Chart and Counters
    function init(){
        displayTweet()
        vue.updateCounters()
        vue.updateLineChart()
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

//Gets range of dates  between start and end date with key (for example 'day') steps
function getRangeOfDates(start, end, key, arr = [start.startOf(key)]) {

    if(start.isAfter(end)) throw new Error('start must precede end')

    const next = moment(start).add(1, key).startOf(key);

    if(next.isAfter(end, key)) return arr.map((v) => formatDate(v.toDate()))

    return getRangeOfDates(next, end, key, arr.concat(next));

}

 //Get the tweets to be displayed
 function displayTweet(){

    //example for tweet ID of a possibly non offensive tweet
    nonOffTweetId = 507185938620219395

    //To-Do: Request html data from the backend, which retrieves the data from the Twitter-Api
    axios.get('https://cors-anywhere.herokuapp.com/https://publish.twitter.com/oembed?url=https%3A%2F%2Ftwitter.com%2Fx%2Fstatus%2F507185938620219395&align=center'
    )
        .then(function (response) {
        // handle success

        //set the corresponding div content to received html
        document.getElementById("nonOffTweet").innerHTML = response.data.html
        twttr.widgets.load()

    })
    .catch(function (error) {
        // handle error
        console.log(error)
    })
    .finally(function () {
        // always executed
    })

    //example for tweet ID of a possibly offensive tweet
    offTweetId = 1130427754971881472

    axios.get('https://cors-anywhere.herokuapp.com/https://publish.twitter.com/oembed?url=https%3A%2F%2Ftwitter.com%2Fx%2Fstatus%2F1130427754971881472&align=center'
    )
        .then(function (response) {
        // handle success

        document.getElementById("offTweet").innerHTML = response.data.html
        twttr.widgets.load()

    })
    .catch(function (error) {
        // handle error
        console.log(error)
    })
    .finally(function () {
        // always executed
    })

}

//get number of tweets for each day between start and end date for specified label, currently just returns dummy data
//To-Do: get real data from Backend
function getDataRange(label, start, end, l){
    if(label === "off"){
        //To-Do: Axios.get() request number of offensive tweets for each day from start to end

        //placeholder return
        return Array.from({length: l}, () => Math.floor(Math.random() * 25));
    }else if(label === "nonOff"){
        //To-Do: Axios.get() request number of non-offensive tweets for each day from start to end

        //placeholder return
        return Array.from({length: l}, () => Math.floor(Math.random() * 25));
    }
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

// url Async requesting function
function httpGetAsync(theUrl, callback)
{
    // create the request object
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

// callback for the random offensive search
function tenorCallback_randomsearch_off(responsetext)
{
    // parse the json response
    var response_objects = JSON.parse(responsetext);

    top_10_gifs = response_objects["results"];

    // load the GIFs -- for our example we will load the first GIFs preview size (nanogif) and share size (tinygif)

    document.getElementById("Off_gif").src = top_10_gifs[0]["media"][0]["tinygif"]["url"];

    return;

}

// callback for the random non-offensive search
function tenorCallback_randomsearch_non_off(responsetext)
{
    // parse the json response
    var response_objects = JSON.parse(responsetext);

    top_10_gifs = response_objects["results"];

    // load the GIFs
    document.getElementById("Non_Off_gif").src = top_10_gifs[0]["media"][0]["nanogif"]["url"];

    return;

}


// function to request random offensive gifs for a given search term
function grab_data_off()
{

    // using default locale of en_US
    var search_url = "https://api.tenor.com/v1/random?q=angry&key=SXCYAWE2GDPA&limit=8";

    httpGetAsync(search_url,tenorCallback_randomsearch_off);

    // data will be loaded by each call's callback
    return;
}

// function to request random non-offensive gifs for a given search term
function grab_data_non_off()
{

    // using default locale of en_US
    var search_url = "https://api.tenor.com/v1/random?q=happy&key=SXCYAWE2GDPA&limit=8";

    httpGetAsync(search_url,tenorCallback_randomsearch_non_off);

    // data will be loaded by each call's callback
    return;
}

// callback for anonymous id -- for first time users
function tenorCallback_anonid(responsetext)
{
    // pass on to grab_data (offensive & non-offensive Gifs)
    grab_data_off();
    grab_data_non_off();
}

var url = "https://api.tenor.com/v1/anonid?key=SXCYAWE2GDPA";

// start the flow by getting a new anonymous id and having the callback pass it to grab_data
httpGetAsync(url,tenorCallback_anonid);
