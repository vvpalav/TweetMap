# Tweet Map with Sentiment Analysis.

This application allow users to search tweets related to any twitter id and perform sentiment analysis on it using Alchemy API. 
User needs to enter twitter id of interested user and click on search.
Application grabs those tweets for inputted twitter id whose latitude and longitude is not null, perform Sentiment Analysis on it
and plots all the tweets on. Google Map with the information like username, sentiment (Positive, Negative and Neutral), location etc.

#####Alchemy API Reference: http://www.alchemyapi.com/products/alchemylanguage/sentiment-analysis

#####Amazon Web Services:
We are using various Amazon Web Services in the server side to fetch and process tweets.
We are using **Amazon RDS, Elastic Beanstalk, SQS, SNS, Amazon EC2, Load Balancer** etc
