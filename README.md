# Financial Time series Feature Engineering in Java

## finfeat4j-core

Base functions to compute different financial time series features (indicators).
Includes labeling algorithms and very simple trading engine.

Some usage examples:
```java
Stream<Bar> barStream = ... // Stream of bars
var indicatorSet = new IndicatorSet(
    new Close(), // Close price 
    new Close().then(new SMA(3)), // SMA over 3 bars for Close price
    new Close().then(new SMA(3)).then(new ROC(2)) // Rate of Change for two bars of SMA computed over 3 bars for Close price
); 
var dataset = indicatorSet.transform(barStream); // will return transformed dataset with 3 computed features
```

## finfeat4j-ml

Base functions to apply discretization (using SFA) and perform feature selection and classification.

## References
```
@inproceedings{10.1145/2247596.2247656,            
author = {Schäfer, Patrick and Högqvist, Mikael},
title = {SFA: a symbolic fourier approximation and index for similarity search in high dimensional datasets},
year = {2012},
isbn = {9781450307901},
publisher = {Association for Computing Machinery},
address = {New York, NY, USA},
url = {https://doi.org/10.1145/2247596.2247656},
doi = {10.1145/2247596.2247656},
abstract = {Time series analysis, as an application for high dimensional data mining, is a common task in biochemistry, meteorology, climate research, bio-medicine or marketing. Similarity search in data with increasing dimensionality results in an exponential growth of the search space, referred to as Curse of Dimensionality. A common approach to postpone this effect is to apply approximation to reduce the dimensionality of the original data prior to indexing. However, approximation involves loss of information, which also leads to an exponential growth of the search space. Therefore, indexing an approximation with a high dimensionality, i. e. high quality, is desirable.We introduce Symbolic Fourier Approximation (SFA) and the SFA trie which allows for indexing of not only large datasets but also high dimensional approximations. This is done by exploiting the trade-off between the quality of the approximation and the degeneration of the index by using a variable number of dimensions to represent each approximation. Our experiments show that SFA combined with the SFA trie can scale up to a factor of 5--10 more indexed dimensions than previous approaches. Thus, it provides lower page accesses and CPU costs by a factor of 2--25 respectively 2--11 for exact similarity search using real world and synthetic data.},
booktitle = {Proceedings of the 15th International Conference on Extending Database Technology},
pages = {516–527},
numpages = {12},
keywords = {data mining, discretisation, indexing, symbolic representation, time series},
location = {Berlin, Germany},
series = {EDBT '12}
}
```
```
@Article{e22101162,
AUTHOR = {Wu, Dingming and Wang, Xiaolong and Su, Jingyong and Tang, Buzhou and Wu, Shaocong},
TITLE = {A Labeling Method for Financial Time Series Prediction Based on Trends},
JOURNAL = {Entropy},
VOLUME = {22},
YEAR = {2020},
NUMBER = {10},
ARTICLE-NUMBER = {1162},
URL = {https://www.mdpi.com/1099-4300/22/10/1162},
PubMedID = {33286931},
ISSN = {1099-4300},
ABSTRACT = {Time series prediction has been widely applied to the finance industry in applications such as stock market price and commodity price forecasting. Machine learning methods have been widely used in financial time series prediction in recent years. How to label financial time series data to determine the prediction accuracy of machine learning models and subsequently determine final investment returns is a hot topic. Existing labeling methods of financial time series mainly label data by comparing the current data with those of a short time period in the future. However, financial time series data are typically non-linear with obvious short-term randomness. Therefore, these labeling methods have not captured the continuous trend features of financial time series data, leading to a difference between their labeling results and real market trends. In this paper, a new labeling method called “continuous trend labeling” is proposed to address the above problem. In the feature preprocessing stage, this paper proposed a new method that can avoid the problem of look-ahead bias in traditional data standardization or normalization processes. Then, a detailed logical explanation was given, the definition of continuous trend labeling was proposed and also an automatic labeling algorithm was given to extract the continuous trend features of financial time series data. Experiments on the Shanghai Composite Index and Shenzhen Component Index and some stocks of China showed that our labeling method is a much better state-of-the-art labeling method in terms of classification accuracy and some other classification evaluation metrics. The results of the paper also proved that deep learning models such as LSTM and GRU are more suitable for dealing with the prediction of financial time series data.},
DOI = {10.3390/e22101162}
}
```

Until any documentation will be available, please refer to the test cases for more examples.