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
                //Pie chart time selection values
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

                //status of classifier
                classifierStatus: "ist noch nicht klassifiziert"
            }
        },
        methods:{

            /**
             * Updates the main offensive and nonOffensive tweet amount counters (and initiates Pie Chart update)
             */
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
            /**
             * Updates pie chart, based on selected time frame
             */
            updatePieChart: function(){

                //local function to format given date to match the required format for backend request
                function stringDate(d){
                    return d.getFullYear() + "-" + (d.getMonth()+1) + "-" + d.getDate()
                }

                let data
                let startDate = stringDate(getDate(0))
                let endDate
                
                //if selected time frame is not further specified, counts from entire period are used (value the same as the counters)
                if(this.selectPieTime === ''){
                    data = [this.offensive, this.nonOffensive]
                    pieChart.data.datasets[0].data = data
                    pieChart.update()
                //Request data for chosen time frame from backend
                } else {
                    if(this.selectPieTime === 'day'){
                        endDate = stringDate(getDate(-1))
                    }else if(this.selectPieTime === 'week'){
                        endDate = stringDate(getDate(-7))
                    }else if(this.selectPieTime === 'month'){
                        endDate = stringDate(getDate(-30))
                    }
                    axios.all([
                        axios.get('http://basecamp-demos.informatik.uni-hamburg.de:8080/sentiment19/stats',
                        {params: {offensive: 1, startdate: startDate,  enddate: endDate}}),
                        axios.get('http://basecamp-demos.informatik.uni-hamburg.de:8080/sentiment19/stats',
                        {params: {offensive: 0, startdate: startDate,  enddate: endDate}})
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

            //To-Do: Post input to BE and receive classifier response AND reset modal after closing
            classifyInput: function(){
                var input = document.getElementById('classifierInput').value
                console.log(input)
                this.classifierStatus = "wird klassifiziert"
            },

            //
            addModalEvent: function(){
                $(".modal").on("show.bs.modal", function(){
                    vue.classifierStatus = "ist noch nicht klassifiziert";
                    $(this).find("textarea").val('');
                });
            }

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
        displayTweet()
        vue.updateCounters()
        vue.updateLineChart()
        vue.addModalEvent()
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
 * @param {*} key Increment size to be used for each step
 * @param {*} arr Resulting array - does not need to be provided on function call
 * @returns (Already formmatted) date range
 */
function getRangeOfDates(start, end, key, arr = [start.startOf(key)]) {
  
    if(start.isAfter(end)) throw new Error('start must precede end')
    
    const next = moment(start).add(1, key).startOf(key);
    
    if(next.isAfter(end, key)) return arr.map((v) => formatDate(v.toDate()))
    
    return getRangeOfDates(next, end, key, arr.concat(next));
    
}

/**
 * Requests html code for offensive/nonoffensive Tweets and displays them as examples
 */
 function displayTweet(){

    axios.all([
        axios.get('http://basecamp-demos.informatik.uni-hamburg.de:8080/sentiment19/tweet',
        {params: {offensive: 1}}),
        axios.get('http://basecamp-demos.informatik.uni-hamburg.de:8080/sentiment19/tweet',
        {params: {offensive: 0}})
      ])
      .then(axios.spread((offTweet, nonOffTweet) => {
        document.getElementById("offTweet").innerHTML = offTweet.data.html
        document.getElementById("nonOffTweet").innerHTML = nonOffTweet.data.html
        twttr.widgets.load()
    }));
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