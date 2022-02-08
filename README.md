# End-to-End-Big-data-Architecture
After three weeks of hard work and more #coffee i am very happy to show my last Big Data project("Real-Time Tweets Search Engine ") that will store and index streaming tweets ranked by sentiment of tweets and spam filter and support auto-complete search based on #Bigdata Techonolgies

In this project I am presenting an end-to-end architecture and how it builds and works !!

First Part , I wrote my custom twitterUtils that will fetch streaming data from twitter and publish them to kafka topic [UnProccessedTweets] the purpose of using Kafka it acts here as a long term log storage is preferred for preventing data loss if streaming processing encounters any problem (network connection, server inaccessibility, etc.). Kafka also provides semantic (exactly-once) to prevent data duplication as well as to achive compatibility between the continuation rate of producing data stream and consumers capability

Second Part :- Once data stored in kafka topic we need a streaming proccesing system that will consume and process data streams in this case we used (Spark Streaming) to transform data and to apply some analytics like (spam detection , sentiment analysis , etc ..) then it will send the data after processing to another kafka topic called [ProccessedTweets] 
 
Third Part :- We need to store and index tweets for full-text search as well as we want to store them in cold storage layer like (Hdfs) for the futher batch processing (you can use Hive , SparkSQL , Pig , etc ..)or train and build machine learning model (SparkMlib , etc..)

After data stored in messaging bus system(kafka) we need a way to store them into hdfs (apache Flume)
Flume is a data ingestion tool that moves data from one place to another. In Kafka, the Flume is integrated for streaming a high volume of data logs from Source(kafka) to Destination for Storing data in HDFS.

On the other hand Logstash will consume tweets from kafka and apply pre-procceessing for tweets to be ready to store and index to elasticsearch 

Kibana is a user interface that lets you visualize your Elasticsearch data and navigate the Elastic Stack and create your dashboards

Flask or other o Web frameworks acts here as interactive layer between elasticsearch and users.

Now !! you have an expandable search engine and add other data sources : )

[link of video demo](https://www.linkedin.com/posts/mohamed-adel-hassan-312b8a167_bigdata-kafka-kafkastreams-activity-6840997072326410240-U8-U)

![1629782250334](https://user-images.githubusercontent.com/58120325/153014619-a39c71ae-bc47-48bc-8b00-de86ba8fdae6.jpg)

