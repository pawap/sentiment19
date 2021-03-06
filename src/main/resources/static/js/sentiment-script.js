window.addEventListener('load', function(){
    //Vue component for the classifer (modal)
    Vue.component('modal-classifier', {
        data: function () {
            return {
                //the current status of the classifier
                status: "not classified",
                //the users input in the classifier
                input: ""
            }
        },
        template: "#modal-classifier",
        methods: {
            /**
             * Classifies the users input and returns an answer
             */
            classifyInput: function(){
                this.status = "classifying"
                axios
                    .get("/sentiment19/classify?tweet=" + this.input)
                    .then(response => {
                        this.status = (response.data.offensive ? "offensive" : "nonoffensive")
                            + " (" + (Number.parseFloat(response.data.probability) * 100).toFixed(2) + "%)"
                    })
            },

            /**
             * Resets the modal (input and status text)
             */
            addModalEvent: function(){
                $(".modal").on("show.bs.modal", function(){
                    this.status = "not classified";
                    $(this).find("textarea").val('');
                });
            }
        }
    });

    Vue.component('popular-hashtags', {
        data: function () {
            return {
                popularHashtags: []
            }
        },
        template: "#popular-hashtags",
        methods: {
            setTags: function (tags) {
                this.popularHashtags = tags;
                this.$nextTick(() => $(function(){
                    $('.has-tooltip').tooltip({trigger: "hover", html:true});
                    $('.tooltip').remove();
                }));
            }
        }
    });

    Vue.component('tweet-filter', {
        props: ['value'],
        data: function() {
            return {
                times: [
                    {text: "Letzter Tag", value: "day", start: getDate(-1), end: getDate(0)},
                    {text: "Letzte Woche", value: "week", start: getDate(-7), end: getDate(0)},
                    {text: "Letzter Monat", value: "month", start: getDate(-30), end: getDate(0)},
                    {text: "Gesamter Zeitraum", value: "range", start: null, end: getDate(0)},
                ],
                availableLanguages: [],
                selectedLanguages: []
            }
        },
        template: "#tweet-filter",
        created: function() {
            axios.get('/sentiment19/availablelanguages')
                .then(response => this.availableLanguages = response.data.availableLanguages)
        },
        methods: {
            addHashtag: function (newTag = null) {
                let tag =  newTag;
                if(!tag.value) {
                    let newHashtag = $('#newhashtag');
                    tag = {
                        value: newHashtag.val()
                    };
                    if (tag.value.substr(0,1) !== "#") tag.value = '#' + tag.value;
                    newHashtag.val('');
                }
                this.value.hashtags.push(tag);
            },
            setPopularHashtags: function (tags) {
                this.$refs.popularhashtags.setTags(tags);
            },
            disableLoading: function () {
                $('#overlay').fadeOut();
                $('.tweetfilter-content').removeClass('disabled');
                $('#apply-button').attr('disabled', false);
            },
            filterUpdated: function () {
                let languages = (this.selectedLanguages.length > 0) ?
                    this.selectedLanguages :
                    this.availableLanguages.map(lang => lang.iso);

                $('#overlay').css("display","flex").hide().fadeIn();
                $('.tweetfilter-content').addClass('disabled');
                $('#apply-button').attr('disabled', true);
                this.$emit('input', {
                    selectedDate: this.value.selectedDate,
                    hashtags: this.value.hashtags,
                    languages: languages
                }
                )
            }
        },
    });



    const vue = new Vue({
        el: "#app",
        data: function(){
            return{
                //counter number of offensive tweets
                offensive: 0,
                //counter number of offensive tweets
                nonOffensive: 0,
                //radio buttons time selection values

                // -------- FILTERS -----------


                tweetFilter: {
                    //datepicker selection, initialized with one week
                    selectedDate: {
                        start: getDate(-7),
                        end: getDate(0)
                    },
                    // hashtags empty init
                    hashtags: [],

                    //languages empty init
                    languages: []
                }
            }
        },
        methods:{
            updateHashtags: function (callback = function(){}) {
                axios.post('/sentiment19/popularhashtags?limit=8',this.getCurrentFilter())
                    .then(response => {
                        let popularHashtags = response.data.hashtags.map(function (tag, index) {
                            return {
                                value: tag.hashtag, key: index, popular: 1, hidden: 0, count: tag.count,
                                percent: (tag.count / response.data.total * 100).toFixed(2) + '%'
                            };

                        });
                        popularHashtags.forEach(function (e) {
                            if (vue.tweetFilter.hashtags.filter(tag => (tag.value === e.value)).length > 0) {
                                e.hidden = 1;
                            }
                        });
                        callback();
                        this.$refs.tweetfilter.setPopularHashtags(popularHashtags);
                    })



            },
            /**
             * Updates the main offensive and nonOffensive tweet amount counters
             */
            updateCounters: function(callback = function(){}){
                axios.all([
                    axios.post('/sentiment19/stats',this.getCurrentFilterByOffensive(1)),
                    axios.post('/sentiment19/stats',this.getCurrentFilterByOffensive(0))
                  ])
                  .then(axios.spread((off, nonOff) => {
                    this.offensive = off.data.count
                    this.nonOffensive = nonOff.data.count
                    callback();
                  }));
                
            },
            /**
             * Updates bar chart, based on selected filter
             */
            updateBarChart: function(callback = function(){}){
                axios.post('/sentiment19/popularhashtags?limit=5',this.getCurrentFilterByOffensive(0))
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
                        callback();
                    })
                axios.post('/sentiment19/popularhashtags?limit=5',this.getCurrentFilterByOffensive(1))
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
             * Updates pie chart
             */
            updatePieChart: function(callback = function(){}){
                axios.all([
                    axios.post('/sentiment19/stats',
                        this.getCurrentFilterByOffensive(1)),
                    axios.post('/sentiment19/stats',
                        this.getCurrentFilterByOffensive(0))
                  ])
                  .then(axios.spread((off, nonOff) => {
                    data =  [off.data.count, nonOff.data.count]
                    pieChart.data.datasets[0].data = data
                    pieChart.update()
                      callback();
                  }));

            },
            /**
             * Updates offensive and non-offensive example tweets
             */
            updateTweets: function(callback = function(){}){
                axios.all([
                    axios.post('/sentiment19/tweet',
                        this.getCurrentFilterByOffensive(1)),
                    axios.post('/sentiment19/tweet',
                        this.getCurrentFilterByOffensive(0))
                ])
                    .then(axios.spread((offTweet, nonOffTweet) => {
                        document.getElementById("offTweet").innerHTML = offTweet.data.html
                        document.getElementById("nonOffTweet").innerHTML = nonOffTweet.data.html
                        twttr.widgets.load()
                        callback();
                    }));
            },

            /**
             * Updates the line chart (labels and data)
             */
            updateLineChart: function(callback = function(){}){
                axios.all([
                    axios.post('/sentiment19/timeline', this.getCurrentFilterByOffensive(1)),
                    axios.post('/sentiment19/timeline', this.getCurrentFilterByOffensive(0))
                ])
                    .then(axios.spread((off, nonOff) => {
                        console.log(off.data.start);
                        dateRange = getRangeOfDates(off.data.start, new Date(off.data.end));
                        lineChart.data.labels = dateRange
                        lineChart.data.datasets[0].data = off.data.timeline
                        lineChart.data.datasets[1].data = nonOff.data.timeline
                        lineChart.update();
                        callback();
                    }));

            },
            updateEmotionGifs: function () {
                axios.all([
                    axios.get('https://api.tenor.com/v1/random?q=angry&key=SXCYAWE2GDPA&limit=8'),
                    axios.post('https://api.tenor.com/v1/random?q=happy&key=SXCYAWE2GDPA&limit=8')
                ])
                    .then(axios.spread((off, nonOff) => {
                        $('#Off_gif').attr("src", off.data.results[0].media[0].tinygif.url);
                        $('#Non_Off_gif').attr("src", nonOff.data.results[0].media[0].tinygif.url);
                    }));

            },
            /**
             * Returns the current filter including offensive / non-offensive selection
             * @param {*} offensive whether the filter is for offensive (1) or non-offensive (0) tweets
             */
            getCurrentFilterByOffensive: function(offensive){
                return {
                    offensive: offensive,
                    start: this.tweetFilter.selectedDate.start,
                    end: this.tweetFilter.selectedDate.end,
                    hashtags: this.tweetFilter.hashtags.map(function (o) {
                        return o.value
                    }),
                    languages: this.tweetFilter.languages
                }
            },
            /**
             * Returns current filter
             */
            getCurrentFilter: function(){
                return {
                    start: this.tweetFilter.selectedDate.start,
                    end: this.tweetFilter.selectedDate.end,
                    hashtags: this.tweetFilter.hashtags.map(function (o) {
                        return o.value
                    }),
                    languages: this.tweetFilter.languages
                }
            },
            enableTenorApi() {
                axios.get('https://api.tenor.com/v1/anonid?key=SXCYAWE2GDPA');
            }
        },
        /**
         * Updates the view, when data is changed
         */
        updated: function () {
            let counter = 6;
            let callback = function () {
                counter--;
                if (counter <= 0) {
                    vue.$refs.tweetfilter.disableLoading();
                }
            };
            this.updateHashtags(callback);
            this.updatePieChart(callback);
            this.updateLineChart(callback);
            this.updateTweets(callback);
            this.updateBarChart(callback);
            this.updateCounters(callback);
            this.updateEmotionGifs();
        },
        created: function () {
            this.enableTenorApi();
        }
    });


    //Barchart for top non-offensive Hashtags
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
                text: 'Top non-offensive Hashtags for chosen timeframe'
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
    //Barchart for top offensive Hashtags
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
                text: 'Top offensive Hashtags for chosen timeframe',
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
                backgroundColor: ['rgb(253, 51, 53)','rgb(111, 157, 60)'],
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
                backgroundColor: 'rgb(253, 51, 53)',
                borderColor: 'rgb(253, 51, 53)',
                fill: false,
                data: [1,2,3,4]
                },
                {
                label: 'non-offensive',
                backgroundColor: 'rgb(111, 157, 60)',
                borderColor: 'rgb(111, 157, 60)',
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
     * Initialising / updating methods (charts, tweets, hashtags and counters) for startup
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
                $('[data-toggle="tooltip"]').tooltip({ trigger: "hover", html:true});
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
 * Returns range of dates, between specified start and end date
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